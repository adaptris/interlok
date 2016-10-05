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

package com.adaptris.core.services.splitter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Splits incoming {@link com.adaptris.core.AdaptrisMessage}s into several <code>AdaptrisMessage</code>s using an implementation of
 * {@link MessageSplitter}.
 * </p>
 * <p>
 * This implementation simply uses the configured producer and connection to produce the split message.
 * </p>
 * 
 * @config basic-message-splitter-service
 * 
 * 
 */
@XStreamAlias("basic-message-splitter-service")
@AdapterComponent
@ComponentProfile(summary = "Split a message and produce each split message somewhere",
    tag = "service,splitter")
@DisplayOrder(order = {"splitter", "connection", "producer", "ignoreSplitMessageFailures"})
public class BasicMessageSplitterService extends MessageSplitterServiceImp {

  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisConnection connection;
  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisMessageProducer producer;

  /**
   * <p>
   * Creates a new instance. Defaults to copying all metadata from the original
   * message to the new, split messages.
   * </p>
   */
  public BasicMessageSplitterService() {
    super();
    setConnection(new NullConnection());
    setProducer(new NullMessageProducer());
  }

  /**
   *
   * @see MessageSplitterServiceImp#handleSplitMessage(AdaptrisMessage)
   */
  @Override
  protected void handleSplitMessage(AdaptrisMessage msg) throws ServiceException {
    try {
      producer.produce(msg);
    } catch (ProduceException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {
    super.initService();
    connection.addExceptionListener(this); // back ref
    connection.addMessageProducer(producer);
    LifecycleHelper.init(connection);
    LifecycleHelper.init(producer);
  }

  @Override
  protected void closeService() {
    LifecycleHelper.stop(producer);
    LifecycleHelper.stop(connection);
    super.closeService();
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(producer);
    LifecycleHelper.start(connection);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    LifecycleHelper.stop(producer);
    LifecycleHelper.stop(connection);
  }

  /**
   * <p>
   * Sets the <code>AdaptrisConnection</code> to use for producing split
   * messages.
   * </p>
   *
   * @param conn the <code>AdaptrisConnection</code> to use for producing split
   *          messages, may not be null
   */
  public void setConnection(AdaptrisConnection conn) {
    connection = Args.notNull(conn, "connection");
  }

  /**
   * <p>
   * Returns the <code>AdaptrisConnection</code> to use for producing split
   * messages.
   * </p>
   *
   * @return the <code>AdaptrisConnection</code> to use for producing split
   *         messages
   */
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * <p>
   * Sets the <code>AdaptrisMessageProducer</code> to use for producing split
   * messages.
   * </p>
   *
   * @param prod the <code>AdaptrisMessageProducer</code> to use for producing
   *          split messages, may not be null
   */
  public void setProducer(AdaptrisMessageProducer prod) {
    producer = Args.notNull(prod, "producer");
  }

  /**
   * <p>
   * Returns the <code>AdaptrisMessageProducer</code> to use for producing split
   * messages.
   * </p>
   *
   * @return the <code>AdaptrisMessageProducer</code> to use for producing split
   *         messages
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
