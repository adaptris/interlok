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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Basic implementation of <code>EventHandler</code>.
 * </p>
 * 
 * @config default-event-handler
 * 
 */
@XStreamAlias("default-event-handler")
@AdapterComponent
@ComponentProfile(summary = "Sends MessageLifecycleEvents to the specified location", tag = "base,events")
public class DefaultEventHandler extends EventHandlerBase {

  @NotNull
  @Valid
  @AutoPopulated
  private AdaptrisConnection connection; // used for consume and produce
  @NotNull
  @Valid
  @AutoPopulated
  private AdaptrisMessageProducer producer;

  public DefaultEventHandler() {
    this(new NullConnection(), new NullMessageProducer());
  }

  public DefaultEventHandler(AdaptrisMessageProducer producer) {
    this(new NullConnection(), producer);
  }

  public DefaultEventHandler(AdaptrisConnection connection, AdaptrisMessageProducer producer)  {
    setConnection(connection);
    setProducer(producer);
  }

  @Override
  protected AdaptrisMessageSender retrieveProducer() {
    return producer;
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  protected void eventHandlerInit() throws CoreException {
    connection.addExceptionListener(this);
    connection.addMessageProducer(producer);
    LifecycleHelper.init(connection);
    LifecycleHelper.init(producer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  protected void eventHandlerStart() throws CoreException {
    LifecycleHelper.start(producer);
    LifecycleHelper.start(connection);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  protected void eventHandlerStop() {
    LifecycleHelper.stop(connection);
    LifecycleHelper.stop(producer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  protected void eventHandlerClose() {
    LifecycleHelper.close(connection);
    LifecycleHelper.close(producer);
  }

  /**
   * <p>
   * Sets the <code>AdaptrisConnection</code> to use. May not be null.
   * </p>
   * 
   * @param c the <code>AdaptrisConnection</code> to use
   */
  public void setConnection(AdaptrisConnection c) {
    if (!retrieveComponentState().equals(ClosedState.getInstance())) {
      throw new IllegalStateException("Attempt to set the connection when already initialised");
    }
    connection = Args.notNull(c, "connection");
  }

  /**
   * <p>
   * Returns the <code>AdaptrisConnection</code> to use.
   * </p>
   * 
   * @return the <code>AdaptrisConnection</code> to use
   */
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * <p>
   * Sets the <code>AdaptrisMessageProducer</code> to use. May not be null.
   * </p>
   * 
   * @param p the <code>AdaptrisMessageProducer</code> to use
   */
  public void setProducer(AdaptrisMessageProducer p) {
    if (!retrieveComponentState().equals(ClosedState.getInstance())) {
      throw new IllegalStateException("Attempt to set the producer when already initialised");
    }
    producer = Args.notNull(p, "producer");
  }

  /**
   * <p>
   * Returns the <code>AdaptrisMessageProducer</code> to use.
   * </p>
   * 
   * @return the <code>AdaptrisMessageProducer</code> to use
   */
  public AdaptrisMessageProducer getProducer() {
    return producer;
  }

  @Override
  public void prepare() throws CoreException {
    getConnection().prepare();
    getProducer().prepare();
  }

}
