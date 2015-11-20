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

import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * JMS Publish-and-subscribe implementation of <code>AdaptrisMessageConsumer</code>.
 * </p>
 * 
 * @config jms-topic-consumer
 * 
 */
@XStreamAlias("jms-topic-consumer")
public class PasConsumer extends JmsConsumerImpl {

  private Boolean durable; // defaults to false
  private String subscriptionId;

  public PasConsumer() {
    super();
  }

  PasConsumer(boolean b) {
    super(b);
  }

  public PasConsumer(ConsumeDestination d) {
    super(d);
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException, CoreException {
    if (durable() && isEmpty(subscriptionId)) {
        throw new CoreException(
            "trying to create durable subscription with null subscription id");
    }
    MessageConsumer consumer = retrieveConnection(JmsConnection.class).configuredVendorImplementation().createTopicSubscriber(
        getDestination(), durable() ? subscriptionId : null, this);
    return consumer;
  }


  /**
   * <p>
   * Sets whether this consumer is durable.
   * </p>
   *
   * @param b whether this consumer is durable
   */
  public void setDurable(Boolean b) {
    durable = b;
  }

  /**
   * <p>
   * Returns whether this consumer is durable.
   * </p>
   *
   * @return whether this consumer is durable
   */
  public Boolean getDurable() {
    return durable;
  }

  boolean durable() {
    return getDurable() != null ? getDurable().booleanValue() : false;
  }

  /**
   * <p>
   * Sets the subscription ID to use for durable subscriptions. This must remain
   * constant for the same durable subscription to be accessed each time
   * messages are consumed.
   * </p>
   *
   * @param s the subscription ID to use for durable subscriptions
   */
  public void setSubscriptionId(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException();
    }
    subscriptionId = s;
  }

  /**
   * <p>
   * Returns the subscription ID to use for durable subscriptions.
   * </p>
   *
   * @return the subscription ID to use for durable subscriptions
   */
  public String getSubscriptionId() {
    return subscriptionId;
  }

}
