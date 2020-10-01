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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.jms.JmsDestination.DestinationType;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.interlok.util.Closer;

public class JmsMessageConsumerFactoryTest {

  private static final String SUBSCRIPTION_ID = "mySubscriptionId";
  private static final String SHARED_CONSUMER_ID = "mySharedConsumerId";
  private static final String RFC6167 = "jms:topic:myTopic";
  private static final String RFC6167_SUBSCRIPTION = RFC6167 + "?subscriptionId=" + SUBSCRIPTION_ID;
  private static final String FILTER = "filter";

  @Mock
  private BasicActiveMqImplementation mockVendor;
  @Mock
  private MessageConsumer mockMessageConsumer;
  @Mock
  private Session session;
  @Mock
  private JmsActorConfig jmsActorConfig;

  private AutoCloseable openMocks;
  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  @Test
  public void testDeferConsumerCreationToVendor() throws Exception {
    JmsDestination jmsDestination = mockTopicJmsDestination(false);
    mockCreateDestinationAndConsumer(jmsDestination, RFC6167_SUBSCRIPTION);

    JmsMessageConsumerFactory jmsMessageConsumerFactory = new JmsMessageConsumerFactory(mockVendor, session, RFC6167_SUBSCRIPTION, true,
        FILTER,
        jmsActorConfig);

    MessageConsumer messageConsumer = jmsMessageConsumerFactory.create();

    assertEquals(mockMessageConsumer, messageConsumer);
    verify(mockVendor).createConsumer(eq(jmsDestination), eq(FILTER), any(JmsActorConfig.class));
  }

  @Test
  public void testDurableTopicConsumer() throws Exception {
    JmsDestination jmsDestination = mockTopicJmsDestination(false);
    mockCreateDestinationAndConsumer(jmsDestination, RFC6167_SUBSCRIPTION);

    TopicSubscriber topicSubscriber = mock(TopicSubscriber.class);
    when(session.createDurableSubscriber(any(Topic.class), eq(SUBSCRIPTION_ID))).thenReturn(topicSubscriber);

    JmsMessageConsumerFactory jmsMessageConsumerFactory = new JmsMessageConsumerFactory(mockVendor, session, RFC6167_SUBSCRIPTION, false,
        FILTER,
        jmsActorConfig);

    MessageConsumer messageConsumer = jmsMessageConsumerFactory.create();

    assertEquals(topicSubscriber, messageConsumer);
    verify(session).createDurableSubscriber(any(Topic.class), eq(SUBSCRIPTION_ID));
  }

  @Test
  public void testSharedDurableTopicConsumer() throws Exception {
    String rfc6167 = RFC6167_SUBSCRIPTION + "&sharedConsumerId=" + SHARED_CONSUMER_ID;
    JmsDestination jmsDestination = mockTopicJmsDestination(true);
    mockCreateDestinationAndConsumer(jmsDestination, rfc6167);

    when(session.createSharedDurableConsumer(any(Topic.class), eq(SUBSCRIPTION_ID), eq(FILTER))).thenReturn(mockMessageConsumer);

    JmsMessageConsumerFactory jmsMessageConsumerFactory = new JmsMessageConsumerFactory(mockVendor, session, rfc6167, false, FILTER,
        jmsActorConfig);

    MessageConsumer messageConsumer = jmsMessageConsumerFactory.create();

    assertEquals(mockMessageConsumer, messageConsumer);
    verify(session).createSharedDurableConsumer(any(Topic.class), eq(SUBSCRIPTION_ID), eq(FILTER));
  }

  @Test
  public void testSharedTopicConsumer() throws Exception {
    String rfc6167 = RFC6167 + "?sharedConsumerId=" + SHARED_CONSUMER_ID;
    JmsDestination jmsDestination = mockTopicJmsDestination(false, true);
    mockCreateDestinationAndConsumer(jmsDestination, rfc6167);

    when(session.createSharedConsumer(any(Topic.class), eq(SHARED_CONSUMER_ID), eq(FILTER))).thenReturn(mockMessageConsumer);

    JmsMessageConsumerFactory jmsMessageConsumerFactory = new JmsMessageConsumerFactory(mockVendor, session, rfc6167, false, FILTER,
        jmsActorConfig);

    MessageConsumer messageConsumer = jmsMessageConsumerFactory.create();

    assertEquals(mockMessageConsumer, messageConsumer);
    verify(session).createSharedConsumer(any(Topic.class), eq(SHARED_CONSUMER_ID), eq(FILTER));
  }

  @Test
  public void testTopicConsumer() throws Exception {
    String rfc6167 = RFC6167;
    JmsDestination jmsDestination = mockTopicJmsDestination(false, false);
    mockCreateDestinationAndConsumer(jmsDestination, rfc6167);

    when(session.createConsumer(any(Topic.class), eq(FILTER))).thenReturn(mockMessageConsumer);

    JmsMessageConsumerFactory jmsMessageConsumerFactory = new JmsMessageConsumerFactory(mockVendor, session, rfc6167, false, FILTER,
        jmsActorConfig);

    MessageConsumer messageConsumer = jmsMessageConsumerFactory.create();

    assertEquals(mockMessageConsumer, messageConsumer);
    verify(session).createConsumer(any(Topic.class), eq(FILTER));
  }

  @Test
  public void testQueueConsumer() throws Exception {
    String rfc6167 = "jms:queue:myQueue";
    JmsDestination jmsDestination = mockQueueJmsDestination();
    mockCreateDestinationAndConsumer(jmsDestination, rfc6167);

    when(session.createConsumer(any(Queue.class), eq(FILTER))).thenReturn(mockMessageConsumer);

    JmsMessageConsumerFactory jmsMessageConsumerFactory = new JmsMessageConsumerFactory(mockVendor, session, rfc6167, false, FILTER,
        jmsActorConfig);

    MessageConsumer messageConsumer = jmsMessageConsumerFactory.create();

    assertEquals(mockMessageConsumer, messageConsumer);
    verify(session).createConsumer(any(Queue.class), eq(FILTER));
  }

  private void mockCreateDestinationAndConsumer(JmsDestination jmsDestination, String rfc6167) throws JMSException {
    when(mockVendor.createDestination(eq(rfc6167), any(JmsActorConfig.class))).thenReturn(jmsDestination);
    when(mockVendor.createConsumer(eq(jmsDestination), eq(FILTER), any(JmsActorConfig.class))).thenReturn(mockMessageConsumer);
  }

  private JmsDestination mockTopicJmsDestination(boolean shareable) {
    return mockTopicJmsDestination(true, shareable);
  }

  private JmsDestination mockTopicJmsDestination(boolean subscription, boolean shareable) {
    JmsDestination jmsDestination = mock(JmsDestination.class);
    when(jmsDestination.destinationType()).thenReturn(DestinationType.TOPIC);
    if (subscription) {
      when(jmsDestination.subscriptionId()).thenReturn(SUBSCRIPTION_ID);
    }
    if (shareable) {
      when(jmsDestination.sharedConsumerId()).thenReturn(SHARED_CONSUMER_ID);
    }
    when(jmsDestination.getDestination()).thenReturn(mock(Topic.class));
    return jmsDestination;
  }

  private JmsDestination mockQueueJmsDestination() {
    JmsDestination jmsDestination = mock(JmsDestination.class);
    when(jmsDestination.destinationType()).thenReturn(DestinationType.QUEUE);
    when(jmsDestination.getDestination()).thenReturn(mock(Queue.class));
    return jmsDestination;
  }

}
