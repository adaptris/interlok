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

import static com.adaptris.core.jms.JmsConfig.MESSAGE_TRANSLATOR_LIST;
import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import javax.jms.MessageConsumer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.jms.activemq.EmbeddedArtemis;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;

public class JmsConsumerTest extends JmsConsumerCase {

  @Mock private BasicActiveMqImplementation mockVendor;
  @Mock MessageConsumer mockMessageConsumer;

  @Override
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  public JmsConsumerTest(String name) {
    super(name);
  }


  public void testDeferConsumerCreationToVendor() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();

    when(mockVendor.createConsumer(any(), any(), any(JmsActorConfig.class))).thenReturn(mockMessageConsumer);

    when(mockVendor.getBrokerUrl())
        .thenReturn("vm://" + activeMqBroker.getName());

    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://" + activeMqBroker.getName());
    when(mockVendor.createConnectionFactory()).thenReturn(factory);
    when(mockVendor.createConnection(any(), any())).thenReturn(factory.createConnection());

    String rfc6167 = "jms:topic:" + getName() + "?subscriptionId=" + getName();

    try {
      activeMqBroker.start();

      JmsConsumer consumer = new JmsConsumer(new ConfiguredConsumeDestination(rfc6167));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setDeferConsumerCreationToVendor(true);

      JmsConnection jmsConnection = activeMqBroker.getJmsConnection();
      jmsConnection.setVendorImplementation(mockVendor);

      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(jmsConnection, consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      LifecycleHelper.initAndStart(standaloneConsumer);

      verify(mockVendor).createConsumer(any(), any(), any(JmsConsumer.class));

      LifecycleHelper.stopAndClose(standaloneConsumer);

    } finally {
      activeMqBroker.destroy();
    }
  }

  public void testDefaultFalseDeferConsumerCreationToVendor() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();

    when(mockVendor.createConsumer(any(JmsDestination.class), any(String.class), any(JmsActorConfig.class)))
        .thenReturn(mockMessageConsumer);

    when(mockVendor.getBrokerUrl())
        .thenReturn("vm://" + activeMqBroker.getName());

    when(mockVendor.createConnectionFactory())
        .thenReturn(new ActiveMQConnectionFactory("vm://" + activeMqBroker.getName()));

    String rfc6167 = "jms:topic:" + getName() + "?subscriptionId=" + getName();

    try {
      activeMqBroker.start();

      JmsConsumer consumer = new JmsConsumer(new ConfiguredConsumeDestination(rfc6167));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
//      consumer.setDeferConsumerCreationToVendor(true);

      JmsConnection jmsConnection = activeMqBroker.getJmsConnection();
      jmsConnection.setVendorImplementation(mockVendor);

      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(jmsConnection, consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      try {
        LifecycleHelper.initAndStart(standaloneConsumer);
      } catch (Exception ex) {}

      verify(mockVendor, times(0)).createConsumer(any(JmsDestination.class), any(String.class), any(JmsActorConfig.class));

      LifecycleHelper.stopAndClose(standaloneConsumer);

    } finally {
      activeMqBroker.destroy();
    }
  }

  public void testDurableTopicConsume() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String rfc6167 = "jms:topic:" + getName() + "?subscriptionId=" + getName();

    try {
      activeMqBroker.start();

      JmsConsumer consumer = new JmsConsumer(new ConfiguredConsumeDestination(rfc6167));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer =
          new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), new JmsProducer(
              new ConfiguredProduceDestination(rfc6167)));
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    } finally {
      activeMqBroker.destroy();
    }

  }

  public void testSharedDurableTopicConsume() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedArtemis activeMqBroker = new EmbeddedArtemis();
    String rfc6167 = "jms:topic:" + getName() + "?subscriptionId=MySubId&sharedConsumerId=" + getName();

    try {
      activeMqBroker.start();

      JmsConsumer consumer = new JmsConsumer(new ConfiguredConsumeDestination(rfc6167));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer =
          new StandaloneProducer(activeMqBroker.getJmsConnection(), new JmsProducer(
              new ConfiguredProduceDestination(rfc6167)));
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    } finally {
      activeMqBroker.destroy();
    }

  }

  public void testSharedTopicConsume() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedArtemis activeMqBroker = new EmbeddedArtemis();
    String rfc6167 = "jms:topic:" + getName() + "?sharedConsumerId=" + getName();

    try {
      activeMqBroker.start();

      JmsConsumer consumer = new JmsConsumer(new ConfiguredConsumeDestination(rfc6167));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer =
          new StandaloneProducer(activeMqBroker.getJmsConnection(), new JmsProducer(
              new ConfiguredProduceDestination(rfc6167)));
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    } finally {
      activeMqBroker.destroy();
    }

  }

  public void testTopicConsume() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String rfc6167 = "jms:topic:" + getName();

    try {
      activeMqBroker.start();

      JmsConsumer consumer = new JmsConsumer(new ConfiguredConsumeDestination(rfc6167));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer =
          new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), new JmsProducer(
              new ConfiguredProduceDestination(rfc6167)));
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    } finally {
      activeMqBroker.destroy();
    }

  }

  public void testQueueConsume() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String rfc6167 = "jms:queue:" + getName();

    try {
      activeMqBroker.start();
      JmsConsumer consumer = new JmsConsumer(new ConfiguredConsumeDestination(rfc6167));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer producer =
          new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), new JmsProducer(
              new ConfiguredProduceDestination(rfc6167)));
      execute(standaloneConsumer, producer, createMessage(null), jms);
      assertMessages(jms, 1);
    } finally {
      activeMqBroker.destroy();
    }
  }



  protected BasicActiveMqImplementation createVendorImpl() {
    return new BasicActiveMqImplementation();
  }


  @Override
  protected List<?> retrieveObjectsForSampleConfig() {
    ArrayList<StandaloneConsumer> result = new ArrayList<>();
    boolean useQueue = true;
    for (MessageTypeTranslator t : MESSAGE_TRANSLATOR_LIST) {
      StandaloneConsumer p = retrieveSampleConfig(useQueue);
      ((JmsConsumerImpl) p.getConsumer()).setMessageTranslator(t);
      result.add(p);
      useQueue = !useQueue;
    }
    return result;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String createBaseFileName(Object object) {
    JmsConsumerImpl p = (JmsConsumerImpl) ((StandaloneConsumer) object).getConsumer();
    return super.createBaseFileName(object) + "-" + p.getMessageTranslator().getClass().getSimpleName();
  }

  protected StandaloneConsumer retrieveSampleConfig(boolean destQueue) {
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    ConfiguredConsumeDestination dest = new ConfiguredConsumeDestination("jms:topic:MyTopicName?subscriptionId=mySubscriptionId");
    if (destQueue) {
      dest = new ConfiguredConsumeDestination("jms:queue:MyQueueName");
    }
    JmsConsumer pc = new JmsConsumer(dest);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    StandaloneConsumer result = new StandaloneConsumer(c, pc);
    return result;
  }
}
