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

import static com.adaptris.core.util.ServiceUtil.discardNulls;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceWrapper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that wraps other services allowing them to be branching.
 * <p>
 * This is somewhat analogous to {@link com.adaptris.core.services.exception.ExceptionHandlingServiceWrapper} but fits a more
 * generalised use case. In the event of an Exception; {@link #getFailureId()} is always set as the
 * {@link AdaptrisMessage#setNextServiceId(String)}. In the event of a successfull execution then if the wrapped service is already
 * branching then {@link AdaptrisMessage#setNextServiceId(String)} is untouched; otherwise {@link #getSuccessId()} is used.
 * </p>
 * 
 * @config branching-service-enabler
 * @since 3.4.1
 */
@XStreamAlias("branching-service-enabler")
@AdapterComponent
@ComponentProfile(summary = "Wraps another service, performing a branch based on its success", tag = "service, branching",
    branchSelector = true)
@DisplayOrder(order =
{
    "service", "successId", "failureId"
})
public class BranchingServiceEnabler extends BranchingServiceImp implements EventHandlerAware, ServiceWrapper {

  @NotNull
  @Valid
  private Service service = null;
  @NotNull
  private String successId = null;
  @NotNull
  private String failureId = null;

  private transient EventHandler eventHandler;
  
  public BranchingServiceEnabler() {
  }

  public BranchingServiceEnabler(Service s) {
    this();
    setService(s);
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getService(), "service");
      Args.notNull(getSuccessId(), "successId");
      Args.notNull(getFailureId(), "failureId");
      LifecycleHelper.registerEventHandler(getService(), eventHandler);
      LifecycleHelper.init(getService());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getService());
  }
  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getService());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getService());
  }


  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getService());
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      service.doService(msg);
      if (!service.isBranching()) {
        msg.setNextServiceId(getSuccessId());
      }
    }
    catch (ServiceException e) {
      msg.setNextServiceId(getFailureId());
    }
  }

  /**
   * @return the wrappedService
   */
  public Service getService() {
    return service;
  }

  /**
   * @param s the wrappedService to set
   */
  public void setService(Service s) {
    service = s;
  }

  /**
   * @return the failId
   */
  public String getFailureId() {
    return failureId;
  }

  /**
   * @param s the failId to set
   */
  public void setFailureId(String s) {
    failureId = Args.notNull(s, "failureId");
  }

  /**
   * @return the successId
   */
  public String getSuccessId() {
    return successId;
  }

  /**
   * @param s the successId to set
   */
  public void setSuccessId(String s) {
    successId = Args.notNull(s, "successId");
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  @Override
  public Service[] wrappedServices() {
    return discardNulls(getService());
  }
}
