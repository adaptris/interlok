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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
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
 * {@link com.adaptris.core.Service} and subsequently joins all the messages back using the configured {@link MessageAggregator} implementation
 * <p>
 * <p>
 * For simplicity a new (cloned) instance of the underlying {@link com.adaptris.core.Service} is created for every split message, and executed in its
 * own thread; this means that where there is a high cost of initialisation for the service, then you may get better performance
 * aggregating the messages in a different way.
 * </p>
 * 
 * @config split-join-service
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("split-join-service")
@AdapterComponent
@ComponentProfile(
    summary = "Split a message and then execute the associated services on the split items, aggregating the split messages afterwards",
    tag = "service,splitjoin")
@DisplayOrder(order = {"splitter", "service", "aggregator", "timeout", "maxThreads"})
public class SplitJoinService extends ServiceImp implements EventHandlerAware {

  private static final String GENERIC_EXCEPTION_MSG = "Exception waiting for all services to complete";

  private static TimeInterval DEFAULT_TTL = new TimeInterval(600L, TimeUnit.SECONDS);
  private static transient ManagedThreadFactory myThreadFactory = new ManagedThreadFactory();
  private static final int DEFAULT_WAIT_INTERVAL = 100;

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
  @InputFieldDefault(value = "0")
  @AdvancedConfig
  private Integer maxThreads;

  private transient ExecutorService executors;
  private transient AdaptrisMarshaller marshaller = null;
  private transient EventHandler eventHandler;

  public SplitJoinService() {
    super();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    List<AdaptrisMessage> splitMessages = new ArrayList<>();
    AtomicLong marker = new AtomicLong(0);
    try (CloseableIterable<AdaptrisMessage> messages = CloseableIterable.FACTORY.ensureCloseable(getSplitter().splitMessage(msg))) {
      final ServiceExceptionHandler handler = new ServiceExceptionHandler();
      long count = 0;
      for (AdaptrisMessage splitMsg : messages) {
        count++;
        splitMessages.add(splitMsg);
        splitMsg.addMetadata(MessageSplitterServiceImp.KEY_CURRENT_SPLIT_MESSAGE_COUNT, Long.toString(count));
        ServiceExecutor exe = new ServiceExecutor(handler, marker, cloneService(service), splitMsg);
        executors.execute(exe);
      }
      msg.addMetadata(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT, Long.toString(count));
      if (!waitFor(marker, count)) {
        throw new ServiceException(GENERIC_EXCEPTION_MSG);
      }
      checkForExceptions(handler);
      if (count > 0) {
        joinMessage(msg, splitMessages);
      } else {
        log.trace("Split produced no msgs, nothing to do");
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private void checkForExceptions(ServiceExceptionHandler handler) throws ServiceException {
    Throwable e = handler.getFirstThrowableException();
    if (e != null) {
      log.error("One or more services failed; " + e.getMessage());
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private void joinMessage(AdaptrisMessage joined, List<AdaptrisMessage> split) throws ServiceException {
    try {
      getAggregator().joinMessage(joined, split);
    }
    catch (CoreException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private boolean waitFor(AtomicLong marker, long expected) throws InterruptedException {
    long waitTime = 0;
    while (waitTime < timeoutMs() && marker.get() != expected) {
      waitTime += DEFAULT_WAIT_INTERVAL;
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
    }
    if (marker.get() == expected) {
      return true;
    }
    return false;
  }

  @Override
  protected void initService() throws CoreException {
    if (getSplitter() == null) {
      throw new CoreException("Null MessageSplitter implementation");
    }
    if (getAggregator() == null) {
      throw new CoreException("Null MessageJoiner implementation");
    }
    if (getService() == null) {
      throw new CoreException("Null Service implementation");
    }
    executors = maxThreads() <= 0 ? Executors.newCachedThreadPool(myThreadFactory)
        : Executors.newFixedThreadPool(maxThreads(), myThreadFactory);
    marshaller = DefaultMarshaller.getDefaultMarshaller();
  }

  @Override
  protected void closeService() {
    executors.shutdown();
    try {
      if (!executors.awaitTermination(60, TimeUnit.SECONDS)) {
        executors.shutdownNow();
      }
    } catch (InterruptedException e) {
      log.warn("Failed to shutdown execution pool");
    }
  }

  @Override
  public void prepare() throws CoreException {
    if (getService() != null) {
      getService().prepare();
    }
  }

  private Service cloneService(Service original) throws ServiceException {
    Service result = null;
    try {
      result = (Service) marshaller.unmarshal(marshaller.marshal(original));
      LifecycleHelper.prepare(result);
    }
    catch (CoreException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    return result;
  }

  private class ServiceExecutor implements Runnable {
    private ServiceExceptionHandler handler;
    private AtomicLong counter;
    private Service service;
    private AdaptrisMessage msg;

    ServiceExecutor(ServiceExceptionHandler ceh, AtomicLong l, Service s, AdaptrisMessage msg) {
      handler = ceh;
      counter = l;
      service = s;
      this.msg = msg;
    }

    @Override
    public void run() {
      try {
        LifecycleHelper.registerEventHandler(service, eventHandler);
        LifecycleHelper.init(service);
        LifecycleHelper.start(service);
        service.doService(msg);
      }
      catch (Exception e) {
        handler.uncaughtException(Thread.currentThread(), e);
      }
      finally {
        LifecycleHelper.stop(service);
        LifecycleHelper.close(service);
      }
      counter.incrementAndGet();
    }
  }

  private class ServiceExceptionHandler implements Thread.UncaughtExceptionHandler {
    private List<Throwable> exceptionList = Collections.synchronizedList(new ArrayList<Throwable>());

    /**
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      log.error("uncaughtException from " + t.getName(), e);
      exceptionList.add(e);
    }

    public Throwable getFirstThrowableException() {
      Throwable result = null;
      if (exceptionList.size() > 0) {
        result = exceptionList.get(0);
      }
      return result;
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

  /**
   * @return the maxThreads
   */
  public Integer getMaxThreads() {
    return maxThreads;
  }

  /**
   * Set the maximum number of threads used to execute the split/join.
   * 
   * @param i the maxThreads to set, defaults to 0 which means no limit.
   */
  public void setMaxThreads(Integer i) {
    this.maxThreads = i;
  }

  int maxThreads() {
    return getMaxThreads() != null ? getMaxThreads().intValue() : 0;
  }

}
