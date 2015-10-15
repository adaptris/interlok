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

package com.adaptris.core.jms;

import javax.jms.JMSException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProducerSessionFactory} that creates a new session/producer based the number of messages.
 * 
 * <p>
 * This implementaton refreshes the session based on a count of the number of messages.
 * </p>
 * 
 * @config jms-message-count-producer-session
 * @license STANDARD
 * @author lchan
 * 
 */
@XStreamAlias("jms-message-count-producer-session")
public class MessageCountProducerSessionFactory extends ProducerSessionFactoryImpl {

  private static final int DEFAULT_MAX_MESSAGES = 1024;

  private Integer maxMessages;

  private transient int currentCount;

  public MessageCountProducerSessionFactory() {
    super();
  }

  public MessageCountProducerSessionFactory(Integer max) {
    super();
    setMaxMessages(max);
  }

  @Override
  public boolean isEnabled(License l) {
    return l.isEnabled(LicenseType.Standard);
  }

  @Override
  public void init() throws CoreException {
    super.init();
    currentCount = 0;
  }

  @Override
  public ProducerSession createProducerSession(JmsProducerImpl producer, AdaptrisMessage msg)
      throws JMSException {
    if (newSessionRequired() || session == null) {
      if (newSessionRequired()) log.trace("Message Count {} exceeds {}", currentCount, maxMessages());
      closeQuietly(session);
      session = createProducerSession(producer);
      currentCount = 1;
    }
    else {
      currentCount++;
    }
    return session;
  }

  boolean newSessionRequired() {
    return currentCount > maxMessages();
  }


  public Integer getMaxMessages() {
    return maxMessages;
  }

  /**
   * Set the maximum number of messages before a session refresh is required.
   * 
   * @param max the max number of messages; if not specified, defaults to 1024.
   */
  public void setMaxMessages(Integer max) {
    this.maxMessages = max;
  }

  int maxMessages() {
    return getMaxMessages() != null ? getMaxMessages().intValue() : DEFAULT_MAX_MESSAGES;
  }

}
