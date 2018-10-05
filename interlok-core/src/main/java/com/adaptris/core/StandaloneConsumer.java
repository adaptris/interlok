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

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Wrapper class for a <code>AdaptrisMessageConsumer</code> and a <code>AdaptrisConnection</code>. Implements
 * <code>AdaptrisMessageConsumer</code> and delegates all method calls to the underlying consumer.
 * </p>
 * 
 * @config standalone-consumer
 */
@XStreamAlias("standalone-consumer")
@AdapterComponent
@ComponentProfile(summary = "Standalone wrapper for a consumer and connection", tag = "consumer,base")
public class StandaloneConsumer implements AdaptrisMessageConsumer, StateManagedComponent, ComponentLifecycleExtension {
  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private AdaptrisConnection connection;
  private AdaptrisMessageConsumer consumer;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean isTrackingEndpoint;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean isConfirmation;
  private transient ComponentState consumerState;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public StandaloneConsumer() {
    this(new NullConnection(), new NullMessageConsumer());
  }

  public StandaloneConsumer(AdaptrisMessageConsumer consumer) {
    this(new NullConnection(), consumer);
  }

  public StandaloneConsumer(AdaptrisConnection c) {
    this(c, new NullMessageConsumer());
  }

  public StandaloneConsumer(AdaptrisConnection c, AdaptrisMessageConsumer amc) {
    setConnection(c);
    setConsumer(amc);
    changeState(ClosedState.getInstance());
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    getConnection().addExceptionListener(this);
    getConnection().addMessageConsumer(getConsumer());
    LifecycleHelper.init(getConnection());
    LifecycleHelper.init(getConsumer());
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getConnection());
    LifecycleHelper.start(getConsumer());
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public void stop() {
    LifecycleHelper.stop(getConsumer());
    LifecycleHelper.stop(getConnection());
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
    LifecycleHelper.close(getConsumer());
    LifecycleHelper.close(getConnection());
  }

  // @Override
  // public AdaptrisConnection retrieveConnection() {
  // return connection;
  // }

  @Override
  public <T> T retrieveConnection(Class<T> type) {
    return connection.retrieveConnection(type);
  }

  @Override
  public String createQualifier() {
    return defaultIfEmpty(getUniqueId(), "");
  }

  @Override
  public String getUniqueId() {
    return getConsumer().getUniqueId();
  }

  /**
   * Get the connection to be used.
   * <p>
   * As this class is designed to be marshalled to XML, the getter and setter for the connection is required to be present, and
   * simply proxies the underlying AdaptrisMessageWorker implementation.
   * </p>
   * 
   * @return the connection
   */
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * Set the connection to be used.
   * <p>
   * As this class is designed to be marshalled to XML, the getter and setter
   * for the connection is required to be present, and simply proxies the
   * underlying AdaptrisMessageWorker implementation.
   * </p>
   *
   * @see AdaptrisMessageWorker#registerConnection(AdaptrisConnection)
   * @param conn the connection
   */
  public void setConnection(AdaptrisConnection conn) {
    connection = Args.notNull(conn, "connection");
  }

  /**
   * <p>
   * Sets the <code>AdaptrisConnection</code> to use. May not be null.
   * </p>
   *
   * @param conn the <code>AdaptrisConnection</code> to use
   */
  @Override
  public void registerConnection(AdaptrisConnection conn) {
    connection = Args.notNull(conn, "connection");;
    connection.addExceptionListener(this);
  }

  /**
   * <p>
   * Returns the underlying <code>AdaptrisMessageConsumer</code>.
   * </p>
   *
   * @return the underlying <code>AdaptrisMessageConsumer</code>
   */
  public AdaptrisMessageConsumer getConsumer() {
    return consumer;
  }

  /**
   * <p>
   * Sets the underlying <code>AdaptrisMessageConsumer</code>. May not be null.
   * </p>
   *
   * @param cons the underlying <code>AdaptrisMessageConsumer</code>
   */
  public void setConsumer(AdaptrisMessageConsumer cons) {
    consumer = Args.notNull(cons, "consumer");
  }


  // methods below delegate to the underlying consumer...

  /**
   * @see com.adaptris.core.AdaptrisMessageConsumer
   *      #registerAdaptrisMessageListener(com.adaptris.core.AdaptrisMessageListener)
   */
  @Override
  public void registerAdaptrisMessageListener(AdaptrisMessageListener l) {
    getConsumer().registerAdaptrisMessageListener(l);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageConsumer#getDestination()
   */
  @Override
  public ConsumeDestination getDestination() {
    return getConsumer().getDestination();
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageConsumer
   *      #setDestination(com.adaptris.core.ConsumeDestination)
   */
  @Override
  public void setDestination(ConsumeDestination destination) {
    getConsumer().setDestination(destination);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageWorker#getEncoder()
   */
  @Override
  public AdaptrisMessageEncoder getEncoder() {
    return getConsumer().getEncoder();
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageWorker#setEncoder
   *      (com.adaptris.core.AdaptrisMessageEncoder)
   */
  @Override
  public void setEncoder(AdaptrisMessageEncoder encoder) {
    if (getConsumer().getEncoder() != null) {
      log.warn("Ignoring attempt to implicitly overwrite the inner encoder");
    }
    else {
      getConsumer().setEncoder(encoder);
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageWorker#handleConnectionException()
   */
  @Override
  public void handleConnectionException() throws CoreException {
    getConsumer().handleConnectionException();
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageWorker#encode
   *      (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public byte[] encode(AdaptrisMessage msg) throws CoreException {
    return getConsumer().encode(msg);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageWorker#decode(byte[])
   */
  @Override
  public AdaptrisMessage decode(byte[] bytes) throws CoreException {
    return getConsumer().decode(bytes);
  }

  @Override
  public void prepare() throws CoreException {
    getConnection().prepare();
    getConsumer().prepare();
  }


  /**
   * @see com.adaptris.core.MessageEventGenerator#createName()
   */
  @Override
  public String createName() {
    return getConsumer().createName();
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
   *
   * @see com.adaptris.core.AdaptrisMessageWorker#getMessageFactory()
   */
  @Override
  public AdaptrisMessageFactory getMessageFactory() {
    return getConsumer().getMessageFactory();
  }

  /**
   *
   * @see AdaptrisMessageWorker#setMessageFactory(AdaptrisMessageFactory)
   */
  @Override
  public void setMessageFactory(AdaptrisMessageFactory f) {
    getConsumer().setMessageFactory(f);
  }

  public void changeState(ComponentState newState) {
    consumerState = newState;
  }

  public ComponentState retrieveComponentState() {
    return consumerState;
  }

  public void requestInit() throws CoreException {
    consumerState.requestInit(this);
  }

  public void requestStart() throws CoreException {
    consumerState.requestStart(this);
  }

  public void requestStop() {
    consumerState.requestStop(this);
  }

  public void requestClose() {
    consumerState.requestClose(this);
  }

}
