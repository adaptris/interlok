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

import static com.adaptris.core.util.ServiceUtil.discardNulls;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceWrapper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Splits incoming {@link com.adaptris.core.AdaptrisMessage}s into several using an implementation of {@link MessageSplitter}.
 * </p>
 * <p>
 * Rather than directly producing the message to a producer, this allows the use of a {@link com.adaptris.core.ServiceCollection} as the target for
 * the resulting split messages.
 * </p>
 * 
 * @config advanced-message-splitter-service
 * 
 * 
 */
@XStreamAlias("advanced-message-splitter-service")
@AdapterComponent
@ComponentProfile(summary = "Split a message and execute an arbitary number of services on the split message",
    tag = "service,splitter")
@DisplayOrder(order = {"splitter", "service", "ignoreSplitMessageFailures", "sendEvents"})
public class AdvancedMessageSplitterService extends MessageSplitterServiceImp implements EventHandlerAware, ServiceWrapper {

  @NotNull
  @AutoPopulated
  @Valid
  private Service service;
  private transient EventHandler eventHandler;
  @InputFieldDefault(value = "false")
  private Boolean sendEvents;

  /**
   * <p>
   * Creates a new instance. Defaults to copying all metadata from the original
   * message to the new, split messages.
   * </p>
   */
  public AdvancedMessageSplitterService() {
    super();
    setService(new NullService());
  }

  /**
   *
   * @see com.adaptris.core.services.splitter.MessageSplitterServiceImp#handleSplitMessage(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void handleSplitMessage(AdaptrisMessage msg) throws ServiceException {
    try {
      service.doService(msg);
    }
    finally {
      if (eventHandler != null && sendEvents()) {
        try {
          eventHandler.send(msg.getMessageLifecycleEvent());
        }
        catch (CoreException e) {
          throw new ServiceException(e);
        }
      }
    }
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.registerEventHandler(service, eventHandler);
    super.initService();
    LifecycleHelper.init(service);
  }

  @Override
  protected void closeService() {
    LifecycleHelper.stop(service);
    super.closeService();
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(service);
    super.start();

  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    LifecycleHelper.stop(service);
    super.stop();
  }

  /**
   * @return the serviceList
   */
  public Service getService() {
    return service;
  }

  /**
   * @param sc the serviceList to set
   */
  public void setService(Service sc) {
    service = Args.notNull(sc, "service");
  }

  /**
   * @return the sendEvents
   */
  public Boolean getSendEvents() {
    return sendEvents;
  }

  public boolean sendEvents() {
    return getSendEvents() != null ? getSendEvents().booleanValue() : false;
  }

  /**
   * Whether or not to send events for the message that has been split.
   * <p>
   * Note that even if this is set to true, because each child message has its
   * own unique id, you will have to externally correlate the message lifecycle
   * events together. Child messages will always have the metadata
   * {@link com.adaptris.core.CoreConstants#PARENT_UNIQUE_ID_KEY} set with the originating message
   * id.
   * </p>
   *
   * @param b true to send messages (default false)
   */
  public void setSendEvents(Boolean b) {
    sendEvents = b;
  }

  /**
   * @see com.adaptris.core.EventHandlerAware#registerEventHandler(com.adaptris.core.EventHandler)
   */
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getService());
  }

  @Override
  public Service[] wrappedServices() {
    return discardNulls(getService());
  }
}
