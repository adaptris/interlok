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

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Topic;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsDestination.DestinationType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * JMS Consumer implementation that can target queues or topics via an RFC6167 style destination.
 * <p>
 * This differs from the standard {@link PtpConsumer} and {@link PasConsumer} in that it supports a destination that is specified in
 * RFC6167 style. For instance {@code jms:queue:myQueueName} will consume from a queue called {@code myQueueName} and
 * {@code jms:topic:myTopicName} from a topic called {@code myTopicName}
 * </p>
 * <p>
 * While RFC6167 defines the ability to use jndi to lookup the (as part of the 'jndi' variant section); this is not supported. There
 * is also support for {@code subscriptionId} which indicates the subscriptionId that should be used when attaching a subscriber to
 * a topic; {@code jms:topic:MyTopicName?subscriptionId=myId} would return a {@link JmsDestination#subscriptionId()} of
 * {@code myId}. If a subscription ID is not specified, then a durable subscriber is never created; specifying a subscription ID
 * automatically means a durable subscriber.
 * </p>
 * For instance you could have the following destinations:
 * <ul>
 * <li>jms:queue:MyQueueName</li>
 * <li>jms:topic:MyTopicName</li>
 * <li>jms:topic:MyTopicName?subscriptionId=mySubscriptionId</li>
 * </ul>
 * </p>
 * 
 * @config jms-consumer
 * 
 */
@XStreamAlias("jms-consumer")
@AdapterComponent
@ComponentProfile(summary = "Listen for JMS messages on the specified queue or topic", tag = "consumer,jms",
    recommended = {JmsConnection.class})
@DisplayOrder(order = {"destination", "acknowledgeMode", "messageTranslator"})
public class JmsConsumer extends JmsConsumerImpl {

  public JmsConsumer() {
  }

  // Here for test purposes.
  JmsConsumer(boolean transacted) {
    super(transacted);
  }

  public JmsConsumer(ConsumeDestination d) {
    this();
    setDestination(d);
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException, CoreException {
    String rfc6167 = getDestination().getDestination();
    String filterExp = getDestination().getFilterExpression();
    MessageConsumer consumer = null;
    
    VendorImplementation vendor = retrieveConnection(JmsConnection.class).configuredVendorImplementation();
    JmsDestination d = vendor.createDestination(rfc6167, this);
    if (d.destinationType().equals(DestinationType.TOPIC) && !isEmpty(d.subscriptionId())) {
      consumer = currentSession().createDurableSubscriber((Topic) d.getDestination(), d.subscriptionId(), filterExp, false);
    } else {
      consumer = currentSession().createConsumer(d.getDestination(), filterExp);
    }
    return consumer;
  }



}
