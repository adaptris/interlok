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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.validation.Valid;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.annotation.Removal;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Periodically sends a template message to trigger a {@link Workflow}
 * </p>
 * 
 * @config polling-trigger
 * 
 * 
 */
@XStreamAlias("polling-trigger")
@AdapterComponent
@ComponentProfile(summary = "Generate a static trigger message on a schedule", tag = "consumer,base",
    recommended = {NullConnection.class})
@DisplayOrder(order = {"poller", "messageProvider"})
public class PollingTrigger extends AdaptrisPollingConsumer {

  @MarshallingCDATA
  @Deprecated
  @Removal(version = "3.9.0", message = "Use a message-provider")
  private String template;
  @Valid
  private MessageProvider messageProvider;

  private static final ConfiguredConsumeDestination DEFAULT_DEST = new ConfiguredConsumeDestination(
      "PollingTrigger");

  public PollingTrigger() {

  }

  @Deprecated
  @Removal(version = "3.9.0")
  public PollingTrigger(Poller p, String t) {
    this();
    setPoller(p);
    setTemplate(t);
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
    if (getMessageProvider() == null) {
      if (!isEmpty(getTemplate())) {
        log.warn("template is deprecated; use a message-provider instead");
      }
      setMessageProvider(new StaticPollingTemplate(getTemplate()));
    }
    LifecycleHelper.prepare(getMessageProvider());
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
      AdaptrisMessage msg = getMessageProvider().createMessage(defaultIfNull(getMessageFactory()));
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
    LifecycleHelper.init(getMessageProvider());
    LifecycleHelper.init(getPoller());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getMessageProvider());
    super.start();
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getMessageProvider());
    super.stop();
  }

  @Override
  public void close() {
    LifecycleHelper.close(getMessageProvider());
    super.close();
  }

  /**
   * <p>
   * Sets the template message to use.
   * </p>
   *
   * @param s the template message to use
   * @deprecated since 3.6.2 use {@link #setMessageProvider(MessageProvider)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use a message-provider")
  public void setTemplate(String s) {
    template = s;
  }

  /**
   * <p>
   * Returns the template message to use.
   * </p>
   *
   * @return the template message to use
   * @deprecated since 3.6.2 use {@link #getMessageProvider()} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use a message-provider")
  public String getTemplate() {
    return template;
  }

  @Override
  protected String renameThread() {
    String oldName = Thread.currentThread().getName();
    String newName = DEFAULT_DEST.getDeliveryThreadName();
    if (super.getDestination() != null) {
      newName = super.getDestination().getDeliveryThreadName();
    }
    if (isEmpty(newName)) {
      newName = retrieveAdaptrisMessageListener().friendlyName();
    }
    
    Thread.currentThread().setName(newName);
    return oldName;
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
    this.messageProvider = p;
  }

  /**
   * How to generate the template that will be sent to the workflow.
   * 
   * @author lchan
   *
   */
  public interface MessageProvider extends ComponentLifecycle {

    AdaptrisMessage createMessage(AdaptrisMessageFactory fac) throws CoreException;
  }

}
