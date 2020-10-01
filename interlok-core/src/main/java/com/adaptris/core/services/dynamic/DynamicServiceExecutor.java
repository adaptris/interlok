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

package com.adaptris.core.services.dynamic;

import static com.adaptris.core.util.LoggingHelper.friendlyName;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
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
import com.adaptris.core.ServiceList;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link com.adaptris.core.Service} which dynamically obtains and applies a
 * {@link com.adaptris.core.Service} to an {@link com.adaptris.core.AdaptrisMessage} based on the
 * contents of the message.
 *
 * <p>
 * This class will attempt to extract a marshalled service from the specified location (which might
 * be the current message), unmarshal that service, and then execute that service against the
 * current message. The use of this type of service is discouraged from a supportability
 * perspective; however there will be use cases where it is appropriate. No checks are performed on
 * the {@link com.adaptris.core.Service} that is unmarshalled; any exceptions thrown by unmarshalled
 * service are simply rethrown back to the workflow for standard message error handling.
 * </p>
 *
 * @config dynamic-service-executor
 *
 * @see ServiceExtractor
 */
@XStreamAlias("dynamic-service-executor")
@AdapterComponent
@ComponentProfile(summary = "Lookup and execute a dynamic service", tag = "service,dynamic")
@DisplayOrder(order = {"serviceExtractor", "marshaller", "treatNotFoundAsError"})
public class DynamicServiceExecutor extends ServiceImp implements EventHandlerAware {

  private transient EventHandler eventHandler;

  @NotNull
  @AutoPopulated
  @Valid
  private ServiceExtractor serviceExtractor;
  @Valid
  @AdvancedConfig
  private AdaptrisMarshaller marshaller;

  @InputFieldDefault(value = "true")
  private Boolean treatNotFoundAsError;

  public DynamicServiceExecutor() {
    this(new DefaultServiceExtractor());
  }

  public DynamicServiceExecutor(ServiceExtractor se) {
    setServiceExtractor(se);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Service service = createService(msg);
      log.trace("Created service [{}]", friendlyName(service));
      LifecycleHelper.registerEventHandler(service, eventHandler);
      LifecycleHelper.initAndStart(service, false);
      service.doService(msg);
      LifecycleHelper.stopAndClose(service, false);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private Service createService(AdaptrisMessage msg) throws Exception {
    try {
      return getServiceExtractor().getService(msg, currentMarshaller());
    } catch (Exception e) {
      return onException(e);
    }
  }

  private Service onException(Exception e) throws Exception {
    if (treatNotFoundAsError()) {
      throw e;
    }
    log.trace("Encountered [{}] attempting to create dynamic-service; using empty service-list",
        e.getMessage());
    return new ServiceList();
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getServiceExtractor());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getServiceExtractor());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getServiceExtractor());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getServiceExtractor());
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getServiceExtractor());
  }


  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  public ServiceExtractor getServiceExtractor() {
    return serviceExtractor;
  }

  /**
   * Set the {@link ServiceExtractor} implementation used to extract the service from the message.
   *
   * @param s the service extractor implementation, the default is {@link DefaultServiceExtractor}
   */
  public void setServiceExtractor(ServiceExtractor s) {
    serviceExtractor = Args.notNull(s, "serviceExtractor");

  }

  public AdaptrisMarshaller getMarshaller() {
    return marshaller;
  }

  /**
   * Set the marshaller to use to unmarshal the service.
   *
   * @param m the marshaller, if not configured will default to
   *        {@link DefaultMarshaller#getDefaultMarshaller()}
   */
  public void setMarshaller(AdaptrisMarshaller m) {
    marshaller = m;
  }

  private AdaptrisMarshaller currentMarshaller() {
    return DefaultMarshaller.defaultIfNull(getMarshaller());
  }

  public Boolean getTreatNotFoundAsError() {
    return treatNotFoundAsError;
  }

  /**
   * Specify whether a failure to find a dynamic service is treated as an exception.
   *
   * @param b true to treat failures to unmarshal / find the service as an exception; default is
   *        true if not specified
   */
  public void setTreatNotFoundAsError(Boolean b) {
    treatNotFoundAsError = b;
  }

  private boolean treatNotFoundAsError() {
    return BooleanUtils.toBooleanDefaultIfNull(getTreatNotFoundAsError(), true);
  }
}
