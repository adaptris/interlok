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

import static com.adaptris.core.util.ServiceUtil.discardNulls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceWrapper;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of the Splitter and Aggregator enterprise integration pattern.
 * 
 * <p>
 * This service splits a message according to the configured {@link MessageSplitter} implementation, executes the configured
 * {@link com.adaptris.core.Service} and subsequently joins all the messages back using the configured {@link MessageAggregator}
 * implementation
 * <p>
 * <p>
 * A new (cloned) instance of the underlying {@link com.adaptris.core.Service} is created for every split message, and executed in
 * its own thread; this means that where there is a high cost of initialisation for the service, then you may get better performance
 * aggregating the messages in a different way.
 * </p>
 * 
 * @config split-join-service
 */
@XStreamAlias("split-join-service")
@AdapterComponent
@ComponentProfile(summary = "Split a message and then execute the associated services on the split items, aggregating the split messages afterwards", tag = "service,splitjoin")
@DisplayOrder(order =
{
    "splitter", "service", "aggregator", "maxThreads", "timeout"
})
public class SplitJoinService extends ServiceImp implements EventHandlerAware, ServiceWrapper {

  private static TimeInterval DEFAULT_TTL = new TimeInterval(600L, TimeUnit.SECONDS);

  @NotNull
  @Valid
  private Service service;
  @NotNull
  @Valid
  private MessageSplitter splitter;
  @NotNull
  @Valid
  private MessageAggregator aggregator;
  @Valid
  private TimeInterval timeout;

  private transient ExecutorService executors;
  private transient EventHandler eventHandler;

  @AdvancedConfig
  @Valid
  private PoolingFutureExceptionStrategy exceptionStrategy;

  public SplitJoinService() {
    super();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      List<AdaptrisMessage> splitMessages = toList(getSplitter().splitMessage(msg));
      if (splitMessages.isEmpty()) {
        log.debug("No output from splitter; nothing to do");
        return;
      }
      ServiceExceptionHandler handler = new ServiceExceptionHandler();
      Collection<Callable<AdaptrisMessage>> jobs = buildTasks(handler, splitMessages);
      submitAndWait(handler, jobs);
      msg.addMetadata(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT, Long.toString(jobs.size()));
      getAggregator().joinMessage(msg, splitMessages);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected List<Callable<AdaptrisMessage>> buildTasks(ServiceExceptionHandler handler, List<AdaptrisMessage> msgs)
      throws Exception {
    int count = 0;
    List<Callable<AdaptrisMessage>> result = new ArrayList<>();
    for (AdaptrisMessage splitMsg : msgs) {
      count++;
      splitMsg.addMetadata(MessageSplitterServiceImp.KEY_CURRENT_SPLIT_MESSAGE_COUNT, Long.toString(count));
      Callable<AdaptrisMessage> job = new MyServiceExecutor(handler, cloneService(service), splitMsg);
      result.add(job);
    }
    return result;
  }

  private List<Future<AdaptrisMessage>> submitAndWait(ServiceExceptionHandler handler, Collection<Callable<AdaptrisMessage>> jobs)
      throws Exception {
    List<Future<AdaptrisMessage>> results = executors.invokeAll(jobs, timeoutMs(), TimeUnit.MILLISECONDS);
    exceptionStrategy.handle(handler, results);
    log.trace("Finished waiting for operations...");
    return results;
  }

  private List<AdaptrisMessage> toList(Iterable<AdaptrisMessage> iter) throws IOException, CoreException {
    if (iter instanceof List) {
      return (List<AdaptrisMessage>) iter;
    }
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try (com.adaptris.core.util.CloseableIterable<AdaptrisMessage> messages = com.adaptris.core.util.CloseableIterable
        .ensureCloseable(iter)) {
      for (AdaptrisMessage msg : messages) {
        result.add(msg);
      }
    }
    return result;
  }

  protected ExecutorService createExecutor() {
    return Executors.newCachedThreadPool(new ManagedThreadFactory(this.getClass().getSimpleName()));
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getSplitter(), "splitter");
      Args.notNull(getAggregator(), "aggregator");
      Args.notNull(getService(), "service");
      if (exceptionStrategy == null)
        exceptionStrategy = new DefaultPoolingFutureExceptionStrategy();
      executors = createExecutor();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {
    ManagedThreadFactory.shutdownQuietly(executors, new TimeInterval());
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getService());
  }

  private Service cloneService(Service original) throws CoreException {
    Service result = DefaultMarshaller.roundTrip(original);
    LifecycleHelper.registerEventHandler(result, eventHandler);
    return result;
  }

  private class MyServiceExecutor implements Callable<AdaptrisMessage> {
    private ServiceExceptionHandler handler;
    private Service service;
    private AdaptrisMessage msg;

    MyServiceExecutor(ServiceExceptionHandler ceh, Service s, AdaptrisMessage msg) {
      handler = ceh;
      service = s;
      this.msg = msg;
    }

    @Override
    public AdaptrisMessage call() throws Exception {
      try {
        LifecycleHelper.registerEventHandler(service, eventHandler);
        LifecycleHelper.initAndStart(service);
        service.doService(msg);
      } catch (Exception e) {
        handler.uncaughtException(Thread.currentThread(), e);
      } finally {
        LifecycleHelper.stopAndClose(service);
      }
      return msg;
    }

  }

  /**
   * @return the timeToLive
   */
  public TimeInterval getTimeout() {
    return timeout;
  }

  /**
   * Set the maximum amount of time to wait for all the instances of services to complete.
   * <p>
   * If the time to live is exceeded then an exception will be thrown by the service
   * </p>
   * 
   * @param ttl the timeout to set, default is 10 minutes
   */
  public void setTimeout(TimeInterval ttl) {
    this.timeout = ttl;
  }

  long timeoutMs() {
    return getTimeout() != null ? getTimeout().toMilliseconds() : DEFAULT_TTL.toMilliseconds();
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  /**
   * @return the service
   */
  public Service getService() {
    return service;
  }

  /**
   * The {@link com.adaptris.core.Service} to execute over all the split messages.
   * 
   * @param s the service to set
   */
  public void setService(Service s) {
    this.service = Args.notNull(s, "service");
  }

  /**
   * @return the messageSplitter
   */
  public MessageSplitter getSplitter() {
    return splitter;
  }

  /**
   * The {@link MessageSplitter} implementation to use to split the incoming message.
   * 
   * @param ms the messageSplitter to set
   */
  public void setSplitter(MessageSplitter ms) {
    this.splitter = Args.notNull(ms, "splitter");
  }

  /**
   * @return the messageJoiner
   */
  public MessageAggregator getAggregator() {
    return aggregator;
  }

  /**
   * The {@link MessageAggregator} implementation to use to join messages together.
   * 
   * @param mj the messageJoiner to set
   */
  public void setAggregator(MessageAggregator mj) {
    this.aggregator = Args.notNull(mj, "aggregator");
  }

  @Override
  public Service[] wrappedServices() {
    return discardNulls(getService());
  }

  /**
   * The {@link PoolingFutureExceptionStrategy} implementation to use to handle task exceptions.
   * @param exceptionStrategy
   */
  public void setExceptionStrategy(PoolingFutureExceptionStrategy exceptionStrategy) {
    this.exceptionStrategy = exceptionStrategy;
  }
}
