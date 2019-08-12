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

import static org.apache.commons.lang3.StringUtils.isEmpty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Wrapper for a <code>AdaptrisConnection</code> and a <code>AdaptrisMessageProducer</code> for convenience. Also implements
 * <code>Service</code> for use in <code>ServiceCollection</code>s.
 * </p>
 * 
 * @config standalone-producer
 * 
 * @see AdaptrisMessageSender
 */
@XStreamAlias("standalone-producer")
@AdapterComponent
@ComponentProfile(summary = "Produce a message where as part of a service chain", tag = "service")
@DisplayOrder(order = {"connection", "producer"})
public class StandaloneProducer extends ServiceImp implements AdaptrisMessageSender, ConnectedService {

  @Valid
  @NotNull
  @AutoPopulated
  private AdaptrisConnection connection;
  @Valid
  @NotNull
  @AutoPopulated
  private AdaptrisMessageProducer producer;

  /**
   * <p>
   * Creates a new instance. Defaults to null connection / producer.
   * </p>
   */
  public StandaloneProducer() {
    this(new NullConnection(), new NullMessageProducer());
  }

  public StandaloneProducer(AdaptrisMessageProducer p) {
    this(new NullConnection(), p);
  }

  public StandaloneProducer(AdaptrisConnection c, AdaptrisMessageProducer p) {
    setConnection(c);
    setProducer(p);
    changeState(ClosedState.getInstance());
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    getProducer().produce(msg);
  }


  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    getProducer().produce(msg, dest);
  }

  @Override
  protected void initService() throws CoreException {
    connection.addExceptionListener(this);
    connection.addMessageProducer(producer);
    LifecycleHelper.init(connection);
    LifecycleHelper.init(producer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(connection);
    LifecycleHelper.start(producer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    LifecycleHelper.stop(producer);
    LifecycleHelper.stop(connection);

  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(producer);
    LifecycleHelper.close(connection);
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getConnection());
    LifecycleHelper.prepare(getProducer());
  }


  // sets and gets...

  /**
   * <p>
   * Returns the connection to use.
   * </p>
   *
   * @return the connection to use
   */
  @Override
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * <p>
   * Returns the producer to use.
   * </p>
   *
   * @return the producer to use
   */
  public AdaptrisMessageProducer getProducer() {
    return producer;
  }

  /**
   * <p>
   * Sets the connection to use, may not be null.
   * </p>
   *
   * @param conn the connection to use, may not be null
   */
  @Override
  public void setConnection(AdaptrisConnection conn) {
    connection = Args.notNull(conn, "connection");
  }

  /**
   * <p>
   * Sets the producer to use, may not be null.
   * </p>
   *
   * @param prod the producer to use, may not be null
   */
  public void setProducer(AdaptrisMessageProducer prod) {
    producer = Args.notNull(prod, "producer");
  }

  /**
   * @see com.adaptris.core.Service #doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      this.produce(msg);
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public String createName() {
    String name = getProducer().createName();
    return isEmpty(name) ? super.createName() : name;
  }

  @Override
  public String createQualifier() {
    String qualifier = getProducer().createQualifier();
    return isEmpty(qualifier) ? super.createQualifier() : qualifier;
  }

}
