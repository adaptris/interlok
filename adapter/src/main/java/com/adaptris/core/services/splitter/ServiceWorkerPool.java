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

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventHandler;
import com.adaptris.core.Service;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

class ServiceWorkerPool {
  private static final long EVICT_RUN = new TimeInterval(60L, TimeUnit.SECONDS).toMilliseconds();

  private transient Service wrappedService;
  private transient EventHandler eventHandler;
  private transient int maxThreads;

  ServiceWorkerPool(Service s, EventHandler eh, int maxThreads) throws CoreException {
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

  GenericObjectPool<Worker> createObjectPool() {
    GenericObjectPool<Worker> pool = new GenericObjectPool(new WorkerFactory());
    // Make the pool the same size as the thread pool
    pool.setMaxActive(maxThreads);
    pool.setMinIdle(maxThreads);
    pool.setMaxIdle(maxThreads);
    pool.setMaxWait(-1L);
    pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
    pool.setMinEvictableIdleTimeMillis(EVICT_RUN);
    pool.setTimeBetweenEvictionRunsMillis(EVICT_RUN + new Random(EVICT_RUN).nextLong());
    return pool;
  }

  ExecutorService createExecutor(String prefix) {
    return Executors.newFixedThreadPool(maxThreads, new ManagedThreadFactory(prefix));
  }

  static void closeQuietly(ObjectPool pool) {
    try {
      if (pool != null) pool.close();
    } catch (Exception ignored) {

    }
  }

  class Worker {
    private Service workerService;

    Worker() throws Exception {
      workerService = cloneService(wrappedService);
    }

    public Worker start() throws CoreException {
      LifecycleHelper.initAndStart(workerService);
      return this;
    }

    public Worker stop() {
      LifecycleHelper.stopAndClose(workerService);
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

  class WorkerFactory implements PoolableObjectFactory<Worker> {

    WorkerFactory() {
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
