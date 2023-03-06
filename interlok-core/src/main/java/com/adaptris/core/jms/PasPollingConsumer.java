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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
* <p>
* PAS implementation of <code>JmsPollingConsumer</code>. The consumer created by this class is always durable and thus requires the
* clientID and subscriptionId to be set. It is up to the user to ensure that these are set such that this consumer is uniquely
* identified in the context of the broker's other consumers.
* </p>
*
* @config jms-topic-poller
*
*/
@JacksonXmlRootElement(localName = "jms-topic-poller")
@XStreamAlias("jms-topic-poller")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from a JMS Topic by actively polling for them", tag = "consumer,jms",
recommended = {NullConnection.class})
@DisplayOrder(order = {"topic", "messageSelector", "poller", "vendorImplementation",
"userName", "password", "clientId", "subscriptionId", "acknowledgeMode",
"messageTranslator"})
public class PasPollingConsumer extends JmsPollingConsumerImpl {

/**
* The JMS Topic
*
*/
@Getter
@Setter
@NotBlank
private String topic;

@NotNull
@NotBlank
private String subscriptionId;

public PasPollingConsumer() {
super();
}

@Override
protected String configuredEndpoint() {
return getTopic();
}

/** @see com.adaptris.core.AdaptrisComponent#init() */
@Override
public void init() throws CoreException {
try {
Args.notBlank(getSubscriptionId(), "subscriptionId");
Args.notBlank(getClientId(), "clientId");
log.trace("client ID [{}] subscription ID [{}]", getClientId(), getSubscriptionId());
super.init();
}
catch (Exception e) {
throw ExceptionHelper.wrapCoreException(e);
}
}

@Override
protected MessageConsumer createConsumer() throws JMSException {
return getVendorImplementation().createTopicSubscriber(getTopic(), getMessageSelector(), getSubscriptionId(), this);
}

public PasPollingConsumer withTopic(String s) {
setTopic(s);
return this;
}

/**
* <p>
* Returns the subscription ID to use.
* </p>
*
* @return subscriptionId the subscription ID to use
*/
public String getSubscriptionId() {
return subscriptionId;
}

/**
* <p>
* Sets the subscription ID to use. This, in combination with the clientId
* should uniquely identify this subscription in the context of the broker.
* </p>
*
* @param s the subscription ID to use
* @see JmsPollingConsumerImpl
*/
public void setSubscriptionId(String s) {
subscriptionId = Args.notBlank(s, "subscriptionId");
}
}
