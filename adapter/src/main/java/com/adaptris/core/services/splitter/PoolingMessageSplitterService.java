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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extension to {@link AdvancedMessageSplitterService} that uses a underlying thread pool to execute the service list on each split
 * messages.
 * <p>
 * Note that using this splitter may mean that messages become un-ordered; if the order of the split messages is critical, then you
 * probably shouldn't use this service. Additionally, individual split-message failures will only be reported on after all the split
 * messages have been processed, so {@link #setIgnoreSplitMessageFailures(Boolean)} will not stop the processing of the subsequent
 * split messages
 * </p>
 * <p>
 * Like {@link SplitJoinService} new instance of the underlying {@link com.adaptris.core.Service} is created for every split
 * message, and executed by the thread pool; this means that where there is a high cost of initialisation for the service, then you
 * may get better performance aggregating the messages in a different way.
 * </p>
 * 
 * @config pooling-message-splitter-service
 * 
 * 
 */
@XStreamAlias("pooling-message-splitter-service")
@AdapterComponent
@ComponentProfile(summary = "Split a message and execute an arbitary number of services on the split message", tag = "service,splitter", since = "3.7.1")
@DisplayOrder(order =
{
    "splitter", "service", "maxThreads", "ignoreSplitMessageFailures", "sendEvents"
})
public class PoolingMessageSplitterService extends AdvancedMessageSplitterService {

  @InputFieldDefault(value = "10")
  private Integer maxThreads;

  private transient ExecutorService executor;
  private transient ServiceExceptionHandler exceptionHandler;

  @Override
  public Future<?> handleSplitMessage(AdaptrisMessage msg) throws ServiceException {
    try {
      exceptionHandler.clearExceptions();
      return executor.submit(new ServiceExecutor(exceptionHandler, cloneService(getService()), msg));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected void initService() throws CoreException {
    executor = Executors.newFixedThreadPool(maxThreads(), new ManagedThreadFactory(this.getClass().getSimpleName()));
    exceptionHandler = new ServiceExceptionHandler();
    super.initService();
  }

  protected void closeService() {
    ManagedThreadFactory.shutdownQuietly(executor, new TimeInterval());
    super.closeService();
  }

  protected void waitForCompletion(List<Future> tasks) throws ServiceException {
    super.waitForCompletion(tasks);
    exceptionHandler.throwFirstException();
  }

  public Integer getMaxThreads() {
    return maxThreads;
  }

  public void setMaxThreads(Integer maxThreads) {
    this.maxThreads = maxThreads;
  }

  int maxThreads() {
    return getMaxThreads() != null ? getMaxThreads().intValue() : 10;
  }

  private Service cloneService(Service original) throws Exception {
    Service result = DefaultMarshaller.roundTrip(original);
    LifecycleHelper.registerEventHandler(result, eventHandler);
    return result;
  }

  private class ServiceExecutor implements Callable<AdaptrisMessage> {
    private ServiceExceptionHandler handler;
    private Service service;
    private AdaptrisMessage msg;

    ServiceExecutor(ServiceExceptionHandler ceh, Service s, AdaptrisMessage msg) {
      handler = ceh;
      service = s;
      this.msg = msg;
    }

    @Override
    public AdaptrisMessage call() throws Exception {
      try {
        LifecycleHelper.initAndStart(service);
        executeService(service, msg);
      } catch (Exception e) {
        handler.uncaughtException(Thread.currentThread(), e);
      } finally {
        LifecycleHelper.stopAndClose(service);
      }
      return msg;
    }
  }
}
