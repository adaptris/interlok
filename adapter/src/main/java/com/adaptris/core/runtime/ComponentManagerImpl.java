package com.adaptris.core.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.StoppedState;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;

/**
 * Base class for all component managers.
 * 
 * @author lchan
 */
public abstract class ComponentManagerImpl<E extends StateManagedComponent> extends NotificationBroadcasterSupport implements
    AdapterRuntimeComponent<E> {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  private transient String xmlConfig;
  private transient ManagedThreadFactory threadFactory = new ManagedThreadFactory();
  private AtomicInteger sequenceNumber = new AtomicInteger();
  private AtomicInteger requestNumber = new AtomicInteger();
  private static final String THREAD_NAME_PREFIX = "JMX-Request-";

  protected enum ComponentNotificationType {
    LIFECYCLE, CONFIG
  };

  public ComponentManagerImpl() {
    super(Executors.newCachedThreadPool(new ManagedThreadFactory()));
  }

  /**
   * Check whether the transition is allowed in the context of the current component's (or parent's) state.
   * 
   * @param futureState the state we want to transition to.
   * @throws CoreException if the state transition check fails.
   * @throws IllegalArgumentException - if the futureState could not be handled.
   */
  protected abstract void checkTransitionTo(ComponentState futureState) throws CoreException, IllegalArgumentException;

  @Override
  public String getUniqueId() {
    return getWrappedComponent().getUniqueId();

  }

  @Override
  public ComponentState getComponentState() {
    return getWrappedComponent().retrieveComponentState();
  }

  @Override
  public void requestInit() throws CoreException {
    checkTransitionTo(InitialisedState.getInstance());
    LifecycleHelper.init(getWrappedComponent());
    sendLifecycleNotification(NOTIF_MSG_INITIALISED, getComponentState());
  }

  @Override
  public void requestInit(final long timeout) throws CoreException, TimeoutException {
    checkTransitionTo(InitialisedState.getInstance());
    final CyclicBarrier barrier = new CyclicBarrier(2);
    executeAndWait(timeout, barrier, new Runnable() {
      @Override
      public void run() {
        Thread current = Thread.currentThread();
        current.setName(THREAD_NAME_PREFIX + requestNumber.getAndIncrement());
        try {
          LifecycleHelper.init(getWrappedComponent());
        }
        catch (CoreException e) {
          current.getUncaughtExceptionHandler().uncaughtException(current, e);
        }
        try {
          barrier.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
          current.getUncaughtExceptionHandler().uncaughtException(current, e);
        }
      }
    });
    sendLifecycleNotification(NOTIF_MSG_INITIALISED, getComponentState());
  }

  @Override
  public void requestStart() throws CoreException {
    checkTransitionTo(StartedState.getInstance());
    LifecycleHelper.start(getWrappedComponent());
    sendLifecycleNotification(NOTIF_MSG_STARTED, getComponentState());
  }

  @Override
  public void requestStart(final long timeout) throws CoreException, TimeoutException {
    checkTransitionTo(StartedState.getInstance());
    final CyclicBarrier barrier = new CyclicBarrier(2);
    executeAndWait(timeout, barrier, new Runnable() {
      @Override
      public void run() {
        Thread current = Thread.currentThread();
        current.setName(THREAD_NAME_PREFIX + requestNumber.getAndIncrement());
        try {
          LifecycleHelper.start(getWrappedComponent());
        }
        catch (CoreException e) {
          current.getUncaughtExceptionHandler().uncaughtException(current, e);
        }
        try {
          barrier.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
          current.getUncaughtExceptionHandler().uncaughtException(current, e);
        }
      }
    });
    sendLifecycleNotification(NOTIF_MSG_STARTED, getComponentState());

  }

  @Override
  public void requestStop() throws CoreException {
    checkTransitionTo(StoppedState.getInstance());
    LifecycleHelper.stop(getWrappedComponent());
    sendLifecycleNotification(NOTIF_MSG_STOPPED, getComponentState());
  }

  public void requestStop(final long timeout) throws CoreException, TimeoutException {
    checkTransitionTo(StoppedState.getInstance());
    final CyclicBarrier barrier = new CyclicBarrier(2);
    executeAndWait(timeout, barrier, new Runnable() {
      @Override
      public void run() {
        Thread current = Thread.currentThread();
        current.setName(THREAD_NAME_PREFIX + requestNumber.getAndIncrement());
        LifecycleHelper.stop(getWrappedComponent());
        try {
          barrier.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
          current.getUncaughtExceptionHandler().uncaughtException(current, e);
        }
      }
    });
    sendLifecycleNotification(NOTIF_MSG_STOPPED, getComponentState());

  }

  @Override
  public void requestClose() throws CoreException {
    checkTransitionTo(ClosedState.getInstance());
    LifecycleHelper.close(getWrappedComponent());
    sendLifecycleNotification(NOTIF_MSG_CLOSED, getComponentState());
  }

  public void requestClose(final long timeout) throws CoreException, TimeoutException {
    checkTransitionTo(ClosedState.getInstance());
    final CyclicBarrier barrier = new CyclicBarrier(2);
    executeAndWait(timeout, barrier, new Runnable() {
      @Override
      public void run() {
        Thread current = Thread.currentThread();
        current.setName(THREAD_NAME_PREFIX + requestNumber.getAndIncrement());
        LifecycleHelper.close(getWrappedComponent());
        try {
          barrier.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
          current.getUncaughtExceptionHandler().uncaughtException(current, e);
        }
      }
    });
    sendLifecycleNotification(NOTIF_MSG_CLOSED, getComponentState());
  }

  @Override
  public void requestRestart() throws CoreException {
    requestClose();
    requestStart();
    sendLifecycleNotification(NOTIF_MSG_RESTARTED, getComponentState());
  }

  public void requestRestart(long timeout) throws CoreException, TimeoutException {
    requestClose(timeout);
    requestStart(timeout);
    sendLifecycleNotification(NOTIF_MSG_RESTARTED, getComponentState());
  }

  /**
   * Marshal the object as XML.
   * 
   */
  protected String asXml(Object o) throws CoreException {
    return DefaultMarshaller.getDefaultMarshaller().marshal(o);
  }

  @Override
  public String getConfiguration() throws CoreException {
    return xmlConfig;
  }

  /**
   * Store the XML representation of the object.
   * 
   * <p>
   * Note that this does not send any notifications, and is only intended for use upon first initialisation; you should probably use
   * {@link #marshalAndSendNotification()} if the config has been updated at runtime.
   * </p>
   * 
   * @throws CoreException
   */
  protected void marshalConfig() throws CoreException {
    xmlConfig = asXml(getWrappedComponent());
  }

  /**
   * Store the XML representation of the object and send a config-update notification.
   * 
   * @throws CoreException
   */
  protected void marshalAndSendNotification() throws CoreException {
    marshalConfig();
    sendConfigUpdateNotification();
  }

  /**
   * Convenience method to handle {@link BaseComponentMBean#unregisterMBean()}
   * 
   */
  protected void unregisterSelf() throws CoreException {
    try {
      JmxHelper.unregister(createObjectName());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   * Convenience method to handle {@link BaseComponentMBean#registerMBean()}
   * 
   */
  protected void registerSelf() throws CoreException {
    try {
      JmxHelper.register(createObjectName(), this);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  protected void ensureState(ComponentState... states) {
    ComponentState currentState = getWrappedComponent().retrieveComponentState();
    List<ComponentState> stateList = Arrays.asList(states);
    if (!stateList.contains(currentState)) {
      throw new IllegalStateException("Adapter State is currently [" + getWrappedComponent().retrieveComponentState()
          + "] must be [" + stateList + "]");
    }
  }

  private void sendLifecycleNotification(String message, ComponentState newState) {
    try {
      Notification n = createLifecycleNotification(message, createObjectName());
      n.setUserData(newState);
      sendNotification(n);
    }
    catch (MalformedObjectNameException e) {
      // Don't care about notifications really.
    }
  }

  protected Notification createLifecycleNotification(String message, ObjectName objectName) {
    return new Notification(getNotificationType(ComponentNotificationType.LIFECYCLE), objectName, sequenceNumber.getAndIncrement(),
        message);
  }

  protected void sendConfigUpdateNotification() {
    try {
      Notification n = new Notification(getNotificationType(ComponentNotificationType.CONFIG), createObjectName(),
          sequenceNumber.getAndIncrement(), NOTIF_MSG_CONFIG_UPDATED);
      n.setUserData(getConfiguration());
      sendNotification(n);
    }
    catch (MalformedObjectNameException | CoreException e) {
      // Don't care about notifications really.
    }
  }

  protected abstract String getNotificationType(ComponentNotificationType type);

  protected void closeQuietly(AdaptrisComponent c) {
    LifecycleHelper.stop(c);
    LifecycleHelper.close(c);
  }

  private void executeAndWait(final long timeout, CyclicBarrier barrier, Runnable runnable) throws CoreException, TimeoutException {
    if (timeout < 1) throw new IllegalArgumentException("Timeout < 1ms");
    CoreExceptionHandler exceptionHandler = new CoreExceptionHandler();
    Thread initThread = threadFactory.newThread(runnable);
    initThread.setUncaughtExceptionHandler(exceptionHandler);
    initThread.start();
    try {
      barrier.await(timeout, TimeUnit.MILLISECONDS);
    }
    catch (BrokenBarrierException | InterruptedException gateException) {
      ExceptionHelper.rethrowCoreException(gateException);
    }
    exceptionHandler.throwFirstException();
  }

  private class CoreExceptionHandler implements Thread.UncaughtExceptionHandler {
    private List<Throwable> exceptionList = Collections.synchronizedList(new ArrayList<Throwable>());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
      exceptionList.add(e);
    }

    public void throwFirstException() throws CoreException {
      for (Throwable t : exceptionList) {
        if (t instanceof CoreException) {
          throw (CoreException) t;
        }
      }
      if (exceptionList.size() > 0) {
        Throwable t = exceptionList.get(0);
        if (t instanceof RuntimeException) {
          throw (RuntimeException) t;
        }
        else if (t instanceof Error) {
          throw (Error) t;
        }
        else {
          throw new CoreException(t);
        }
      }
    }
  }

}
