/*
 * Copyright 2015 Adaptris Ltd.
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.services.splitter.ServiceWorkerPool.Worker;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Variant of {@link SplitJoinService} that uses a underlying thread and object pool to execute the service on each split message.
 * 
 * <p>
 * This service splits a message according to the configured {@link MessageSplitter} implementation, executes the configured
 * {@link com.adaptris.core.Service} and subsequently joins all the messages back using the configured {@link MessageAggregator}
 * implementation
 * <p>
 * <p>
 * This differs from {@link SplitJoinService} in that a pool of {@link com.adaptris.core.Service} instances is maintained and
 * re-used for each message; so the high cost of initialisation for the service, is not incurred (more than the max number of
 * threads specified) as much.
 * </p>
 * 
 * @config pooling-split-join-service
 * 
 */
@XStreamAlias("pooling-split-join-service")
@AdapterComponent
@ComponentProfile(summary = "Split a message and then execute the associated services on the split items, aggregating the split messages afterwards", tag = "service,splitjoin", since = "3.7.1")
@DisplayOrder(order =
{
    "splitter", "service", "aggregator", "maxThreads", "timeout", "warmStart"
})
public class PoolingSplitJoinService extends SplitJoinService {

  private static final int DEFAULT_THREADS = 10;

  @AdvancedConfig
  @InputFieldDefault(value = "10")
  private Integer maxThreads;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean warmStart;


  private transient ServiceWorkerPool workerFactory;
  private transient GenericObjectPool<ServiceWorkerPool.Worker> objectPool;

  public PoolingSplitJoinService() {
    super();
  }

  @Override
  public void prepare() throws CoreException {
    super.prepare();
    workerFactory = new ServiceWorkerPool(getService(), null, maxThreads());
  }

  @Override
  public void initService() throws CoreException {
    objectPool = workerFactory.createObjectPool();
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
  public void closeService() {
    super.closeService();
    ServiceWorkerPool.closeQuietly(objectPool);
  }

  @Override
  protected List<Callable<AdaptrisMessage>> buildTasks(ServiceExceptionHandler handler, List<AdaptrisMessage> msgs)
      throws Exception {
    int count = 0;
    List<Callable<AdaptrisMessage>> result = new ArrayList<>();
    for (AdaptrisMessage splitMsg : msgs) {
      count++;
      splitMsg.addMetadata(MessageSplitterServiceImp.KEY_CURRENT_SPLIT_MESSAGE_COUNT, Long.toString(count));
      Callable<AdaptrisMessage> job = new MyServiceExecutor(handler, splitMsg);
      result.add(job);
    }
    return result;
  }

  @Override
  protected ExecutorService createExecutor() {
    return workerFactory.createExecutor(this.getClass().getSimpleName());
  }

  public Integer getMaxThreads() {
    return maxThreads;
  }

  /**
   * Set the max number of threads to handle the execution of the split messages.
   * 
   * @param size the max number of threads, defaults to 10 if not specified.
   */
  public void setMaxThreads(Integer size) {
    this.maxThreads = size;
  }

  public PoolingSplitJoinService withMaxThreads(Integer max) {
    setMaxThreads(max);
    return this;
  }

  int maxThreads() {
    return getMaxThreads() != null ? getMaxThreads().intValue() : DEFAULT_THREADS;
  }

  boolean warmStart() {
    return BooleanUtils.toBooleanDefaultIfNull(getWarmStart(), false);
  }

  public Boolean getWarmStart() {
    return warmStart;
  }

  /**
   * Specify if the underlying object pool should be warmed up on {@link #start()}.
   * 
   * @param b true or false (default false if not specified).
   */
  public void setWarmStart(Boolean b) {
    this.warmStart = b;
  }

  public PoolingSplitJoinService withWarmStart(Boolean b) {
    setWarmStart(b);
    return this;
  }

  private class MyServiceExecutor implements Callable<AdaptrisMessage> {

    private ServiceExceptionHandler handler;
    private AdaptrisMessage msg;

    MyServiceExecutor(ServiceExceptionHandler ceh, AdaptrisMessage msg) {
      handler = ceh;
      this.msg = msg;
    }

    @Override
    public AdaptrisMessage call() throws Exception {
      Worker w = objectPool.borrowObject();
      try {
        w.doService(msg);
      } catch (Exception e) {
        handler.uncaughtException(Thread.currentThread(), e);
      } finally {
        objectPool.returnObject(w);
      }
      return msg;
    }

  }

}
