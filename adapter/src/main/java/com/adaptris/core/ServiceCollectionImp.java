package com.adaptris.core;

import static com.adaptris.core.util.LoggingHelper.friendlyName;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.FifoMutexLock;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.license.License;

/**
 * <p>
 * Behaviour common to <code>ServiceCollection</code>s.
 * </p>
 */
public abstract class ServiceCollectionImp extends AbstractCollection<Service> implements Service, ServiceCollection {

  private static final OutOfStateHandler DEFAULT_STATE_HANDLER = new RaiseExceptionOutOfStateHandler();
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private String uniqueId;
  private Boolean restartAffectedServiceOnException;
  @AdvancedConfig
  private Boolean continueOnFail;
  @AdvancedConfig
  private Boolean isTrackingEndpoint;
  @AdvancedConfig
  private Boolean isConfirmation;
  @AdvancedConfig
  @Deprecated
  private Boolean checkServiceState;
  @AdvancedConfig
  private OutOfStateHandler outOfStateHandler;
  @AutoPopulated
  @NotNull
  private List<Service> services;
  
  private transient FifoMutexLock lock = new FifoMutexLock();
  protected transient EventHandler eventHandler;
  private transient boolean isBranching; // defaults to false
  
  private transient ComponentState serviceListState;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ServiceCollectionImp() {
    setServices(new ArrayList<Service>());
    setUniqueId(new GuidGenerator().getUUID());
    changeState(ClosedState.getInstance());
  }

  public ServiceCollectionImp(Collection<Service> list) {
    this();
    addAll(list);
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  @Override
  public String createName() {
    return this.getClass().getName();
  }

  @Override
  public String createQualifier() {
    return defaultIfEmpty(getUniqueId(), "");
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Override
  public void setUniqueId(String s) {
    if (s == null) {
      throw new IllegalArgumentException("null param");
    }
    uniqueId = s;
  }

  @Override
  public boolean isBranching() {
    return isBranching;
  }

  @Override
  public boolean continueOnFailure() {
    return getContinueOnFail() != null ? getContinueOnFail().booleanValue() : false;
  }

  /**
   * @see com.adaptris.core.Service#continueOnFailure()
   * @return whether or not this service is configured to continue on failure.
   */
  public Boolean getContinueOnFail() {
    return continueOnFail;
  }

  /**
   * @see com.adaptris.core.Service#continueOnFailure() param b whether or not this service is configured to continue on failure.
   */
  public void setContinueOnFail(Boolean b) {
    continueOnFail = b;
  }

  public Boolean getIsTrackingEndpoint() {
    return isTrackingEndpoint;
  }

  public void setIsTrackingEndpoint(Boolean b) {
    isTrackingEndpoint = b;
  }

  public Boolean getIsConfirmation() {
    return isConfirmation;
  }

  public void setIsConfirmation(Boolean b) {
    isConfirmation = b;
  }

  /**
   *
   * @see com.adaptris.core.MessageEventGenerator#isTrackingEndpoint()
   */
  @Override
  public boolean isTrackingEndpoint() {
    if (isTrackingEndpoint != null) {
      return isTrackingEndpoint.booleanValue();
    }
    return false;
  }

  /**
   *
   * @see com.adaptris.core.MessageEventGenerator#isConfirmation()
   */
  @Override
  public boolean isConfirmation() {
    if (isConfirmation != null) {
      return isConfirmation.booleanValue();
    }
    return false;
  }

  /**
   * <p>
   * Adds a <code>Service</code> to the end of the configured <code>List</code>.
   * </p>
   *
   * @param service the <code>Service</code> to add to the end of the configured <code>List</code> may not be null
   */
  @Override
  public void addService(Service service) {
    add(service);
  }

  /**
   * <p>
   * Returns the configured <code>List</code> of <code>Service</code>s. May be empty but not null.
   * </p>
   *
   * @return the configured <code>List</code> of <code>Service</code>s
   */
  @Override
  public List<Service> getServices() {
    return services;
    // return new CastorizedList(this);
  }

  /**
   * Override the underlying service list.
   *
   * @param serviceList the service list.
   */
  public void setServices(List<Service> serviceList) {
    if (serviceList == null) {
      throw new IllegalArgumentException("ServiceList is null");
    }
    services = (List<Service>) enforceRequirements(serviceList);
  }

  @Override
  public final void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      lock.acquire();
      this.checkServiceStates();
      applyServices(msg);
    }
    catch (InterruptedException e) {
      throw new ServiceException(e);
    }
    catch (OutOfStateException e) {
      throw new ServiceException(e);
    }
    finally {
      lock.release();
    }
  }

  private void checkServiceStates() throws OutOfStateException {
    OutOfStateHandler handler = outOfStateHandler();
    for(Service service : this.getServices()) {
      try {
        if (!handler.isInCorrectState(service)) {
          handler.handleOutOfState(service);
        }
      } catch (OutOfStateException ex) {
        throw new OutOfStateException("Service (" + service.getUniqueId() + ") cannot be run, it is not in the correct state - " + service.retrieveComponentState().getClass().getSimpleName());
      }
    }
  }

  /**
   * Apply the required services to the message.
   *
   * @param msg the message
   * @throws ServiceException wrapping any underlying exception.
   */
  protected abstract void applyServices(AdaptrisMessage msg) throws ServiceException;

  @Override
  public final void init() throws CoreException {
    try {
      lock.acquire();
      for (Service s : this) {
        LifecycleHelper.registerEventHandler(s, eventHandler);
        LifecycleHelper.init(s);
      }
      doInit();
    }
    catch (InterruptedException e) {
      throw new CoreException(e);
    }
    finally {
      lock.release();
    }
  }

  /**
   * Start any additional components.
   *
   * @throws CoreException wrapping any underlying exception
   */
  protected abstract void doInit() throws CoreException;

  @Override
  public final void start() throws CoreException {
    try {
      lock.acquire();
      for (Service s : this) {
        LifecycleHelper.start(s);
      }
      doStart();
    }
    catch (InterruptedException e) {
      throw new CoreException(e);

    }
    finally {
      lock.release();
    }
  }

  /**
   * Start any additional components.
   *
   * @throws CoreException wrapping any underlying exception
   */
  protected abstract void doStart() throws CoreException;

  @Override
  public final void stop() {
    try {
      lock.acquire();
      for (Service s : this) {
        LifecycleHelper.stop(s);
      }
      doStop();
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    finally {
      lock.release();
    }
  }

  /**
   * Stop any additional components.
   *
   */
  protected abstract void doStop();

  @Override
  public final void close() {
    try {
      lock.acquire();
      for (Service s : this) {
        LifecycleHelper.close(s);
      }
      doClose();
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    finally {
      lock.release();
    }
  }

  /**
   * Close any additional components.
   *
   */
  protected abstract void doClose();

  /**
   * <p>
   * Updates the state for the component <code>ComponentState</code>.
   * </p>
   */
  public void changeState(ComponentState newState) {
    serviceListState = newState;
  }
  
  /**
   * <p>
   * Returns the last record <code>ComponentState</code>.
   * </p>
   * @return the current <code>ComponentState</code>
   */
  public ComponentState retrieveComponentState() {
    return serviceListState;
  }
  
  /**
   * <p>
   * Request this component is init'd.
   * </p>
   * @throws CoreException wrapping any underlying Exceptions
   */
  public void requestInit() throws CoreException {
    serviceListState.requestInit(this);
  }

  /**
   * <p>
   * Request this component is started.
   * </p>
   * @throws CoreException wrapping any underlying Exceptions
   */
  public void requestStart() throws CoreException {
    serviceListState.requestStart(this);
  }

  /**
   * <p>
   * Request this component is stopped.
   * </p>
   */
  public void requestStop() {
    serviceListState.requestStop(this);
  }

  /**
   * <p>
   * Request this component is closed.
   * </p>
   */
  public void requestClose() {
    serviceListState.requestClose(this);
  }
  
  @Override
  public boolean isEnabled(License license) throws CoreException {
    for (Service s : this) {
      if (!s.isEnabled(license)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void handleException(Service service, AdaptrisMessage msg, Exception e) throws ServiceException {
    msg.getObjectMetadata().put(CoreConstants.OBJ_METADATA_EXCEPTION, e);
    String serviceName = friendlyName(service);
    if (isRestartAffectedServiceOnException()) {
      log.debug("Service restarts on error, restarting [" + serviceName + "]");
      restartService(service);
    } 
    if ((service != null) && (service.continueOnFailure())) {
      log.debug("continue-on-fail is true, ignoring Exception [" + e.getMessage() + "] from [" + serviceName + "]");
    }
    else {
      if (e instanceof ServiceException) {
        throw (ServiceException) e;
      }
      else {
        throw new ServiceException(e);
      }
    }
  }

  private void restartService(Service s) throws ServiceException {
    try {
      LifecycleHelper.stop(s);
      LifecycleHelper.close(s);
      LifecycleHelper.init(s);
      LifecycleHelper.start(s);
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
  }

  private boolean isRestartAffectedServiceOnException() {
    return getRestartAffectedServiceOnException() == null ? false : getRestartAffectedServiceOnException().booleanValue();
  }

  public Boolean getRestartAffectedServiceOnException() {
    return restartAffectedServiceOnException;
  }

  /**
   * Whether to restart the service that threw the {@link ServiceException} during processing.
   *
   * @param b true to restart affected services, default false
   */
  public void setRestartAffectedServiceOnException(Boolean b) {
    restartAffectedServiceOnException = b;
  }

  @Override
  public boolean add(Service service) {
    return services.add(enforceRequirements(service));
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public Iterator<Service> iterator() {
    return services.iterator();
  }

  @Override
  public int size() {
    return services.size();
  }

  @Override
  public void add(int index, Service element) {
    services.add(index, enforceRequirements(element));
  }

  @Override
  public boolean addAll(Collection<? extends Service> c) {
    return services.addAll(enforceRequirements(c));
  }

  @Override
  public boolean addAll(int index, Collection<? extends Service> c) {
    return services.addAll(index, enforceRequirements(c));
  }

  @Override
  public Service get(int index) {
    return services.get(index);
  }

  @Override
  public int indexOf(Object o) {
    return services.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return services.lastIndexOf(o);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public ListIterator<Service> listIterator() {
    return services.listIterator();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public ListIterator<Service> listIterator(int index) {
    return services.listIterator(index);
  }

  @Override
  public Service remove(int index) {
    return services.remove(index);
  }

  @Override
  public Service set(int index, Service element) {
    return services.set(index, element);
  }

  @Override
  public List<Service> subList(int fromIndex, int toIndex) {
    return services.subList(fromIndex, toIndex);
  }

  @Override
  public void clear() {
    services.clear();
  }

  /**
   * Enforce any requirements of the list.
   *
   * @param service the service due to be added.
   * @return the service
   * @throws IllegalArgumentException if the requirements are not met
   */
  protected Service enforceRequirements(Service service) {
    if (service == null) {
      throw new IllegalArgumentException("Service is null");
    }
    return service;
  }

  /**
   * Enforce any requirements of the list.
   *
   * @param collection the services due to be added.
   * @return the collection
   * @throws IllegalArgumentException if the requirements are not met
   */
  protected Collection<? extends Service> enforceRequirements(Collection<? extends Service> collection) {
    return collection;
  }

  /**
   * 
   * @deprecated since 3.0.6 configure an explicit {@link OutOfStateHandler} using {@link #setOutOfStateHandler(OutOfStateHandler)).
   */
  @Deprecated
  public Boolean getCheckServiceState() {
    return checkServiceState;
  }

  /**
   * 
   * @deprecated since 3.0.6 configure an explicit {@link OutOfStateHandler} using {@link #setOutOfStateHandler(OutOfStateHandler)).
   */
  @Deprecated
  public void setCheckServiceState(Boolean checkServiceState) {
    this.checkServiceState = checkServiceState;
  }

  public OutOfStateHandler getOutOfStateHandler() {
    return outOfStateHandler;
  }

  /**
   * Set the behaviour when internal services are not in the correct state.
   * 
   * @param handler if not specified defaults to {@link RaiseExceptionOutOfStateHandler}.
   */
  public void setOutOfStateHandler(OutOfStateHandler handler) {
    this.outOfStateHandler = handler;
  }

  private OutOfStateHandler outOfStateHandler() {
    OutOfStateHandler result = getOutOfStateHandler() != null ? getOutOfStateHandler() : DEFAULT_STATE_HANDLER;
    if (getCheckServiceState() != null) {
      log.warn("setCheckService() is deprecated; explicitly configure using #setOutOfStateHandler() instead");
      // They've asked for no service-state checking; and OOSH isn't configured...
      if (Boolean.FALSE.equals(getCheckServiceState()) && result == DEFAULT_STATE_HANDLER) {
        result = new NullOutOfStateHandler();
      }
    }
    return result;
  }

}
