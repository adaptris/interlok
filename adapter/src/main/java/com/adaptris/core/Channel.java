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

package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Links two {@link com.adaptris.core.AdaptrisConnection} implementations and has a {@link WorkflowList}
 * </p>
 * <p>
 * 
 * @config channel
 */
// Should probably implement EventAware...
@XStreamAlias("channel")
@AdapterComponent
@ComponentProfile(summary = "The base container for workflows", tag = "base")
public class Channel implements ComponentLifecycleExtension, StateManagedComponentContainer, EventHandlerAware {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisConnection consumeConnection;
  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisConnection produceConnection;
  @NotNull
  @AutoPopulated
  @Valid
  private WorkflowList workflowList;
  private ProcessingExceptionHandler messageErrorHandler;
  @NotNull
  @NotBlank
  private String uniqueId;
  @InputFieldDefault(value = "true")
  private Boolean autoStart;

  private transient boolean available;
  private transient ComponentState state;
  private transient ProcessingExceptionHandler activeErrorHandler;
  private transient boolean prepared = false;
  protected transient EventHandler eventHandler;
  protected transient Date startTime;
  protected transient Date stopTime;

  private transient Object lock = new Object();

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public Channel() {
    stopTime = new Date();

    setConsumeConnection(new NullConnection());
    setProduceConnection(new NullConnection());

    available = true;
    registerActiveMsgErrorHandler(new NullProcessingExceptionHandler());
    workflowList = new WorkflowList();

    changeState(ClosedState.getInstance());
  }

  @Override
  public void prepare() throws CoreException {
    produceConnection.addExceptionListener(this); // set back ref.
    consumeConnection.addExceptionListener(this); // set back ref.
    EventHandler ehToUse = eventHandler();
    for (Workflow w : workflowList) {
      w.registerChannel(this);
      w.registerEventHandler(ehToUse);
    }
    consumeConnection.prepare();
    produceConnection.prepare();
    workflowList.prepare();
    prepared = true;
  }

  private EventHandler eventHandler() throws CoreException {
    EventHandler ehToUse = eventHandler;
    if (ehToUse == null) {
      ehToUse = new DefaultEventHandler();
      ehToUse.requestStart();
    }
    return ehToUse;
  }

  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  public void changeState(ComponentState s) {
    state = s;
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    if (!prepared) {
      prepare();
    }
    synchronized (lock) {
      // If these are the same, then we have our own event handler; we need to register the event handler.
      if (retrieveActiveMsgErrorHandler().equals(getMessageErrorHandler())) {
        LifecycleHelper.registerEventHandler(retrieveActiveMsgErrorHandler(), eventHandler());
      }
      checkConnectionErrorHandlers();
      LifecycleHelper.init(messageErrorHandler);
      LifecycleHelper.init(produceConnection);
      LifecycleHelper.init(consumeConnection);
      LifecycleHelper.init(workflowList);
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    synchronized (lock) {
      LifecycleHelper.start(messageErrorHandler);
      LifecycleHelper.start(produceConnection);
      LifecycleHelper.start(workflowList);
      LifecycleHelper.start(consumeConnection);
    }
    startTime = new Date();
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    stopTime = new Date();
    synchronized (lock) {
      LifecycleHelper.stop(consumeConnection);
      LifecycleHelper.stop(workflowList);
      LifecycleHelper.stop(produceConnection);
      LifecycleHelper.stop(messageErrorHandler);
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    synchronized (lock) {
      LifecycleHelper.close(consumeConnection);
      LifecycleHelper.close(produceConnection);
      LifecycleHelper.close(workflowList);
      LifecycleHelper.close(messageErrorHandler);
    }
  }

  /**
   * <p>
   * Sets the <code>AdaptrisConnection</code> to use for consuming.
   * </p>
   * 
   * @param connection the <code>AdaptrisConnection</code> to use for consuming, may not be null
   */
  public void setConsumeConnection(AdaptrisConnection connection) {
    consumeConnection = Args.notNull(connection, "consumeConnection");
  }

  /**
   * <p>
   * Returns the <code>AdaptrisConnection</code> used for consuming.
   * </p>
   * 
   * @return the <code>AdaptrisConnection</code> used for consuming
   */
  public AdaptrisConnection getConsumeConnection() {
    return consumeConnection;
  }

  /**
   * <p>
   * Sets the <code>AdaptrisConnection</code> to use for producing.
   * </p>
   * 
   * @param connection the <code>AdaptrisConnection</code> to use for producing, may not be null
   */
  public void setProduceConnection(AdaptrisConnection connection) {
    produceConnection = Args.notNull(connection, "produceConnection");
  }

  /**
   * <p>
   * Returns the <code>AdaptrisConnection</code> used for producing.
   * </p>
   * 
   * @return the <code>AdaptrisConnection</code> used for producing
   */
  public AdaptrisConnection getProduceConnection() {
    return produceConnection;
  }

  /**
   * <p>
   * Sets the <code>WorkflowList</code> to use.
   * </p>
   * 
   * @param workflows the <code>WorkflowList</code> to use, may not be null
   */
  public void setWorkflowList(WorkflowList workflows) {
    workflowList = Args.notNull(workflows, "workflowList");
  }

  /**
   * <p>
   * Returns the <code>WorkflowList</code> to use.
   * </p>
   * 
   * @return the <code>WorkflowList</code> to use
   */
  public WorkflowList getWorkflowList() {
    return workflowList;
  }

  /**
   * <p>
   * Sets the <code>MessageErrorHandler</code> to use.
   * </p>
   * 
   * @param errorHandler the <code>MessageErrorHandler</code> to use, may not be null
   */
  public void setMessageErrorHandler(ProcessingExceptionHandler errorHandler) {
    messageErrorHandler = Args.notNull(errorHandler, "messageErrorHandler");
  }

  /**
   * <p>
   * Returns the <code>MessageErrorHandler</code> to use.
   * </p>
   * 
   * @return the <code>MessageErrorHandler</code> to use
   */
  public ProcessingExceptionHandler getMessageErrorHandler() {
    return messageErrorHandler;
  }

  /**
   * <p>
   * Returns <code>true</code> if this <code>Channel</code> is available. A <code>Channel</code> is available if i) it is not in the
   * process of being rebooted or ii) it has not failed to reboot.
   * </p>
   * 
   * @return <code>true</code> if this <code>Channel</code> is available
   */
  public boolean isAvailable() {
    return available;
  }

  /**
   * <p>
   * Set whether this <code>Channel</code> is available.
   * </p>
   * 
   * @param b whether this <code>Channel</code> is available
   */
  public void toggleAvailability(boolean b) {
    available = b;
    // log.trace("available set to [" + b + "]");
  }

  /**
   * Register the active <code>MessageErrorHandler</code> for this Channel.
   * 
   * @param m the active <code>MessageErrorHandler</code>
   */
  public void registerActiveMsgErrorHandler(ProcessingExceptionHandler m) {
    activeErrorHandler = Args.notNull(m, "activeErrorHandler");
  }

  /**
   * Return the active <code>MessageErrorHandler</code> for this Channel.
   * 
   * @return the active <code>MessageErrorHandler</code>
   */
  public ProcessingExceptionHandler retrieveActiveMsgErrorHandler() {
    return activeErrorHandler;
  }

  /**
   * <p>
   * Get the unique id of this channel.
   * </p>
   * 
   * @return the unique id
   */
  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * <p>
   * Set the unique id of this channel. The channel's unique id allows it to be individually controlled, either by an event or by
   * the Adapter container.
   * </p>
   * 
   * @param string the unique id
   */
  public void setUniqueId(String string) {
    uniqueId = string;
  }

  /**
   * <p>
   * Check if this channel has a unique Id.
   * </p>
   * 
   * @return true if the unique id is non-null and non- empty
   */
  public boolean hasUniqueId() {
    return !isBlank(getUniqueId());
  }

  /** @see com.adaptris.core.StateManagedComponent#requestInit() */
  @Override
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestStart() */
  @Override
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestStop() */
  @Override
  public void requestStop() {
    state.requestStop(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestClose() */
  @Override
  public void requestClose() {
    state.requestClose(this);
  }

  /**
   * <p>
   * This method is not <code>synchronized</code> and returns the 'last recorded' state of this object.
   * </p>
   * 
   * @see com.adaptris.core.StateManagedComponent#retrieveComponentState()
   */
  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  /**
   * @see StateManagedComponentContainer#requestChildInit()
   */
  @Override
  public void requestChildInit() throws CoreException {
    workflowList.init();
  }

  /**
   * @see StateManagedComponentContainer#requestChildStart()
   */
  @Override
  public void requestChildStart() throws CoreException {
    workflowList.start();
  }

  /**
   * @see StateManagedComponentContainer#requestChildStop()
   */
  @Override
  public void requestChildStop() {
    workflowList.stop();
  }

  /**
   * @see StateManagedComponentContainer#requestChildClose()
   */
  @Override
  public void requestChildClose() {
    workflowList.close();
  }

  /**
   * <p>
   * Checks that ConnectionErrorHandlers are not sematically equal.
   * 
   * However, if we have a shared connection where both are actually the same object,
   * then we should obviously not throw an exception.
   * </p>
   */
  private void checkConnectionErrorHandlers() throws CoreException {
    ConnectionErrorHandler cceh = getConsumeConnection().connectionErrorHandler();
    ConnectionErrorHandler pceh = getProduceConnection().connectionErrorHandler();

    if (cceh != null && pceh != null) {
      // are they actually the same object?
      if (!ObjectUtils.identityToString(cceh).equals(ObjectUtils.identityToString(pceh))) {
        if (!cceh.allowedInConjunctionWith(pceh)) {
          throw new CoreException("This channel has been configured with 2 ErrorHandlers that are " + "incompatible with each other");
        }
      }
    }
  }

  public Boolean getAutoStart() {
    return autoStart;
  }

  /**
   * Specify whether or not to auto-start the channel.
   * <p>
   * If auto-start is set to false, then this channel is not started. This behaviour only occurs if the channel has a non-null/empty
   * unique-id as otherwise you will not be able to start the channel through the standard JMX controls
   * </p>
   * 
   * @param autoStart default is true.
   */
  public void setAutoStart(Boolean autoStart) {
    this.autoStart = autoStart;
  }

  private boolean isAutoStart() {
    return autoStart != null ? autoStart.booleanValue() : true;
  }

  public boolean shouldStart() {
    if (!isAutoStart() && hasUniqueId()) {
      return false;
    }
    return true;
  }

  /**
   * Get the last time this channel was started
   * 
   * @return channel start time
   */
  public Date lastStartTime() {
    return startTime;
  }

  /**
   * Get the last time this channel was stopped. This is set when the channel is initialised so it may have been subsequently
   * started.
   * 
   * @return channel stop time
   */
  public Date lastStopTime() {
    return stopTime;
  }
}
