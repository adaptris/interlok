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
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.util.Args;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>
 * JMS Publish-and-subscribe implementation of <code>AdaptrisMessageConsumer</code>.
 * </p>
 *
 * @config jms-topic-consumer
 *
 */
@XStreamAlias("jms-topic-consumer")
@AdapterComponent
@ComponentProfile(summary = "Listen for JMS messages on the specified topic", tag = "consumer,jms",
recommended = {JmsConnection.class})
@DisplayOrder(order = {"topic", "messageSelector", "destination", "durable",
    "subscriptionId", "acknowledgeMode", "messageTranslator"})
@NoArgsConstructor
public class PasConsumer extends JmsConsumerImpl {

  /**
   * Should we be a durable subscriber.
   *
   */
  @InputFieldDefault(value = "false")
  @Deprecated
  @ConfigDeprecated(removalVersion = "4.0.0", message = "Durable subscriptions will be implied if subscription-id is not empty", groups = Deprecated.class)
  @Getter
  @Setter
  private Boolean durable;

  /**
   * Our subscription ID.
   * <p>
   * Sets the subscription ID to use for durable subscriptions. This must remain constant for the
   * same durable subscription to be accessed each time messages are consumed.
   * </p>
   */
  @Getter
  @Setter
  private String subscriptionId;

  /**
   * The JMS Topic
   *
   */
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String topic;

  private transient boolean durableWarningLogged = false;


  protected String subscriptionId() {
    if (durable()) {
      return Args.notBlank(getSubscriptionId(), "subscriptionId");
    }
    // Should just return getSubscriptionId() once durable is removed.
    return null;
  }

  @Override
  public void prepare() throws CoreException {
    super.prepare();
    if (getDurable() != null) {
      LoggingHelper.logWarning(durableWarningLogged, () -> durableWarningLogged = true,
          "{} uses 'durable', this will be implied if subscription-id is not blank",
          LoggingHelper.friendlyName(this));

    }
  }

  @Override
  protected String configuredEndpoint() {
    return getTopic();
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException, CoreException {
    VendorImplementation jmsImpl =
        retrieveConnection(JmsConnection.class).configuredVendorImplementation();
    return jmsImpl.createTopicSubscriber(
        endpoint(), messageSelector(), subscriptionId(), this);
  }

  public PasConsumer withTopic(String t) {
    setTopic(t);
    return this;
  }

  boolean durable() {
    return BooleanUtils.or(new boolean[] {
        BooleanUtils.toBooleanDefaultIfNull(getDurable(), false),
        !StringUtils.isBlank(getSubscriptionId())
    });
  }
}
