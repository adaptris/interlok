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

package com.adaptris.core.runtime;

import java.util.concurrent.TimeoutException;

import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;

/**
 * Base interface that exposes management functionality for an Adapter component.
 *
 * @author lchan
 */
public interface AdapterComponentMBean extends BaseComponentMBean {

  String PROPERTY_SEPARATOR = ",";
  String EQUALS = "=";

  /**
   * ObjectName Key representing the unique-id of a component
   *
   */
  String KEY_ID = "id";
  /**
   * ObjectName Key representing the adapter unique-id
   *
   */
  String KEY_ADAPTER = "adapter";
  /**
   * ObjectName Key representing parent channel unique-id
   *
   */
  String KEY_CHANNEL = "channel";
  /**
   * ObjectName Key representing the parent workflow unique-id
   *
   */
  String KEY_WORKFLOW = "workflow";

  /**
   * The standard prefix indicating the id of this component, which is @{value}
   */
  String ID_PREFIX = PROPERTY_SEPARATOR + KEY_ID + EQUALS;

  /**
   * The standard prefix indicating the parent adapter which is {@value}
   */
  String ADAPTER_PREFIX = PROPERTY_SEPARATOR + KEY_ADAPTER + EQUALS;

  /**
   * The standard prefix indicating the parent channel which is {@value}
   */
  String CHANNEL_PREFIX = PROPERTY_SEPARATOR + KEY_CHANNEL + EQUALS;

  /**
   * The standard prefix indicating the parent workflow which is {@value}
   */
  String WORKFLOW_PREFIX = PROPERTY_SEPARATOR + KEY_WORKFLOW + EQUALS;

  /**
   * The standard Domain name for components.
   *
   */
  String JMX_DOMAIN_NAME = "com.adaptris";

  /**
   * The standard JMX Prefix specifying domain and type which resolves to {@value}
   *
   */
  String JMX_ADAPTER_TYPE = JMX_DOMAIN_NAME + ":type=Adapter";

  /**
   * The standard JMX Prefix specifying domain and type which resolves to {@value}
   *
   */
  String JMX_WORKFLOW_TYPE = JMX_DOMAIN_NAME + ":type=Workflow";

  /**
   * The standard JMX Prefix for channels which resolves to {@value}
   *
   */
  String JMX_CHANNEL_TYPE = JMX_DOMAIN_NAME + ":type=Channel";

  /**
   * The standard JMX Prefix for a given Message Error Digest exposed via JMX which resolves to {@value}
   *
   */
  String JMX_MSG_ERR_DIGESTER_TYPE = JMX_DOMAIN_NAME + ":type=MessageErrorDigest";

  /**
   * The standard JMX Prefix for a {@link com.adaptris.core.LogHandler} that is exposed via JMX which resolves to {@value}
   *
   */
  String JMX_LOG_HANDLER_TYPE = JMX_DOMAIN_NAME + ":type=LogHandler";

  /**
   * The standard JMX Prefix for a {@link com.adaptris.core.FailedMessageRetrier} that is exposed via JMX which resolves to {@value}
   *
   */
  String JMX_FAILED_MESSAGE_RETRIER_TYPE = JMX_DOMAIN_NAME + ":type=FailedMessageRetrier";

  /**
   * The standard JMX Prefix for a given MessageMetricsStatistics exposed via JMX which resolves to {@value}
   *
   */
  String JMX_METRICS_TYPE = JMX_DOMAIN_NAME + ":type=Metrics";

  /**
   * The standard JMX Prefix for a given MessageInFlight Interceptor exposed via JMX which resolves to {@value}
   *
   */
  String JMX_INFLIGHT_TYPE = JMX_DOMAIN_NAME + ":type=InFlight";

  /**
   * The standard JMX Prefix for a given Filesystem monitor exposed via JMX which resolves to {@value}
   *
   */
  String JMX_FS_MONITOR_TYPE = JMX_DOMAIN_NAME + ":type=FsMonitor";

  /**
   * The standard JMX Prefix for a given consumer monitor exposed via JMX which resolves to {@value}
   *
   */
  String JMX_CONSUMER_MONITOR_TYPE = JMX_DOMAIN_NAME + ":type=ConsumerMonitor";

  /**
   * The standard JMX Prefix for a given RetryMessageErrorHandler monitor exposed via JMX which resolves to {@value}
   *
   */
  String JMX_RETRY_MONITOR_TYPE = JMX_DOMAIN_NAME + ":type=RetryMessageHandlerMonitor";

  /**
   * The standard JMX Prefix for an Interceptor which emits {@link javax.management.Notification} which resolves to {@value}
   *
   */
  String JMX_NOTIFIER_TYPE = JMX_DOMAIN_NAME + ":type=Notifications";

  /**
   * The notification type for adapter lifecycle notifications '{@value} '.
   *
   */
  String NOTIF_TYPE_ADAPTER_LIFECYCLE = "adaptris.jmx.adapter.lifecycle";

  /**
   * The notification type for channel lifecycle notifications '{@value} '.
   *
   */
  String NOTIF_TYPE_CHANNEL_LIFECYCLE = "adaptris.jmx.channel.lifecycle";

  /**
   * The notification type for workflow lifecycle notifications '{@value} '.
   *
   */
  String NOTIF_TYPE_WORKFLOW_LIFECYCLE = "adaptris.jmx.workflow.lifecycle";

  /**
   * Notification type for workflow config update notifications '{@value} '
   *
   */
  String NOTIF_TYPE_WORKFLOW_CONFIG = "adaptris.jmx.workflow.config";
  /**
   * Notification type for channel config update notifications '{@value} '
   *
   */
  String NOTIF_TYPE_CHANNEL_CONFIG = "adaptris.jmx.channel.config";
  /**
   * Notification type for adapter config update notifications '{@value} '
   *
   */
  String NOTIF_TYPE_ADAPTER_CONFIG = "adaptris.jmx.adapter.config";

  /**
   * Standard Message for a configuration update.
   */
  String NOTIF_MSG_CONFIG_UPDATED = "Configuration Updated";

  /**
   * The standard message for a component being initialised '{@value} '.
   *
   */
  String NOTIF_MSG_INITIALISED = "Component Initialised";
  /**
   * The standard message for a component being started '{@value} '.
   *
   */
  String NOTIF_MSG_STARTED = "Component Started";
  /**
   * The standard message for a component being stopped '{@value} '.
   *
   */
  String NOTIF_MSG_STOPPED = "Component Stopped";
  /**
   * The standard message for a component being closed '{@value} '.
   *
   */
  String NOTIF_MSG_CLOSED = "Component Closed";
  /**
   * The standard message for a component being restarted '{@value} '.
   *
   */
  String NOTIF_MSG_RESTARTED = "Component Restarted";

  /**
   * Initialise this component.
   *
   * @throws CoreException wrapping any underlying Exception
   * @deprecated since 3.0.0, use {@link #requestInit(long)} instead.
   */
  @Deprecated
  void requestInit() throws CoreException;

  /**
   * Initialise this component.
   * <p>
   * A timeout is required as Connections within the adapter may be configured to retry forever (if a broker is not available for
   * instance) which would cause this method to never return.
   * </p>
   *
   * @param timeoutMs the max timeout to wait for in milliseconds
   * @throws CoreException wrapping any underlying adapter exception
   * @throws TimeoutException if the timeout was exceeded.
   */
  void requestInit(long timeoutMs) throws CoreException, TimeoutException;

  /**
   * Start this component.
   *
   * @throws CoreException wrapping any underlying Exception
   * @deprecated since 3.0.0, use {@link #requestStart(long)} instead.
   */
  @Deprecated
  void requestStart() throws CoreException;

  /**
   * Start this component.
   * <p>
   * A timeout is required as Connections within the adapter may be configured to retry forever (if a broker is not available for
   * instance) which would cause this method to never return.
   * </p>
   *
   * @param timeoutMs the max timeout to wait for in milliseconds
   * @throws CoreException wrapping any underlying adapter exception
   * @throws TimeoutException if the timeout was exceeded.
   */
  void requestStart(long timeoutMs) throws CoreException, TimeoutException;

  /**
   * stop this component.
   *
   * @throws CoreException wrapping any underlying Exception
   * @deprecated since 3.0.0, use {@link #requestStop(long)} instead.
   */
  @Deprecated
  void requestStop() throws CoreException;

  /**
   * Stop this component.
   * <p>
   * A timeout is required as Connections within the adapter may be configured to retry forever (if a broker is not available for
   * instance) which would cause this method to never return.
   * </p>
   *
   * @param timeout the max timeout to wait for in milliseconds
   * @throws CoreException wrapping any underlying adapter exception
   * @throws TimeoutException if the timeout was exceeded.
   */
  void requestStop(long timeout) throws CoreException, TimeoutException;

  /**
   * Close this component.
   *
   * @throws CoreException wrapping any underlying Exception
   * @deprecated since 3.0.0, use {@link #requestClose(long)} instead.
   */
  @Deprecated
  void requestClose() throws CoreException;

  /**
   * Close this component.
   * <p>
   * A timeout is required as Connections within the adapter may be configured to retry forever (if a broker is not available for
   * instance) which would cause this method to never return.
   * </p>
   *
   * @param timeoutMs the max timeout to wait for in milliseconds
   * @throws CoreException wrapping any underlying adapter exception
   * @throws TimeoutException if the timeout was exceeded.
   */
  void requestClose(long timeoutMs) throws CoreException, TimeoutException;

  /**
   * Restart this channel.
   * <p>
   * This is semantically equivalent to calling {@link #requestClose()} and {@link #requestStart()}
   * </p>
   *
   * @throws CoreException wrapping any underlying Exception
   * @deprecated since 3.0.0, use {@link #requestRestart(long)} instead.
   */
  @Deprecated
  void requestRestart() throws CoreException;

  /**
   * Restart this component.
   * <p>
   * This just invokes {@link #requestClose(long)} and {@link #requestStart(long)} in sequence with the timeoutMs parameter. Bear in
   * mind that a {@code TimeoutException} may not occur until 2 * timeoutMs depending on how long the component takes to close and
   * start.
   * </p>
   *
   * @param timeoutMs the max timeout to pass into {@link #requestClose(long)} and {@link #requestStart(long)} in ms. This means
   *          that a {@code TimeoutException} might not occur until 2 * timeoutMs.
   * @throws CoreException wrapping any underlying adapter exception
   * @throws TimeoutException if the timeout was exceeded.
   */
  void requestRestart(long timeoutMs) throws CoreException, TimeoutException;

  /**
   * Returns timestamp of the last start time for this component
   *
   * @return the last start time, or 0 if the component has never been started.
   */
  long requestStartTime();

  /**
   * Returns timestamp of the last stop time for this component
   *
   * @return the last stop time; generally speaking this will be the time when the component was "created" if an explicit stop has
   *         never been requested.
   */
  long requestStopTime();

  /**
   * Get the state of this managed runtime component.
   *
   * @return the component state
   */
  ComponentState getComponentState();

  /**
   * Get the name of this runtime component.
   *
   * @return the name of this component which is generally semantically equivalent to the corresponding
   *         {@link com.adaptris.core.Workflow#getUniqueId()} or {@link com.adaptris.core.Channel#getUniqueId()}
   */
  String getUniqueId();

  /**
   * Get a marshalled copy of the configuration for this item.
   *
   * @return a string representation of the config for this item.
   * @throws CoreException wrapping any underlying Exception
   * @see DefaultMarshaller#getDefaultMarshaller()
   */
  String getConfiguration() throws CoreException;

  /**
   * Get the class name that is wrapped by this MBean.
   *
   * @return the class name (e.g. {@code com.adaptris.core.StandardWorkflow})
   */
  String getWrappedComponentClassname();

}
