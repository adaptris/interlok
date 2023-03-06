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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProducerSessionFactory} that creates a new session every time is produced.
 * 
 * 
 * @config jms-per-message-producer-session
 * 
 * @author lchan
 * 
 */
@JacksonXmlRootElement(localName = "jms-per-message-producer-session")
@XStreamAlias("jms-per-message-producer-session")
public class PerMessageProducerSessionFactory extends ProducerSessionFactoryImpl {

  public PerMessageProducerSessionFactory() {
    super();
  }

  @Override
  public ProducerSession createProducerSession(JmsProducerImpl producer, AdaptrisMessage msg)
      throws JMSException {
    closeQuietly(session);
    session = createProducerSession(producer);
    return session;
  }

}
