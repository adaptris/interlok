/*
 * Copyright 2021 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.validation.Valid;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A Workflow that has a object pool of ServiceCollections
 *
 */
public abstract class WorkflowWithObjectPool extends WorkflowImp {

  /**
   * The default maximum pool size.
   *
   */
  public static final int DEFAULT_MAX_POOLSIZE = 10;
  /**
   * the default minimum idle size.
   *
   */
  public static final int DEFAULT_MIN_IDLE = 1;
  /**
   * The default max idle size.
   */
  public static final int DEFAULT_MAX_IDLE = DEFAULT_MAX_POOLSIZE;


  /**
   * The default wait time for pool initialisation
   *
   */
  private static final TimeInterval DEFAULT_INIT_WAIT = new TimeInterval(1L, TimeUnit.MINUTES.name());

  /**
   * The max size of the pool
   *
   */
  @InputFieldDefault(value = "10")
  @Getter
  @Setter
  private Integer poolSize;
  /**
   * The minimum number of idle objects in the pool.
   *
   */
  @InputFieldDefault(value = "1")
  @Getter
  @Setter
  private Integer minIdle;
  /**
   * The maximum number of idle objects in the pool.
   *
   */
  @InputFieldDefault(value = "10")
  @Getter
  @Setter
  private Integer maxIdle;

  /**
   * Set the amount of time to wait for object pool population.
   * <p>
   * Upon start the object pool is populated with the {@link #minIdle()} number of workers.
   * </p>
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "1 minute")
  @Valid
  @Getter
  @Setter
  private TimeInterval initWaitTime;

  public WorkflowWithObjectPool() {
    super();
  }

  public WorkflowWithObjectPool(String uniqueId) throws CoreException {
    this();
    setUniqueId(uniqueId);
  }

  /**
   * Process a message from the <code>MessageConsumer</code>
   *
   * @see WorkflowImp#handleBadMessage(AdaptrisMessage)
   *
   */
  @Override
  public void onAdaptrisMessage(final AdaptrisMessage msg, Consumer<AdaptrisMessage> success,
      Consumer<AdaptrisMessage> failure) {
    ListenerCallbackHelper.prepare(msg, success, failure);
    if (!obtainChannel().isAvailable()) {
      handleChannelUnavailable(msg);
    } else {
      onMessage(msg);
    }
  }

  protected abstract void onMessage(AdaptrisMessage msg);

  @Override
  protected void resubmitMessage(AdaptrisMessage msg) {
    onMessage(msg);
  }

  public int poolSize() {
    return NumberUtils.toIntDefaultIfNull(getPoolSize(), DEFAULT_MAX_POOLSIZE);
  }

  /**
   * Check the object pool such that it isn't going to cause issues.
   *
   */
  protected void checkPoolConfig() {
    if (maxIdle() > poolSize()) {
      log.warn("Maximum number of idle workers > pool-size, re-sizing max-idle");
      setMaxIdle(poolSize());
    }
    if (minIdle() > poolSize()) {
      log.warn("Minimum number of idle workers > pool-size, re-sizing min-idle");
      setMinIdle(poolSize());
    }
    if (minIdle() > maxIdle()) {
      log.warn("Minimum number of idle workers > max-idle, max-idle modified");
      setMaxIdle(minIdle());
    }
  }

  protected ObjectPool<Worker> createObjectPool() throws CoreException {
    checkPoolConfig();
    ServiceCollection workerServiceCollection = cloneServiceCollection(getServiceCollection());
    GenericObjectPool<Worker> pool =
        new GenericObjectPool<>(new WorkerFactory(workerServiceCollection));
    long lifetime = DEFAULT_INIT_WAIT.toMilliseconds();
    pool.setMaxTotal(poolSize());
    pool.setMinIdle(minIdle());
    pool.setMaxIdle(maxIdle());
    pool.setMaxWaitMillis(-1L);
    pool.setBlockWhenExhausted(true);
    pool.setSoftMinEvictableIdleTimeMillis(lifetime);
    pool.setTimeBetweenEvictionRunsMillis(
        lifetime + ThreadLocalRandom.current().nextLong(lifetime));
    return pool;
  }

  protected void populatePool(ObjectPool<Worker> objectPool) throws CoreException {
    int size = minIdle();
    ExecutorService populator = Executors.newCachedThreadPool();
    try {
      final CyclicBarrier barrier = new CyclicBarrier(size + 1);
      log.trace("Need more ({}) children as soon as possible to handle work. Get to it", size);
      for (int i = 0; i < size; i++) {
        populator.execute(new Runnable() {
          @Override
          public void run() {
            try {
              objectPool.addObject();
              barrier.await(initWaitTimeMs(), TimeUnit.MILLISECONDS);
            }
            catch (Exception e) {
              barrier.reset();
            }
          }
        });
      }
      barrier.await(initWaitTimeMs(), TimeUnit.MILLISECONDS);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    } finally {
      populator.shutdownNow();
    }
  }

  /**
   * Return the maximum idle objects in the pool.
   *
   * @return the maximum idle number
   */
  public int minIdle() {
    return NumberUtils.toIntDefaultIfNull(getMinIdle(), DEFAULT_MIN_IDLE);
  }

  /**
   * Return the maximum idle objects in the pool.
   *
   * @return the maximum idle number
   */
  public int maxIdle() {
    return NumberUtils.toIntDefaultIfNull(getMaxIdle(), DEFAULT_MAX_IDLE);
  }

  public long initWaitTimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getInitWaitTime(), DEFAULT_INIT_WAIT);
  }


  protected ServiceCollection cloneServiceCollection(ServiceCollection original)
      throws CoreException {
    ServiceCollection result = DefaultMarshaller.roundTrip(original);
    LifecycleHelper.registerEventHandler(result, eventHandler);
    return result;
  }

  @Override
  protected void prepareWorkflow() throws CoreException {}

  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  protected class WorkerFactory implements PooledObjectFactory<Worker> {

    private transient ServiceCollection baseCollection;

    @Override
    public PooledObject<Worker> makeObject() throws Exception {
      Worker w = null;
      try {
        w = new Worker(cloneServiceCollection(baseCollection));
        w.start();
      }
      catch (Exception e) {
        log.error("Error creating object for pool", e);
        throw e;
      }
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

  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  protected class Worker {

    private ServiceCollection sc;

    public void start() throws CoreException {
      LifecycleHelper.initAndStart(sc, false);
    }

    public void stop() {
      LifecycleHelper.stopAndClose(sc, false);
    }

    public boolean isValid() {
      return true;
    }

    public AdaptrisMessage handleMessage(AdaptrisMessage msg) {
      AdaptrisMessage wip = null;
      try {
        long start = System.currentTimeMillis();
        log.debug("start processing msg [{}]", messageLogger().toString(msg));
        wip = (AdaptrisMessage) msg.clone();
        // Set the channel id and workflow id on the message lifecycle.
        wip.getMessageLifecycleEvent().setChannelId(obtainChannel().getUniqueId());
        wip.getMessageLifecycleEvent().setWorkflowId(obtainWorkflowId());
        wip.addEvent(getConsumer(), true);
        sc.doService(wip);
        doProduce(wip);
        // handle success callback here.
        // failure callback will be handled by the message-error-handler that's configured...
        ListenerCallbackHelper.handleSuccessCallback(wip);
        logSuccess(wip, start);
      }
      catch (ProduceException e) {
        wip.addEvent(getProducer(), false);
        handleBadMessage("Exception producing message", e, copyExceptionHeaders(wip, msg));
        handleProduceException();
      }
      catch (Exception e) {
        handleBadMessage("Exception processing message", e, copyExceptionHeaders(wip, msg));
      }
      finally {
        sendMessageLifecycleEvent(wip);
      }
      return wip;
    }
  }

}
