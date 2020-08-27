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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.splitter.ServiceWorkerPool.Worker;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Extension to {@link AdvancedMessageSplitterService} that uses a underlying thread and object pool to execute the service on each
 * split message.
 * <p>
 * Note that using this splitter may mean that messages become un-ordered; if the order of the split messages is critical, then you
 * probably shouldn't use this service. Additionally, individual split-message failures will only be reported on after all the split
 * messages have been processed, so unlike {@link AdvancedMessageSplitterService}; {@link #setIgnoreSplitMessageFailures(Boolean)}
 * will not halt the processing of the subsequent split messages
 * </p>
 *
 * @config pooling-message-splitter-service
 */
@XStreamAlias("pooling-message-splitter-service")
@AdapterComponent
@ComponentProfile(summary = "Split a message and execute an arbitary number of services on the split message", tag = "service,splitter", since = "3.7.1")
@DisplayOrder(order =
{
    "splitter", "service", "maxThreads", "warmStart", "waitWhileBusy", "ignoreSplitMessageFailures",
    "sendEvents"
})
public class PoolingMessageSplitterService extends AdvancedMessageSplitterService {

  /**
   * Set the max number of threads to operate on split messages
   * <p>
   * The default is 10 if not explicitly specified
   * </p>
   */
  @InputFieldDefault(value = "10")
  @AdvancedConfig
  @Getter
  @Setter
  private Integer maxThreads;
  /**
   * Specify if the underlying object pool should be warmed up on {@link #start()}.
   * <p>
   * The default is false if not specified
   * </p>
   */
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean warmStart;
  /**
   * Actively check if the underlying object pool is ready to accept more workers.
   * <p>
   * If set to true, then we check that the underlying object pool has enough space for us to submit
   * more jobs. This means that if you have a large number of split messages, then we don't attempt
   * to flood the queue with thousands of messages causing possible issues within constrained
   * environments. It defaults to false if not explicitly specified, and if set to true will have a
   * negative impact on performance.
   * </p>
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean waitWhileBusy;

  private transient ExecutorService executor;
  private transient ServiceWorkerPool workerFactory;
  private transient GenericObjectPool<ServiceWorkerPool.Worker> objectPool;

  @Override
  protected void handleSplitMessage(AdaptrisMessage msg, Consumer<Exception> callback)
      throws ServiceException {
    if (waitWhileBusy()) {
      waitAndSubmit(msg, callback);
    } else {
      executor.submit(new ServiceExecutor(msg, callback));
    }
  }

  private void waitAndSubmit(AdaptrisMessage msg, Consumer<Exception> callback) {
    // Wait nicely
    Future<AdaptrisMessage> future = null;
    int max = maxThreads();
    do {
      if (objectPool.getNumActive() < max) {
        future = executor.submit(new ServiceExecutor(msg, callback));
      } else {
        LifecycleHelper.waitQuietly(100l);
      }
    } while (future == null);
  }


  @Override
  protected void initService() throws CoreException {
    workerFactory = new ServiceWorkerPool(getService(), eventHandler, maxThreads());
    objectPool = workerFactory.createCommonsObjectPool();
    executor = workerFactory.createExecutor(this.getClass().getSimpleName());
    super.initService();
  }

  @Override
  public void start() throws CoreException {
    if (warmStart()) {
      workerFactory.warmup(objectPool);
    }
    super.start();
  }

  @Override
  protected void closeService() {
    ManagedThreadFactory.shutdownQuietly(executor, new TimeInterval());
    Closer.closeQuietly(objectPool);
    super.closeService();
  }

  private int maxThreads() {
    return NumberUtils.toIntDefaultIfNull(getMaxThreads(), 10);
  }

  private boolean warmStart() {
    return BooleanUtils.toBooleanDefaultIfNull(getWarmStart(), false);
  }

  private boolean waitWhileBusy() {
    return BooleanUtils.toBooleanDefaultIfNull(getWaitWhileBusy(), false);
  }

  public PoolingMessageSplitterService withWarmStart(Boolean b) {
    setWarmStart(b);
    return this;
  }

  public PoolingMessageSplitterService withMaxThreads(Integer i) {
    setMaxThreads(i);
    return this;
  }

  public PoolingMessageSplitterService withWaitWhileBusy(Boolean b) {
    setWaitWhileBusy(b);
    return this;
  }

  private class ServiceExecutor implements Callable<AdaptrisMessage> {
    private AdaptrisMessage msg;
    private Consumer<Exception> callback;

    ServiceExecutor(AdaptrisMessage msg, Consumer<Exception> callback) {
      this.msg = msg;
      this.callback = callback;
    }

    @Override
    public AdaptrisMessage call() throws Exception {
      Worker w = objectPool.borrowObject();
      try {
        w.doService(msg);
        callback.accept(null);
      } catch (Exception e) {
        callback.accept(e);
      } finally {
        sendEvents(msg);
        objectPool.returnObject(w);
      }
      return msg;
    }
  }

}
