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
import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollectionImp;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceWrapper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Stateless service wrapper, wraps any configured service and allows you to set a strategy on when to restart the service before
 * allowing it to execute.
 * </p>
 * <p>
 * Consider using this wrapper on a service where you will periodically want to stop and re-start a service. Check
 * {@link RestartStrategy} for the available restarting strategies. Note that some nested configuration will be ignored,
 * particularly {@link #setContinueOnFail(Boolean)} in the nested service. If you want that behaviour, then you should explicitly
 * configure it in this service.
 * 
 * </p>
 * 
 * @author lchan
 * @config stateless-service-wrapper
 * 
 */
@XStreamAlias("stateless-service-wrapper")
@AdapterComponent
@ComponentProfile(summary = "Wraps another service, with a strategy for restarting the service periodically", tag = "service")
@DisplayOrder(order = {"service", "restartStrategy"})
public class StatelessServiceWrapper extends ServiceImp implements EventHandlerAware, ServiceWrapper {

  private enum MessageEventGeneratorProxy {
    ServiceProxy {
      @Override
      boolean matches(Service s) {
        return s != null && s instanceof ServiceImp;
      }
      @Override
      void setIsTrackingEndpoint(Service s, Boolean b) {
        ((ServiceImp) s).setIsTrackingEndpoint(b);
      }
      @Override
      Boolean getIsTrackingEndpoint(Service s) {
        return ((ServiceImp) s).getIsTrackingEndpoint();
      }
    },
    ServiceCollectionProxy {
      @Override
      boolean matches(Service s) {
        return s != null && s instanceof ServiceCollectionImp;
      }
      @Override
      void setIsTrackingEndpoint(Service s, Boolean b) {
        ((ServiceCollectionImp) s).setIsTrackingEndpoint(b);
      }
      @Override
      Boolean getIsTrackingEndpoint(Service s) {
        return ((ServiceCollectionImp) s).getIsTrackingEndpoint();
      }
    };
    abstract boolean matches(Service s);
    abstract void setIsTrackingEndpoint(Service s, Boolean b);
    abstract Boolean getIsTrackingEndpoint(Service s);
  }
  
  @NotNull
  @Valid
  private Service service = null;

  @NotNull
  @Valid
  @AutoPopulated
  private RestartStrategy restartStrategy;
  private transient EventHandler eventHandler;
  
  public StatelessServiceWrapper() {
    this.setRestartStrategy(new AlwaysRestartStrategy());
  }

  public StatelessServiceWrapper(Service s) {
    this();
    setService(s);
  }

  @Override
  public void stop() {
    stopService();
  }

  /** @see com.adaptris.core.AdaptrisComponent */
  @Override
  public void start() throws CoreException {
    startService();
  }

  @Override
  public String createName() {
    if (getService() != null) {
      return getService().createName();
    }
    return super.createName();
  }

  @Override
  public String createQualifier() {
    String qualifier = null;
    if (getService() != null) {
      qualifier = getService().createQualifier();
    }
    return isEmpty(qualifier) ? super.createQualifier() : qualifier;
  }

  @Override
  public boolean isTrackingEndpoint() {
    if (getService() != null) {
      return getService().isTrackingEndpoint();
    }
    return super.isTrackingEndpoint();
  }

  @Override
  public boolean isBranching() {
    if (getService() != null) {
      return getService().isBranching();
    }
    return super.isBranching();
  }

  @Override
  protected void initService() throws CoreException {
    if (getService() == null) {
      throw new CoreException("No wrapped service");
    }
  }

  @Override
  protected void closeService() {}


  @Override
  public void prepare() throws CoreException {
    if (getService() != null) {
      getService().prepare();
    }
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if(restartStrategy.requiresRestart()) {
      log.debug("Restarting our service due to " + restartStrategy.getClass().getSimpleName());
      stopService();
      startService();
    }
    service.doService(msg);
    restartStrategy.messageProcessed(msg);
  }

  private void startService() throws ServiceException {
    try {
      LifecycleHelper.registerEventHandler(getService(), eventHandler);
      LifecycleHelper.init(getService());
      LifecycleHelper.start(getService());
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
  }

  private void stopService() {
    LifecycleHelper.stop(getService());
    LifecycleHelper.close(getService());
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

  @Override
  public Boolean getIsTrackingEndpoint() {
    try {
      MessageEventGeneratorProxy what = getProxy(getService());
      return what.getIsTrackingEndpoint(getService());
    }
    catch (IllegalArgumentException e) {
      return super.getIsTrackingEndpoint();
    }
  }

  @Override
  public void setIsTrackingEndpoint(Boolean b) {
    try {
      MessageEventGeneratorProxy what = getProxy(getService());
      what.setIsTrackingEndpoint(getService(), b);
    }
    catch (IllegalArgumentException e) {
      super.setIsTrackingEndpoint(b);
    }
  }

  private static MessageEventGeneratorProxy getProxy(Service s) {
    MessageEventGeneratorProxy match = null;
    for (MessageEventGeneratorProxy what : MessageEventGeneratorProxy.values()) {
      if (what.matches(s)) {
        match = what;
        break;
      }
    }
    if (match == null) {
      throw new IllegalArgumentException("No Match");
    }
    return match;
  }
  
  public RestartStrategy getRestartStrategy() {
    return restartStrategy;
  }

  public void setRestartStrategy(RestartStrategy restartStrategy) {
    this.restartStrategy = restartStrategy;
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
