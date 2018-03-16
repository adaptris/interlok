/*
 * Copyright 2016 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.jmx;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.FifoMutexLock;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("jmx-notification-consumer")
@AdapterComponent
@ComponentProfile(summary = "Listen for notifications against the specified ObjectName", tag = "consumer,jmx",
    recommended = {JmxConnection.class})
@DisplayOrder(order = {"serializer"})
public class JmxNotificationConsumer extends AdaptrisMessageConsumerImp implements NotificationListener {

  private static final TimeInterval DEFAULT_INTERVAL = new TimeInterval(60L, TimeUnit.SECONDS);
  @NotNull
  @AutoPopulated
  @Valid
  private NotificationSerializer serializer;

  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean failIfNotFound;

  private transient MBeanServerConnection connection;
  private transient ObjectName actualObjectName;
  private transient ScheduledExecutorService scheduler;
  private transient FifoMutexLock locker;
  // Not configurable
  private TimeInterval retryInterval;

  public JmxNotificationConsumer() {
    setSerializer(new SimpleNotificationSerializer());
    locker = new FifoMutexLock();
    changeState(ClosedState.getInstance());
  }

  @Override
  public void init() throws CoreException {
    try {
      scheduler = Executors.newSingleThreadScheduledExecutor(new ManagedThreadFactory(getClass().getSimpleName()));
      connection = retrieveConnection(JmxConnection.class).mbeanServerConnection();
      actualObjectName = ObjectName.getInstance(getDestination().getDestination());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void start() throws CoreException {
    boolean success = addNotificationListener();
    if (!success) {
      if (failIfNotFound()) {
        throw new CoreException("Failed to add NotificationListener");
      } else {
        scheduleNext();
      }
    }
  }

  @Override
  public void stop() {
    try {
      connection.removeNotificationListener(actualObjectName, this);
    } catch (Exception ignoredIntentionally) {

    }
  }

  @Override
  public void close() {
    shutdownScheduler();
  }

  @Override
  public void prepare() throws CoreException {}


  private AdaptrisMessage createMessage(Notification n) throws CoreException, IOException {
    AdaptrisMessageFactory fac = AdaptrisMessageFactory.defaultIfNull(getMessageFactory());
    AdaptrisMessage msg = getSerializer().serialize(n, fac.newMessage());
    return msg;
  }

  /**
   * @return the serializer
   */
  public NotificationSerializer getSerializer() {
    return serializer;
  }

  /**
   * @param serializer the serializer to set
   */
  public void setSerializer(NotificationSerializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public void handleNotification(Notification notification, Object handback) {
    try {
      retrieveAdaptrisMessageListener().onAdaptrisMessage(createMessage(notification));
    } catch (Exception e) {
      log.error("Unhandled exception {}", e.getMessage(), e);
    }
  }

  /**
   * @return the failIfNotFound
   */
  public Boolean getFailIfNotFound() {
    return failIfNotFound;
  }

  /**
   * Whether or not to fail if the ObjectName is not found.
   * <p>
   * If set to false, and the object is not found, then an attempt will be made periodically to check the MBeanServeConnection for
   * the object instance availability; when it becomes available, the notificaiton listener will be added at that point.
   * </p>
   * 
   * 
   * @param b the failIfNotFound to set, default is true
   */
  public void setFailIfNotFound(Boolean b) {
    this.failIfNotFound = b;
  }

  boolean failIfNotFound() {
    return getFailIfNotFound() != null ? getFailIfNotFound().booleanValue() : true;
  }

  private boolean addNotificationListener() {
    boolean success = false;
    try {
      connection.addNotificationListener(actualObjectName, this, null, null);
      success = true;
    } catch (InstanceNotFoundException e) {
      log.trace("{} not found", actualObjectName);
      success = false;
    } catch (IOException e) {
    }
    return success;
  }

  private void scheduleNext() {
    if (scheduler != null && locker.permitAvailable()) {
      log.trace("Scheduling next attempt in {}ms", retryInterval());
      scheduler.schedule(new NotificationRetry(), retryInterval(), TimeUnit.MILLISECONDS);
    }
  }

  private void shutdownScheduler() {
    try {
      locker.acquire();
      ManagedThreadFactory.shutdownQuietly(scheduler, DEFAULT_INTERVAL);
    } catch (Exception ignoredIntentionally) {
    } finally {
      scheduler = null;
      locker.release();
    }
  }

  private class NotificationRetry implements Runnable {
    NotificationRetry() {}

    @Override
    public void run() {
      boolean success = addNotificationListener();
      if (!success) {
        scheduleNext();
      }
    }
  }

  TimeInterval getRetryInterval() {
    return retryInterval;
  }

  void setRetryInterval(TimeInterval t) {
    this.retryInterval = Args.notNull(t, "retryInterval");
  }

  long retryInterval() {
    return getRetryInterval() != null ? getRetryInterval().toMilliseconds() : DEFAULT_INTERVAL.toMilliseconds();
  }

}
