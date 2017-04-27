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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
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
@DisplayOrder(order = {"splitter", "service", "aggregator", "timeout"})
public class SplitJoinService extends ServiceImp implements EventHandlerAware {

  private static final String GENERIC_EXCEPTION_MSG = "Exception waiting for all services to complete";

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
  private transient AdaptrisMarshaller marshaller = null;
  private transient EventHandler eventHandler;

  public SplitJoinService() {
    super();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    List<AdaptrisMessage> splitMessages = splitMessage(msg);
    if (splitMessages.isEmpty()) {
      log.debug("No output from splitter; nothing to do");
      return;
    }
    final CyclicBarrier gate = new CyclicBarrier(splitMessages.size() + 1);
    final ServiceExceptionHandler handler = new ServiceExceptionHandler();
    long count = 0;
    for (AdaptrisMessage splitMsg : splitMessages) {
      count++;
      splitMsg.addMetadata(MessageSplitterServiceImp.KEY_CURRENT_SPLIT_MESSAGE_COUNT, Long.toString(count));
      ServiceExecutor exe = new ServiceExecutor(handler, gate, cloneService(service), splitMsg);
      executors.execute(exe);
    }
    msg.addMetadata(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT, Long.toString(count));
    waitFor(gate, handler);
    log.trace("Finished waiting for operations ");
    checkForExceptions(handler);
    joinMessage(msg, splitMessages);
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

  private List<AdaptrisMessage> splitMessage(AdaptrisMessage m) throws ServiceException {
    List<AdaptrisMessage> msgs = new ArrayList<AdaptrisMessage>();
    try {
      msgs = toList(getSplitter().splitMessage(m));
    }
    catch (CoreException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    return msgs;
  }

  /**
   * Convert the Iterable into a List. If it's already a list, just return it. If not, 
   * it will be iterated and the resulting list returned.
   */
  private List<AdaptrisMessage> toList(Iterable<AdaptrisMessage> iter) {
    if(iter instanceof List) {
      return (List<AdaptrisMessage>)iter;
    }
    
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    
    try(CloseableIterable<AdaptrisMessage> messages = CloseableIterable.FACTORY.ensureCloseable(iter)) {
      for(AdaptrisMessage msg: messages) {
        result.add(msg);
      }
    } catch (IOException e) {
      log.warn("Could not close Iterable!", e);
    }
    
    return result;
  }

  private void waitFor(CyclicBarrier gate, ServiceExceptionHandler handler) {
    try {
      gate.await(timeoutMs(), TimeUnit.MILLISECONDS);
    }
    catch (Exception gateException) {
      handler.uncaughtException(Thread.currentThread(), new CoreException(GENERIC_EXCEPTION_MSG, gateException));
    }
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
    executors = Executors.newCachedThreadPool();
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
      result.prepare();
    }
    catch (CoreException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    return result;
  }

  private class ServiceExecutor implements Runnable {
    private ServiceExceptionHandler handler;
    private CyclicBarrier gate;
    private Service service;
    private AdaptrisMessage msg;

    ServiceExecutor(ServiceExceptionHandler ceh, CyclicBarrier cb, Service s, AdaptrisMessage msg) {
      handler = ceh;
      gate = cb;
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
      waitFor(gate, handler);
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

}
