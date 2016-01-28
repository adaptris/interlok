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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Periodically sends a template message to trigger other <code>Workflow</code> s.
 * </p>
 * 
 * @config polling-trigger
 * 
 * 
 */
@XStreamAlias("polling-trigger")
@GenerateBeanInfo
@AdapterComponent
@ComponentProfile(summary = "Generate a static trigger message on a schedule", tag = "consumer,base")
public class PollingTrigger extends AdaptrisPollingConsumer {

	@MarshallingCDATA
  private String template;
  private static final ConfiguredConsumeDestination DEFAULT_DEST = new ConfiguredConsumeDestination(
      "PollingTrigger");


  @Override
  protected void prepareConsumer() throws CoreException {
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
    AdaptrisMessage msg;
    if (template != null) {
      msg = defaultIfNull(getMessageFactory()).newMessage(template);
    }
    else {
      msg = defaultIfNull(getMessageFactory()).newMessage(new byte[0]);
    }
    retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
    return 1;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisPollingConsumer#init()
   */
  @Override
  public void init() throws CoreException {
    // Unlike the normal AdaptrisPollingConsumer we don't care about the
    // consume Destination, only about the poller.
    getPoller().registerConsumer(this);
    LifecycleHelper.init(getPoller());
  }

  /**
   * <p>
   * Sets the template message to use.
   * </p>
   *
   * @param s the template message to use
   */
  public void setTemplate(String s) {
    if (s == null) {
      throw new IllegalArgumentException("param is null");
    }
    template = s;
  }

  /**
   * <p>
   * Returns the template message to use.
   * </p>
   *
   * @return the template message to use
   */
  public String getTemplate() {
    return template;
  }

  @Override
  protected String renameThread() {
    String oldName = Thread.currentThread().getName();

    StringBuffer newName = new StringBuffer();
    if (super.getDestination() != null) {
      newName.append(super.getDestination().getDeliveryThreadName());
    }
    else {
      newName.append(DEFAULT_DEST.getDeliveryThreadName());
    }
    Thread.currentThread().setName(newName.toString());
    return oldName;
  }
}
