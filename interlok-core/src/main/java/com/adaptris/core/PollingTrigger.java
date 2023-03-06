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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.Valid;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

/**
 * <p>
 * Periodically sends a template message to trigger a {@link Workflow}
 * </p>
 *
 * @config polling-trigger
 *
 *
 */
@JacksonXmlRootElement(localName = "polling-trigger")
@XStreamAlias("polling-trigger")
@AdapterComponent
@ComponentProfile(summary = "Generate a static trigger message on a schedule", tag = "consumer,base",
recommended = {NullConnection.class})
@DisplayOrder(order = {"poller", "messageProvider"})
public class PollingTrigger extends AdaptrisPollingConsumer {

  private static MessageProvider DEFAULT_MSG_PROVIDER = (factory) ->  {
    return factory.newMessage();
  };

  @Valid
  @InputFieldDefault(value = "empty message")
  private MessageProvider messageProvider;

  public PollingTrigger() {

  }

  public PollingTrigger(Poller p, MessageProvider t) {
    this();
    setPoller(p);
    setMessageProvider(t);
  }

  public PollingTrigger(Poller p) {
    this();
    setPoller(p);
  }

  @Override
  protected void prepareConsumer() throws CoreException {
    LifecycleHelper.prepare(messageProvider());
  }

  /**
   * <p>
   * Sends a new <code>AdaptrisMessage</code> with the configured template as
   * its payload using the configured producer.
   * </p>
   *
   * @return 1 as a single message is always produced
   */
  @Override
  protected int processMessages() {
    int count = 0;
    try {
      AdaptrisMessage msg = messageProvider().createMessage(defaultIfNull(getMessageFactory()));
      retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
      count = 1;
    }
    catch (Exception e) {
      log.warn("Failed to create trigger message; next attempt on next poll");
      log.trace(e.getMessage(), e);
    }
    return count;
  }

  @Override
  public void init() throws CoreException {
    getPoller().registerConsumer(this);
    LifecycleHelper.init(messageProvider());
    LifecycleHelper.init(getPoller());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(messageProvider());
    super.start();
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(messageProvider());
    super.stop();
  }

  @Override
  public void close() {
    LifecycleHelper.close(getMessageProvider());
    super.close();
  }

  /**
   * @return the templateProvider
   */
  public MessageProvider getMessageProvider() {
    return messageProvider;
  }

  /**
   * @param p the templateProvider to set
   */
  public void setMessageProvider(MessageProvider p) {
    messageProvider = Args.notNull(p, "messageProvider");
  }

  MessageProvider messageProvider() {
    return ObjectUtils.defaultIfNull(getMessageProvider(), DEFAULT_MSG_PROVIDER);
  }
  /**
   * How to generate the template that will be sent to the workflow.
   *
   */
  @FunctionalInterface
  public interface MessageProvider extends ComponentLifecycle {

    AdaptrisMessage createMessage(AdaptrisMessageFactory fac) throws CoreException;
  }

}
