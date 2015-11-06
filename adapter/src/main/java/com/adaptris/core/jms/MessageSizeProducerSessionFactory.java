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
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProducerSessionFactory} that creates a new session/producer based on message size.
 * 
 * <p>
 * This implementaton refreshes the session based on the total size of the messages produced.
 * </p>
 * 
 * @config jms-message-size-producer-session
 * @license STANDARD
 * @author lchan
 * 
 */
@XStreamAlias("jms-message-size-producer-session")
public class MessageSizeProducerSessionFactory extends ProducerSessionFactoryImpl {

  private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024;

  private Long maxSizeBytes;

  private transient long currentMsgSize;

  public MessageSizeProducerSessionFactory() {
    super();
  }

  public MessageSizeProducerSessionFactory(Long max) {
    super();
    setMaxSizeBytes(max);
  }


  @Override
  public ProducerSession createProducerSession(JmsProducerImpl producer, AdaptrisMessage msg)
      throws JMSException {
    if (newSessionRequired() || session == null) {
      if (newSessionRequired()) log.trace("Message Size {} exceeds {}", currentMsgSize, maxSizeBytes());
      closeQuietly(session);
      session = createProducerSession(producer);
      currentMsgSize = msg.getSize();
    }
    else {
      currentMsgSize += msg.getSize();
    }
    return session;
  }

  boolean newSessionRequired() {
    return currentMsgSize > maxSizeBytes();
  }

  @Override
  public void init() throws CoreException {
    super.init();
    currentMsgSize = 0;
  }

  public Long getMaxSizeBytes() {
    return maxSizeBytes;
  }

  /**
   * Set the maximum accumulated size of messages before a session refresh is required.
   * 
   * @param max the max size of messages; if not specified, defaults to 10 megabytes.
   */
  public void setMaxSizeBytes(Long max) {
    this.maxSizeBytes = max;
  }

  long maxSizeBytes() {
    return getMaxSizeBytes() != null ? getMaxSizeBytes().longValue() : DEFAULT_MAX_SIZE;
  }

}
