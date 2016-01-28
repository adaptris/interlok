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

import java.io.IOException;
import java.io.InputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link com.adaptris.core.Service} which dynamically obtains and applies a {@link com.adaptris.core.Service} to an {@link com.adaptris.core.AdaptrisMessage} based on
 * the contents of the message.
 * 
 * <p>
 * This class will attempt to extract a marshalled service (roughly analagous to {@link DynamicServiceLocator}) from the payload of
 * the current message, unmarshal that service, and then execute that service against the current message. The use of this type of
 * service is discouraged from a supportability perspective; however there will be use cases where it is appropriate. No checks are
 * performed on the {@link com.adaptris.core.Service} that is unmarshalled other than license verification; any exceptions thrown by unmarshalled
 * service are simply rethrown back to the workflow for standard message error handling.
 * </p>
 * 
 * @config dynamic-service-executor
 * 
 * @author lchan
 * @see com.adaptris.core.ServiceExtractor
 */
@XStreamAlias("dynamic-service-executor")
@AdapterComponent
@ComponentProfile(summary = "Execute a service definition which is defined in the message itself", tag = "service,dynamic")
public class DynamicServiceExecutor extends ServiceImp implements EventHandlerAware {

  private transient EventHandler eventHandler;

  @NotNull
  @AutoPopulated
  @Valid
  private ServiceExtractor serviceExtractor;
  @Valid
  private AdaptrisMarshaller marshaller;

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
      log.trace("Created service [" + friendlyName(service) + "]");
      prepare(service);
      start(service);
      service.doService(msg);
      stop(service);
    }
    catch (IOException | CoreException e) {
      rethrowServiceException(e);
    }
  }

  private void prepare(Service service) throws CoreException {
    LifecycleHelper.registerEventHandler(service, eventHandler);
    service.prepare();
  }

  private static void start(AdaptrisComponent c) throws CoreException {
    LifecycleHelper.init(c);
    try {
      LifecycleHelper.start(c);
    }
    catch (CoreException e) {
      LifecycleHelper.close(c);
      throw e;
    }
  }

  private static void stop(AdaptrisComponent c) {
    LifecycleHelper.stop(c);
    LifecycleHelper.close(c);
  }

  private Service createService(AdaptrisMessage msg) throws CoreException, IOException {
    InputStream in = serviceExtractor.getInputStream(msg);
    Service result = null;
    try {
      result = (Service) currentMarshaller().unmarshal(in);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return result;
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
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
    if (s == null) {
      throw new IllegalArgumentException("Null ServiceExtractor");
    }
    this.serviceExtractor = s;
  }

  public AdaptrisMarshaller getMarshaller() {
    return marshaller;
  }

  /**
   * Set the marshaller to use to unmarshal the service.
   * 
   * @param m the marshaller, if not configured will default to {@link DefaultMarshaller#getDefaultMarshaller()}
   */
  public void setMarshaller(AdaptrisMarshaller m) {
    this.marshaller = m;
  }

  AdaptrisMarshaller currentMarshaller() throws CoreException {
    return getMarshaller() != null ? getMarshaller() : DefaultMarshaller.getDefaultMarshaller();
  }

}
