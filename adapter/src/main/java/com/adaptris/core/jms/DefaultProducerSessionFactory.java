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
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default implementation of {@link ProducerSessionFactory}.
 * 
 * <p>
 * The default implementation creates a single session, and reuses that until restarted.
 * </p>
 * 
 * @config jms-default-producer-session
 * @license BASIC
 * @author lchan
 * 
 */
@XStreamAlias("jms-default-producer-session")
public class DefaultProducerSessionFactory extends ProducerSessionFactoryImpl {

  public DefaultProducerSessionFactory() {
    super();
  }

  @Override
  public ProducerSession createProducerSession(JmsProducerImpl producer, AdaptrisMessage msg)
      throws JMSException {
    if (session == null) {
      session = createProducerSession(producer);
    }
    return session;
  }

}
