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

package com.adaptris.core.services.exception;

import static com.adaptris.core.util.ServiceUtil.discardNulls;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceWrapper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Wraps services so that errors are intercepted and processed by a different service.
 * <p>
 * This Service wraps a {@link com.adaptris.core.Service}; if this wrapped Service throws a ServiceException the configured exceptionHandlingService
 * is applied. Normal message error handling is not triggered unless an exception is thrown from the service handling the exception.
 * </p>
 * <p>
 * This Service is intended for use where an exception processing a message should generate a synchronous reply e.g. when processing
 * web service requests. It could be used where detailed information about the error is required to be sent as part of the reply.
 * This can be acheived by configuring services as part of the exceptionHandlingService.
 * </p>
 * <p>
 * Note that the exception handling service will process the message <b>IN THE STATE AT WHICH IT FAILED</b> (i.e. the payload of the
 * message may have been modified). It might not be possible to recover any meaningful data from this if say, an exception is
 * triggered after encrypting the data with a remote party's public key. If you wish to use process the original message then you
 * should configure a {@link com.adaptris.core.StandardProcessingExceptionHandler} at the workflow level.
 * {@link com.adaptris.core.StandardProcessingExceptionHandler} allows you to configure arbitrary services to perform operations on
 * messages that have failed.
 * </p>
 * 
 * @config exception-handling-service-wrapper
 * 
 * 
 */
@XStreamAlias("exception-handling-service-wrapper")
@AdapterComponent
@ComponentProfile(summary = "Handle exceptions from one service via a separate service", tag = "service,error-handling")
@DisplayOrder(order = {"exceptionMessageMetadataKey"})
public class ExceptionHandlingServiceWrapper extends ServiceImp implements EventHandlerAware, ServiceWrapper {

  public static final String DEFAULT_EXCEPTION_MESSAGE_METADATA_KEY = "adp.exception.wrapper.message";

  @NotNull
  @AutoPopulated
  @Valid
  private Service service;
  @NotNull
  @AutoPopulated
  @Valid
  private Service exceptionHandlingService;
  @NotBlank
  @AutoPopulated
  private String exceptionMessageMetadataKey;
  private transient EventHandler eventHandler;

  /**
   * Creates a new instance.
   */
  public ExceptionHandlingServiceWrapper() {
    setExceptionMessageMetadataKey(DEFAULT_EXCEPTION_MESSAGE_METADATA_KEY);
    setExceptionHandlingService(new NullService());
    setService(new NullService());
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    this.eventHandler = eh;
  }

  /** @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      service.doService(msg);
    }
    catch (ServiceException e) {
      log.warn("exception has occurred, applying exceptionService");
      log.trace("exception details", e);
      msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, e);
      msg.addMetadata(getExceptionMessageMetadataKey(), StringUtils.defaultIfBlank(e.getMessage(), ""));
      exceptionHandlingService.doService(msg);
    }
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.registerEventHandler(exceptionHandlingService, eventHandler);
    LifecycleHelper.registerEventHandler(service, eventHandler);
    LifecycleHelper.init(exceptionHandlingService);
    LifecycleHelper.init(service);
    
    this.getChildComponents().add(exceptionHandlingService);
    this.getChildComponents().add(service);
    
    exceptionHandlingService.setParentComponent(this);
    service.setParentComponent(this);
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(service);
    LifecycleHelper.close(exceptionHandlingService);
  }


  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(exceptionHandlingService);
    LifecycleHelper.start(service);
  }

  /** @see com.adaptris.core.ServiceImp#stop() */
  @Override
  public void stop() {
    LifecycleHelper.stop(exceptionHandlingService);
    LifecycleHelper.stop(service);
    super.stop();
  }

  // properties

  /**
   * Returns the Service to wrap.
   *
   * @return the Service to wrap
   */
  public Service getService() {
    return service;
  }

  /**
   * Sets the Service to wrap.
   *
   * @param s the Service to wrap
   */
  public void setService(Service s) {
    service = Args.notNull(s, "service");
  }

  /**
   * Returns the Service to call if an exception is encountered calling the wrapped Service.
   *
   * @return the Service to call if an exception is encountered calling the wrapped Service
   */
  public Service getExceptionHandlingService() {
    return exceptionHandlingService;
  }

  /**
   * Sets the Service to call if an exception is encountered calling the wrapped Service.
   *
   * @param s the Service to call if an exception is encountered calling the wrapped Service
   */
  public void setExceptionHandlingService(Service s) {
    exceptionHandlingService = Args.notNull(s, "exceptionHandlingService");
  }

  /**
   * Returns the metadata key to store the exception message against. Default is <code>adp.exception.wrapper.message</code>.
   *
   * @return the metadata key to store the exception message against
   */
  public String getExceptionMessageMetadataKey() {
    return exceptionMessageMetadataKey;
  }

  /**
   * Sets the metadata key to store the exception message against.
   *
   * @param s the metadata key to store the exception message against
   */
  public void setExceptionMessageMetadataKey(String s) {
    exceptionMessageMetadataKey = Args.notBlank(s, "exceptionMessageMetadataKey");
  }

  @Override
  public void prepare() throws CoreException {
    getExceptionHandlingService().prepare();
    getService().prepare();
  }

  @Override
  public Service[] wrappedServices() {
    return discardNulls(getService(), getExceptionHandlingService());
  }
}
