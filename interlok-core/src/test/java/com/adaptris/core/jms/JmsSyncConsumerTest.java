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

import static com.adaptris.interlok.junit.scaffolding.jms.JmsConfig.MESSAGE_TRANSLATOR_LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jms.MessageConsumer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.jms.JmsConfig;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.TimeInterval;

public class JmsSyncConsumerTest extends PollingJmsConsumerCase {

  @Mock
  private BasicActiveMqImplementation mockVendor;
  @Mock
  private MessageConsumer mockMessageConsumer;

  private AutoCloseable openMocks;
  
  private static TestJmsBroker activeMqBroker;

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = JmsConfig.jmsTestsEnabled() ? new EmbeddedActiveMq() : new MockitoBroker();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }

  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  @Override
  protected List<?> retrieveObjectsForSampleConfig() {
    JmsConnection connection = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    ArrayList<StandaloneConsumer> result = new ArrayList<>();
    boolean useQueue = true;
    for (MessageTypeTranslator t : MESSAGE_TRANSLATOR_LIST) {
      StandaloneConsumer p = createStandaloneConsumer(connection, useQueue, false);
      ((JmsSyncConsumer) p.getConsumer()).setMessageTranslator(t);
      result.add(p);
      useQueue = !useQueue;
    }
    return result;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  private StandaloneConsumer createStandaloneConsumer(AdaptrisConnection connection, boolean destQueue,
      boolean deferConsumerCreationToVendor) {
    JmsSyncConsumer consumer = createConsumer(destQueue, deferConsumerCreationToVendor);
    return new StandaloneConsumer(connection, consumer);
  }

  @Override
  protected JmsSyncConsumer createConsumer() {
    return createConsumer(false, false);
  }

  protected JmsSyncConsumer createConsumer(boolean destQueue, boolean deferConsumerCreationToVendor) {
    JmsSyncConsumer consumer = new JmsSyncConsumer();
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)));
    consumer.setReacquireLockBetweenMessages(true);
    String dest = "jms:topic:MyTopicName?subscriptionId=mySubscriptionId";
    if (destQueue) {
      dest = "jms:queue:MyQueueName";
    }
    consumer.setEndpoint(dest);
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    consumer.setDeferConsumerCreationToVendor(deferConsumerCreationToVendor);

    return consumer;
  }

  @Test
  public void testDeferConsumerCreationToVendor() throws Exception {
    when(mockVendor.createConsumer(any(), any(), any(JmsActorConfig.class))).thenReturn(mockMessageConsumer);

    when(mockVendor.getBrokerUrl()).thenReturn("vm://" + activeMqBroker.getName());

    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://" + activeMqBroker.getName());
    when(mockVendor.createConnectionFactory()).thenReturn(factory);
    when(mockVendor.createConnection(any(), any())).thenReturn(factory.createConnection());

    JmsConnection jmsConnection = activeMqBroker.getJmsConnection();
    jmsConnection.setVendorImplementation(mockVendor);

    StandaloneConsumer standaloneConsumer = createStandaloneConsumer(jmsConnection, false, true);

    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    LifecycleHelper.initAndStart(standaloneConsumer);

    verify(mockVendor).createConsumer(any(), any(), any(JmsSyncConsumer.class));

    LifecycleHelper.stopAndClose(standaloneConsumer);
  }

  @Test
  public void testDefaultFalseDeferConsumerCreationToVendor() throws Exception {
    JmsConnection jmsConnection = activeMqBroker.getJmsConnection();

    StandaloneConsumer standaloneConsumer = createStandaloneConsumer(jmsConnection, false, false);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    try {
      LifecycleHelper.initAndStart(standaloneConsumer);
    } catch (Exception ex) {
    }

    verify(mockVendor, times(0)).createConsumer(any(JmsDestination.class), any(String.class), any(JmsActorConfig.class));

    LifecycleHelper.stopAndClose(standaloneConsumer);
  }

}
