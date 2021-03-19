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

import static org.apache.commons.lang3.StringUtils.isEmpty;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Topic;
import javax.validation.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.NullConnection;
import com.adaptris.core.jms.JmsDestination.DestinationType;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Concrete {@link JmsPollingConsumerImpl} implementation that can target queues or topics via an
 * RFC6167 style destination.
 * <p>
 * This differs from the standard {@link PtpPollingConsumer} and {@link PasPollingConsumer} in that
 * it supports a destination that is specified in RFC6167 style. For instance
 * {@code jms:queue:myQueueName} will consume from a queue called {@code myQueueName} and
 * {@code jms:topic:myTopicName} from a topic called {@code myTopicName}
 * </p>
 * <p>
 * If you specify a topic as the destination then you should also configure
 * {@link #setClientId(String)}. There are also some custom parameters that are used as part of the
 * URI defining the topic.
 * <ul>
 * <li>{@code subscriptionId} - which indicates the subscriptionId that should be used when
 * attaching a subscriber to a topic; {@code jms:topic:MyTopicName?subscriptionId=myId} would return
 * a {@link JmsDestination#subscriptionId()} of {@code myId}. This must be specified.</li>
 * <li>{@code noLocal} - which corresponds to the
 * {@link javax.jms.Session#createConsumer(javax.jms.Destination, String, boolean)} noLocal setting.
 * This defaults to false, if not specified.</li>
 * <ul>
 * </p>
 * <p>
 * For instance you could have the following destinations:
 * <ul>
 * <li>jms:queue:MyQueueName</li>
 * <li>jms:topic:MyTopicName?subscriptionId=mySubscriptionId</li>
 * </ul>
 * </p>
 *
 * @config jms-poller
 *
 */
@XStreamAlias("jms-poller")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from a JMS broker (queue or topic) by actively polling it", tag = "consumer,jms",
    recommended = {NullConnection.class})
@DisplayOrder(
    order = {"endpoint", "messageSelector", "poller", "vendorImplementation", "userName",
        "password", "clientId", "acknowledgeMode", "messageTranslator"})
public class JmsPollingConsumer extends JmsPollingConsumerImpl {

  /**
   * The JMS destination in RFC6167 format.
   *
   */
  @Getter
  @Setter
  @NotBlank
  private String endpoint;

  public JmsPollingConsumer() {
    super();
  }

  public JmsPollingConsumer withEndpoint(String s) {
    setEndpoint(s);
    return this;
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException {
    String rfc6167 = getEndpoint();
    String filterExp = getMessageSelector();
    MessageConsumer consumer = null;

    VendorImplementation vendor = configuredVendorImplementation();
    JmsDestination d = vendor.createDestination(rfc6167, this);
    if (d.destinationType().equals(DestinationType.TOPIC)) {
      if (!isEmpty(d.subscriptionId()) && !isEmpty(getClientId())) {
        consumer = currentSession().createDurableSubscriber((Topic) d.getDestination(), d.subscriptionId(), filterExp, false);
      } else {
        throw new JMSException("Cannot create durable subscription clientId=[" + getClientId() + "], subscriptionId=["
            + d.subscriptionId() + "]");
      }
    } else {
      consumer = currentSession().createConsumer(d.getDestination(), filterExp);
    }
    return consumer;
  }

  @Override
  protected String configuredEndpoint() {
    return getEndpoint();
  }

}
