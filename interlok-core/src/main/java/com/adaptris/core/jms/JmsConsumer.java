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

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

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
 * <p>
 * Also supported is the JMS 2.0 sharedConsumerId, should you wish to create a multiple load balancing consumers on a single topic endpoint;
 * {@code jms:topic:MyTopicName?sharedConsumerId=12345}
 * </p>
 * For instance you could have the following destinations:
 * <ul>
 * <li>jms:queue:MyQueueName</li>
 * <li>jms:topic:MyTopicName</li>
 * <li>jms:topic:MyTopicName?subscriptionId=mySubscriptionId</li>
 * <li>jms:topic:MyTopicName?sharedConsumerId=mySharedConsumerId</li>
 * <li>jms:topic:MyTopicName?subscriptionId=mySubscriptionId&amp;sharedConsumerId=mySharedConsumerId</li>
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
@DisplayOrder(
    order = {"endpoint", "messageSelector", "destination", "acknowledgeMode",
    "messageTranslator"})
public class JmsConsumer extends JmsConsumerImpl {

  /**
   * Set to true if you wish to let the JMS message consumer be delegated by the configured vendor
   * implementation.
   * <p>
   * The default is false such that we use standard JMS 1.1/2.0 methods to create the appropriate
   * consumers.
   * </p>
   */
  @AdvancedConfig(rare = true)
  @AutoPopulated
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean deferConsumerCreationToVendor;

  /**
   * The RFC6167 format topic/queue.
   *
   */
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String endpoint;

  public JmsConsumer() {
  }

  // Here for test purposes.
  JmsConsumer(boolean transacted) {
    super(transacted);
  }

  public JmsConsumer withEndpoint(String s) {
    setEndpoint(s);
    return this;
  }

  @Override
  protected String configuredEndpoint() {
    return getEndpoint();
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException, CoreException {
    String rfc6167 = endpoint();
    String filterExp = messageSelector();

    VendorImplementation vendor = retrieveConnection(JmsConnection.class).configuredVendorImplementation();
    return new JmsMessageConsumerFactory(vendor, currentSession(), rfc6167, deferConsumerCreationToVendor(), filterExp,
        this).create();
  }

  protected Boolean deferConsumerCreationToVendor() {
    return BooleanUtils.toBooleanDefaultIfNull(getDeferConsumerCreationToVendor(), false);
  }

}
