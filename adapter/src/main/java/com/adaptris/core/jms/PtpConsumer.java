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
import javax.jms.MessageConsumer;

import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * JMS Queue implementation of {@link AdaptrisMessageConsumer}
 * </p>
 * 
 * @config jms-queue-consumer
 * @license BASIC, additional license requirements from the chosen MessageTypeTranslator
 */
@XStreamAlias("jms-queue-consumer")
public class PtpConsumer extends JmsConsumerImpl {

  public PtpConsumer() {
    super();
  }

  PtpConsumer(boolean b) {
    super(b);
  }

  public PtpConsumer(ConsumeDestination d) {
    super(d);
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException, CoreException {
    return retrieveConnection(JmsConnection.class).configuredVendorImplementation().createQueueReceiver(getDestination(), this);
  }
}
