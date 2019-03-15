/*
 * Copyright 2018 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.adaptris.core.services.splitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventHandler;
import com.adaptris.core.Service;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

public class ServiceWorkerPool {
  private static final long EVICT_RUN = new TimeInterval(60L, TimeUnit.SECONDS).toMilliseconds();

  private transient Service wrappedService;
  private transient EventHandler eventHandler;
  private transient int maxThreads;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  private static transient boolean warningLogged = false;
  public ServiceWorkerPool(Service s, EventHandler eh, int maxThreads) throws CoreException {
    try {
      this.wrappedService = Args.notNull(s, "service");
      this.eventHandler = eh;
      this.maxThreads = maxThreads;
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  private Service cloneService(Service original) throws Exception {
    Service result = DefaultMarshaller.roundTrip(original);
    LifecycleHelper.registerEventHandler(result, eventHandler);
    return result;
  }

  public GenericObjectPool<Worker> createCommonsObjectPool() throws CoreException {
    GenericObjectPool<Worker> pool = new GenericObjectPool<>(new WorkerFactory());
    // Make the pool the same size as the thread pool
    pool.setMaxTotal(maxThreads);
    pool.setMinIdle(maxThreads);
    pool.setMaxIdle(maxThreads);
    pool.setMaxWaitMillis(-1L);
    pool.setBlockWhenExhausted(true);
    pool.setMinEvictableIdleTimeMillis(EVICT_RUN);
    pool.setTimeBetweenEvictionRunsMillis(
        EVICT_RUN + ThreadLocalRandom.current().nextLong(EVICT_RUN));
    return pool;
  }

  /**
   * 
   * @deprecated since 3.8.3 switch to commons-pool2 and {@link createCommonsObjectPool()} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "use commons-pool2 + createCommonsObjectPool()")
  public org.apache.commons.pool.impl.GenericObjectPool<Worker> createObjectPool() throws CoreException {
    logDeprecationWarning();
    org.apache.commons.pool.impl.GenericObjectPool<Worker> pool = new org.apache.commons.pool.impl.GenericObjectPool(
        new LegacyCommonsPoolWorkerFactory());
    pool.setMaxActive(maxThreads);
    pool.setMinIdle(maxThreads);
    pool.setMaxIdle(maxThreads);
    pool.setMaxWait(-1L);
    pool.setWhenExhaustedAction(org.apache.commons.pool.impl.GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
    pool.setMinEvictableIdleTimeMillis(EVICT_RUN);
    pool.setTimeBetweenEvictionRunsMillis(
        EVICT_RUN + ThreadLocalRandom.current().nextLong(EVICT_RUN));
    return pool;
  }
  
  public ExecutorService createExecutor(String prefix) {
    return Executors.newFixedThreadPool(maxThreads, new ManagedThreadFactory(prefix));
  }


  public static void closeQuietly(ObjectPool<?> pool) {
    try {
      if (pool != null) pool.close();
    } catch (Exception ignored) {

    }
  }

  /**
   * 
   * @deprecated since 3.8.3 switch to commons-pool2 instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message="use commons-pool2 variant instead")
  public static void closeQuietly(org.apache.commons.pool.ObjectPool<?> pool) {
    logDeprecationWarning();
    try {
      if (pool != null) pool.close();
    } catch (Exception ignored) {

    }
  }
  
  /**
   * 
   * @deprecated since 3.8.3 switch to commons-pool2 instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message="use commons-pool2 variant instead")
  public void warmup(final org.apache.commons.pool.impl.GenericObjectPool<Worker> objectPool) throws CoreException {
    logDeprecationWarning();
    ExecutorService populator = Executors.newCachedThreadPool(new ManagedThreadFactory(this.getClass().getSimpleName()));
    try {
      log.trace("Warming up {} service-workers", maxThreads);
      final List<Future<Worker>> futures = new ArrayList<>(maxThreads);

      for (int i = 0; i < maxThreads; i++) {
        futures.add(populator.submit(new Callable<Worker>() {

          @Override
          public Worker call() throws Exception {
            return objectPool.borrowObject();
          }

        }));
      }
      for (Worker w : waitFor(futures)) {
        objectPool.returnObject(w);
      }
      log.trace("ObjectPool contains {} (active) of {} objects", objectPool.getNumActive(), objectPool.getNumIdle());

    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    finally {
      populator.shutdownNow();
    }
  }
  
  public void warmup(final GenericObjectPool<Worker> objectPool) throws CoreException {
    ExecutorService populator = Executors.newCachedThreadPool(new ManagedThreadFactory(this.getClass().getSimpleName()));
    try {
      log.trace("Warming up {} service-workers", maxThreads);
      final List<Future<Worker>> futures = new ArrayList<>(maxThreads);

      for (int i = 0; i < maxThreads; i++) {
        futures.add(populator.submit(new Callable<Worker>() {

          @Override
          public Worker call() throws Exception {
            return objectPool.borrowObject();
          }

        }));
      }
      for (Worker w : waitFor(futures)) {
        objectPool.returnObject(w);
      }
      log.trace("ObjectPool contains {} (active) of {} objects", objectPool.getNumActive(), objectPool.getNumIdle());
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    finally {
      populator.shutdownNow();
    }
  }
  
  
  private static void logDeprecationWarning() {
    LoggingHelper.logWarning(warningLogged, () -> {
      warningLogged = true;
    }, "switch to using commons-pool2; commons-pool support will be removed w/o warning");    
  }
  
  private List<Worker> waitFor(List<Future<Worker>> tasks) throws Exception {
    List<Worker> result = new ArrayList<>();
    do {
      for (Iterator<Future<Worker>> i = tasks.iterator(); i.hasNext();) {
        Future<Worker> f = i.next();
        if (f.isDone()) {
          result.add(f.get());
          i.remove();
        }
      }
      if (tasks.size() > 0) {
        LifecycleHelper.waitQuietly(100);
      }
    }
    while (tasks.size() > 0);
    return result;
  }

  public class Worker {
    private Service workerService;

    Worker() throws Exception {
      workerService = cloneService(wrappedService);
    }

    public Worker start() throws CoreException {
      LifecycleHelper.initAndStart(workerService, false);
      return this;
    }

    public Worker stop() {
      LifecycleHelper.stopAndClose(workerService, false);
      return this;
    }

    public boolean isValid() {
      return true;
    }

    public AdaptrisMessage doService(AdaptrisMessage msg) throws Exception {
      workerService.doService(msg);
      return msg;
    }

  }

  class WorkerFactory implements PooledObjectFactory<Worker> {

    WorkerFactory() {
    }

    @Override
    public PooledObject<Worker> makeObject() throws Exception {
      Worker w = new Worker();
      w.start();
      return new DefaultPooledObject<>(w);
    }

    @Override
    public void destroyObject(PooledObject<Worker> arg0) throws Exception {
      arg0.getObject().stop();
    }

    @Override
    public boolean validateObject(PooledObject<Worker> arg0) {
      return arg0.getObject().isValid();
    }

    @Override
    public void activateObject(PooledObject<Worker> arg0) throws Exception {
    }

    @Override
    public void passivateObject(PooledObject<Worker> arg0) throws Exception {
    }

  }
  
  @Deprecated
  class LegacyCommonsPoolWorkerFactory implements org.apache.commons.pool.PoolableObjectFactory<Worker> {

    LegacyCommonsPoolWorkerFactory() {
    }

    @Override
    public Worker makeObject() throws Exception {
      return new Worker().start();
    }

    @Override
    public void destroyObject(Worker w) throws Exception {
      w.stop();
    }

    @Override
    public boolean validateObject(Worker w) {
      return w.isValid();
    }

    @Override
    public void activateObject(Worker arg0) throws Exception {
    }

    @Override
    public void passivateObject(Worker arg0) throws Exception {
    }

}

}
