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

import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsConfig.MESSAGE_TRANSLATOR_LIST;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase.assertMessages;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.jms.MessageConsumer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedArtemis;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.jms.JmsConfig;
import com.adaptris.interlok.util.Closer;

public class JmsConsumerTest extends com.adaptris.interlok.junit.scaffolding.jms.JmsConsumerCase {

  private static EmbeddedArtemis activeMqBroker;
  
  @Mock private BasicActiveMqImplementation mockVendor;
  @Mock MessageConsumer mockMessageConsumer;

  private AutoCloseable openMocks;
  @BeforeEach
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }
  
  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedArtemis();
    activeMqBroker.start();
  }
  
  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }

  @Test
  public void testDeferConsumerCreationToVendor() throws Exception {
    Assumptions.assumeTrue(JmsConfig.jmsTestsEnabled());

    when(mockVendor.createConsumer(any(), any(), any(JmsActorConfig.class))).thenReturn(mockMessageConsumer);

    when(mockVendor.getBrokerUrl())
        .thenReturn("vm://" + activeMqBroker.getName());

    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://" + activeMqBroker.getName());
    when(mockVendor.createConnectionFactory()).thenReturn(factory);
    when(mockVendor.createConnection(any(), any())).thenReturn(factory.createConnection());

    String rfc6167 = "jms:topic:" + getName() + "?subscriptionId=" + getName();

    JmsConsumer consumer = new JmsConsumer().withEndpoint(rfc6167);
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
  }

  @Test
  public void testDefaultFalseDeferConsumerCreationToVendor() throws Exception {
    Assumptions.assumeTrue(JmsConfig.jmsTestsEnabled());

    when(mockVendor.createConsumer(any(JmsDestination.class), any(String.class), any(JmsActorConfig.class)))
        .thenReturn(mockMessageConsumer);

    when(mockVendor.getBrokerUrl())
        .thenReturn("vm://" + activeMqBroker.getName());

    when(mockVendor.createConnectionFactory())
        .thenReturn(new ActiveMQConnectionFactory("vm://" + activeMqBroker.getName()));

    String rfc6167 = "jms:topic:" + getName() + "?subscriptionId=" + getName();

    JmsConsumer consumer = new JmsConsumer().withEndpoint(rfc6167);
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
  }

  @Test
  public void testDurableTopicConsume() throws Exception {
    Assumptions.assumeTrue(JmsConfig.jmsTestsEnabled());

    String rfc6167 = "jms:topic:" + getName() + "?subscriptionId=" + getName();

    JmsConsumer consumer = new JmsConsumer().withEndpoint(rfc6167);
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);

    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), new JmsProducer().withEndpoint(rfc6167));
    // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
    LifecycleHelper.prepare(standaloneConsumer);
    LifecycleHelper.prepare(standaloneProducer);
    execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testSharedDurableTopicConsume() throws Exception {
    Assumptions.assumeTrue(JmsConfig.jmsTestsEnabled());

    String rfc6167 = "jms:topic:" + getName() + "?subscriptionId=MySubId&sharedConsumerId=" + getName();

    JmsConsumer consumer = new JmsConsumer().withEndpoint(rfc6167);
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);

    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(),
            new JmsProducer().withEndpoint(rfc6167));
    execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testSharedTopicConsume() throws Exception {
    Assumptions.assumeTrue(JmsConfig.jmsTestsEnabled());

    String rfc6167 = "jms:topic:" + getName() + "?sharedConsumerId=" + getName();

    JmsConsumer consumer = new JmsConsumer().withEndpoint(rfc6167);
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);

    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(),
            new JmsProducer().withEndpoint(rfc6167));
    execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
    assertMessages(jms, 1);

  }

  @Test
  public void testTopicConsume() throws Exception {
    Assumptions.assumeTrue(JmsConfig.jmsTestsEnabled());

    String rfc6167 = "jms:topic:" + getName();

    JmsConsumer consumer = new JmsConsumer().withEndpoint(rfc6167);;
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);

    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(),
            new JmsProducer().withEndpoint(rfc6167));
    execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testQueueConsume() throws Exception {
    Assumptions.assumeTrue(JmsConfig.jmsTestsEnabled());

    String rfc6167 = "jms:queue:" + getName();

    JmsConsumer consumer = new JmsConsumer().withEndpoint(rfc6167);
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);

    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer producer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(),
            new JmsProducer().withEndpoint(rfc6167));
    // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
    LifecycleHelper.prepare(standaloneConsumer);
    LifecycleHelper.prepare(producer);
    execute(standaloneConsumer, producer, createMessage(null), jms);
    assertMessages(jms, 1);
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
    String dest = "jms:topic:MyTopicName?subscriptionId=mySubscriptionId";
    if (destQueue) {
      dest = "jms:queue:MyQueueName";
    }
    JmsConsumer pc = new JmsConsumer().withEndpoint(dest);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    StandaloneConsumer result = new StandaloneConsumer(c, pc);
    return result;
  }
}
