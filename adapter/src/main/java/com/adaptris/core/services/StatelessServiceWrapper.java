package com.adaptris.core.services;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollectionImp;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Stateless service wrapper, wraps any configured service and allows you to set a strategy on when to restart the service before
 * allowing it to execute.
 * </p>
 * <p>
 * Consider using this wrapper on a service where you will periodically want to stop and re-start a service. Check
 * {@link RestartStrategy} for the available restarting strategies.
 * </p>
 * 
 * @author lchan
 * @config stateless-service-wrapper
 * @license BASIC
 */
@XStreamAlias("stateless-service-wrapper")
public class StatelessServiceWrapper extends ServiceImp implements EventHandlerAware {

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
      @Override
      void setIsConfirmation(Service s, Boolean b) {
        ((ServiceImp) s).setIsConfirmation(b);
      }
      @Override
      Boolean getIsConfirmation(Service s) {
        return ((ServiceImp) s).getIsConfirmation();
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
      @Override
      void setIsConfirmation(Service s, Boolean b) {
        ((ServiceCollectionImp) s).setIsConfirmation(b);
      }
      @Override
      Boolean getIsConfirmation(Service s) {
        return ((ServiceCollectionImp) s).getIsConfirmation();
      }
    };
    abstract boolean matches(Service s);
    abstract void setIsTrackingEndpoint(Service s, Boolean b);
    abstract Boolean getIsTrackingEndpoint(Service s);
    abstract void setIsConfirmation(Service s, Boolean b);
    abstract Boolean getIsConfirmation(Service s);
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

  /** @see AdaptrisComponent */
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
  public boolean continueOnFailure() {
    if (getService() != null) {
      return getService().continueOnFailure();
    }
    return super.continueOnFailure();
  }


  @Override
  public boolean isTrackingEndpoint() {
    if (getService() != null) {
      return getService().isTrackingEndpoint();
    }
    return super.isTrackingEndpoint();
  }

  @Override
  public boolean isConfirmation() {
    if (getService() != null) {
      return getService().isConfirmation();
    }
    return super.isConfirmation();
  }

  @Override
  public boolean isBranching() {
    if (getService() != null) {
      return getService().isBranching();
    }
    return super.isBranching();
  }

  @Override
  public void close() {
  }

  @Override
  public void init() throws CoreException {
    if (getService() == null) {
      throw new CoreException("No wrapped service");
    }
  }

  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Basic) && (getService() != null ? getService().isEnabled(l) : true);
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

  @Override
  public Boolean getIsConfirmation() {
    try {
      MessageEventGeneratorProxy what = getProxy(getService());
      return what.getIsConfirmation(getService());
    }
    catch (IllegalArgumentException e) {
      return super.getIsConfirmation();
    }
  }

  @Override
  public void setIsConfirmation(Boolean b) {
    try {
      MessageEventGeneratorProxy what = getProxy(getService());
      what.setIsConfirmation(getService(), b);
    }
    catch (IllegalArgumentException e) {
      super.setIsConfirmation(b);
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
}
