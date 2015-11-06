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

package com.adaptris.core.services;

import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.StartedState;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This service wrapper, will attempt to run the wrapped service and should the service fail with
 * a {@link ServiceException} we will attempt to retry the service according to your configuration.
 * </p>
 * <p>
 * You can control then retrying behaviour with the following two configurable options;
 *  <ul>
 *      <li>num-retries</li>
 *      <li>delay-between-retries</li>
 *  </ul>
 * </p>
 * <p>
 * A value of zero for num-retries will retry the wrapped service infinitely.  The default value if not set is 10.
 * </p>
 * <p>
 * The delay-between-retries is of type {@link TimeInterval}.  The default value if not set is 10 seconds.
 * </p>
 * <p>
 * You may also force the wrapped service to be restarted upon each retry by setting restart-on-failure to true;
 * </p>
 * 
 * @author Aaron McGrath
 * @license BASIC 
 *
 */
@XStreamAlias("retrying-service-wrapper")
public class RetryingServiceWrapper extends ServiceImp implements EventHandlerAware {

  private static final TimeInterval DEFAULT_DELAY = new TimeInterval(10l, TimeUnit.SECONDS);
  private static final int DEFAULT_NUM_RETRIES = 10;
  
  private Integer numRetries;
  @Valid
  private TimeInterval delayBetweenRetries;
  private Boolean restartOnFailure;
  @NotNull
  @Valid
  @AutoPopulated
  private Service service;
  private transient EventHandler eventHandler;
  
  public RetryingServiceWrapper() {
    super();
    setService(new NullService());
  }
  
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    int currentRetries = 0;
    int maxRetries = numRetries();
    // Also test the "started" state of this service, in case we are trying to shutdown the Adapter, we then need to break this loop. 
    while (((this.getNumRetries() == 0) || (currentRetries <= maxRetries))
        && (this.retrieveComponentState().equals(StartedState.getInstance()))) {
      try {
        this.getService().doService(msg);
        break;
      } catch(ServiceException ex) {
        currentRetries++;
        log.debug("Wrapped service: {} failed. Retrying (retry count: {})", this.getService().getClass().getSimpleName(),
            currentRetries);
        log.trace("Logging wrapped service exception for informational purposes only: ", ex);
        
        if(this.isRestartOnFailure()) {
          this.stopAndCloseQuietly();
          this.initAndStartQuietly();
        }
        
        if (!((maxRetries == 0) || (currentRetries <= maxRetries)))
            throw new ServiceException(ex);
        
        try {
          Thread.sleep(delayBetweenRetriesMs());
        } catch (InterruptedException iex) {
          log.debug("RetryingServiceWrapper(" + this.getUniqueId() + ") has been interrupted - exiting service.");
          break;
        }
      }
    }
  }

  @Override
  public void prepare() throws CoreException {
    getService().prepare();
  }

  private void stopAndCloseQuietly() {
    try {
      LifecycleHelper.stop(this.getService());
      LifecycleHelper.close(this.getService());
    } catch (Exception ex) {
      // do nothing
    }
  }
  
  private void initAndStartQuietly() {
    try {
      LifecycleHelper.registerEventHandler(this.getService(), eventHandler);
      LifecycleHelper.init(this.getService());
      LifecycleHelper.start(this.getService());
    } catch (Exception ex) {
      // do nothing here, because the service might not be able to start correctly, but we will
      // still try to use it, which will cause another failure and so the loop continues.
    }
  }
  
  @Override
  public void init() throws CoreException {
    LifecycleHelper.registerEventHandler(this.getService(), eventHandler);
    LifecycleHelper.init(this.getService());
  }

  @Override
  public void close() {
    LifecycleHelper.close(this.getService());
  }
  
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(this.getService());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(this.getService());
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = Args.notNull(service, "service");
  }

  public Integer getNumRetries() {
    return numRetries;
  }

  int numRetries() {
    return getNumRetries() != null ? getNumRetries().intValue() : DEFAULT_NUM_RETRIES;
  }

  public void setNumRetries(Integer numRetries) {
    this.numRetries = numRetries;
  }

  public TimeInterval getDelayBetweenRetries() {
    return delayBetweenRetries;
  }

  long delayBetweenRetriesMs() {
    return getDelayBetweenRetries() != null ? getDelayBetweenRetries().toMilliseconds() : DEFAULT_DELAY.toMilliseconds();
  }

  public void setDelayBetweenRetries(TimeInterval delayBetweenRetries) {
    this.delayBetweenRetries = delayBetweenRetries;
  }

  public boolean isRestartOnFailure() {
    return getRestartOnFailure() != null ? getRestartOnFailure().booleanValue() : false;
  }

  public void setRestartOnFailure(Boolean restartOnFailure) {
    this.restartOnFailure = restartOnFailure;
  }

  public Boolean getRestartOnFailure() {
    return restartOnFailure;
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

}
