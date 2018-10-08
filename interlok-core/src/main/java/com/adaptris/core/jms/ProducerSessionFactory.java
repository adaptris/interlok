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
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;

/**
 * Handles the creation of a JMS Session and MessageProducer for {@link JmsProducerImpl} instances.
 * 
 * @author lchan
 * 
 */
public interface ProducerSessionFactory extends ComponentLifecycle, ComponentLifecycleExtension {

  /**
   * Create or reuse an existing session.
   * 
   * @param conn the {@link JmsProducerImpl} instance
   * @param msg the message that the producer is currently handling.
   * @return a {@link ProducerSession}
   * @throws JMSException if there was a problem creating the session.
   */
  ProducerSession createProducerSession(JmsProducerImpl conn, AdaptrisMessage msg)
      throws JMSException;

}
