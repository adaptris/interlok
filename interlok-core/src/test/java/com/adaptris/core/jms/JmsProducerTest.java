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

import static com.adaptris.interlok.junit.scaffolding.jms.JmsConfig.DEFAULT_PAYLOAD;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsConfig.MESSAGE_TRANSLATOR_LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.jms.BasicJmsProducerCase.Loopback;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.TimeInterval;

public class JmsProducerTest extends com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase {

  @Mock private ProducerSessionFactory mockSessionFactory;
  @Mock private ProducerSession mockProducerSession;
  @Mock private Session mockSession;
  @Mock private Message mockMessage;

  private AutoCloseable openMocks;
  
  private static EmbeddedActiveMq activeMqBroker;

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
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


  @SuppressWarnings("deprecation")
  protected JmsConsumerImpl createConsumer(String dest) {
    PtpConsumer ptp = new PtpConsumer();
    ptp.setQueue(dest);
    return ptp;
  }

  protected BasicJmsProducerCase.QueueLoopback createLoopback(EmbeddedActiveMq mq, String dest) {
    return new BasicJmsProducerCase.QueueLoopback(mq, dest);
  }


  @SuppressWarnings("deprecation")
  protected JmsProducer createProducer(String dest) {
    JmsProducer p = new JmsProducer();
    p.setEndpoint(dest);
    return p;
  }

  private AdaptrisMessage createMessage(Destination d) throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx");
    msg.addObjectHeader(JmsConstants.OBJ_JMS_REPLY_TO_KEY, d);
    return msg;

  }

  private Topic createTopic(EmbeddedActiveMq broker, String name) throws Exception {
    ActiveMQConnection conn = broker.createConnection();
    ActiveMQSession session = (ActiveMQSession) conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    return session.createTopic(name);
  }

  @Test
  public void testTransactedCommit() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.commit();

    verify(mockSession).commit();
  }

  @Test
  public void testNonTransactedNoCommit() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.commit();

    verify(mockSession, times(0)).commit();
  }

  @Test
  public void testTransactedRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();

    verify(mockSession).rollback();
  }

  @Test
  public void testAttemptedTransactedRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);
    doThrow(new JMSException("expected"))
      .when(mockSession).rollback();

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();

    verify(mockSession).rollback();
  }

  @Test
  public void testSessionDeadOnRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenThrow(new JMSException("expected"));

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();

    verify(mockSession, times(0)).rollback();
  }

  @Test
  public void testNotTransactedNoRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();

    verify(mockSession, times(0)).rollback();
  }

  @Test
  public void testNullMessageAck() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(null);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testMessageAckNotTransacted() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage).acknowledge();
  }

  @Test
  public void testMessageAckTransacted() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testMessageAckTransactedAutoMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testMessageAckNonTransactedAutoMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testMessageAckNonTransactedClientMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("CLIENT_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage).acknowledge();
  }

//  @Test
//  public void testProduce_JmsReplyToDestination() throws Exception {
//    Topic topic = createTopic(activeMqBroker, getName());
//    AdaptrisMessage msg = createMessage(topic);
//    JmsReplyToDestination d = new JmsReplyToDestination();
//
//    JmsProducer producer = createProducer(d);
//    producer.setCaptureOutgoingMessageDetails(true);
//    StandaloneProducer sp = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
//
//    ExampleServiceCase.execute(sp, msg);
//    Map<Object, Object> objMd = msg.getObjectHeaders();
//    String prefix = Message.class.getCanonicalName() + ".";
//    assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_MESSAGE_ID));
//    assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_DESTINATION));
//    assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_PRIORITY));
//    assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_TIMESTAMP));
//  }

  @Test
  public void testProduce_CaptureOutgoingMessageDetails() throws Exception {
    String rfc6167 = "jms:queue:" + getName();
    JmsProducer producer = createProducer(rfc6167);
    producer.setCaptureOutgoingMessageDetails(true);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      AdaptrisMessage msg = createMessage();
      ExampleServiceCase.execute(standaloneProducer, msg);
      Map<Object, Object> objectMetadata = msg.getObjectHeaders();
      assertTrue(objectMetadata.containsKey(Message.class.getCanonicalName() + "." + JmsConstants.JMS_MESSAGE_ID));
      assertTrue(objectMetadata.containsKey(Message.class.getCanonicalName() + "." + JmsConstants.JMS_DESTINATION));
      assertTrue(objectMetadata.containsKey(Message.class.getCanonicalName() + "." + JmsConstants.JMS_PRIORITY));
      assertTrue(objectMetadata.containsKey(Message.class.getCanonicalName() + "." + JmsConstants.JMS_TIMESTAMP));
    } finally {
      stop(standaloneProducer);
    }
  }

  @Test
  public void testProduceAndConsume_DeliveryMode() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "?deliveryMode=PERSISTENT";

    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(rfc6167);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testProduceAndConsume_Priority() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "?priority=5";

    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(rfc6167);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testProduceAndConsume_TimeToLive() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "?timeToLive=60000";

    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(rfc6167);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testSetProducerSessionFactory() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";

    JmsProducer producer = createProducer(rfc6167);
    assertEquals(DefaultProducerSessionFactory.class, producer.getSessionFactory().getClass());
    try {
      producer.setSessionFactory(null);
      fail();
    } catch (IllegalArgumentException e) {

    }
    TimedInactivityProducerSessionFactory psf = new TimedInactivityProducerSessionFactory();
    producer.setSessionFactory(psf);
    assertEquals(psf, producer.getSessionFactory());
  }

  @Test
  public void testDefaultSessionFactory() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    producer.setSessionFactory(new DefaultProducerSessionFactory());
    StandaloneProducer sp = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    try {
      start(standaloneConsumer, sp);
      sp.doService(createMessage());
      sp.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(sp, standaloneConsumer);
    }
  }

  @Test
  public void testPerMessageSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(rfc6167);
    producer.setSessionFactory(new PerMessageProducerSessionFactory());
    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);
      standaloneProducer.doService(createMessage());
      // Should create a new Session now.
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testTimedInactivitySession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    TimedInactivityProducerSessionFactory psf =
        new TimedInactivityProducerSessionFactory(new TimeInterval(10L, TimeUnit.MILLISECONDS));
    producer.setSessionFactory(psf);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);

    try {
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      Thread.sleep(200);
      assertTrue(psf.newSessionRequired());
      // Should create a new Session now.
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testTimedInactivitySession_SessionStillValid() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    TimedInactivityProducerSessionFactory psf = new TimedInactivityProducerSessionFactory();
    producer.setSessionFactory(psf);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      Thread.sleep(200);
      assertFalse(psf.newSessionRequired());
      // Still should be a valid session; and could produce regardless.
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMessageCountSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MessageCountProducerSessionFactory psf = new MessageCountProducerSessionFactory(1);
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      standaloneProducer.doService(createMessage());
      assertTrue(psf.newSessionRequired());
      // Should create a new Session now.
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 3);
      assertMessages(jms, 3);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMessageCountSession_SessionStillValid() throws Exception {

    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MessageCountProducerSessionFactory psf = new MessageCountProducerSessionFactory();
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      assertFalse(psf.newSessionRequired());
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMessageSizeSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MessageSizeProducerSessionFactory psf =
        new MessageSizeProducerSessionFactory(Integer.valueOf(DEFAULT_PAYLOAD.length() - 1)
            .longValue());
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      standaloneProducer.doService(createMessage());
      assertTrue(psf.newSessionRequired());
      // Should create a new Session now.
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 3);
      assertMessages(jms, 3);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMessageSizeSession_SessionStillValid() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MessageSizeProducerSessionFactory psf = new MessageSizeProducerSessionFactory();
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      assertFalse(psf.newSessionRequired());
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMetadataSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MetadataProducerSessionFactory psf = new MetadataProducerSessionFactory(getName());
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);

      AdaptrisMessage msg1 = createMessage();
      AdaptrisMessage msg2 = createMessage();
      AdaptrisMessage msg3 = createMessage();
      msg3.addMetadata(getName(), Boolean.FALSE.toString());
      AdaptrisMessage msg4 = createMessage();
      msg4.addMetadata(getName(), Boolean.TRUE.toString());

      standaloneProducer.doService(msg1);
      assertFalse(psf.newSessionRequired(msg2));
      standaloneProducer.doService(msg2);
      assertFalse(psf.newSessionRequired(msg3));
      standaloneProducer.doService(msg3);
      assertTrue(psf.newSessionRequired(msg4));
      standaloneProducer.doService(msg4);
      waitForMessages(jms, 4);
      assertMessages(jms, 4);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMultipleProducersWithSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    ServiceList serviceList =
        new ServiceList(new Service[] {
            new StandaloneProducer(activeMqBroker.getJmsConnection(),
                createProducer(rfc6167)),
            new StandaloneProducer(activeMqBroker.getJmsConnection(),
                createProducer(rfc6167))});
    try {
      start(standaloneConsumer, serviceList);
      AdaptrisMessage msg1 = createMessage();
      AdaptrisMessage msg2 = createMessage();
      serviceList.doService(msg1);
      serviceList.doService(msg2);
      waitForMessages(jms, 4);
      assertMessages(jms, 4);
    } finally {
      stop(serviceList, standaloneConsumer);
    }
  }

  @Test
  public void testMultipleRequestorWithSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    ServiceList serviceList =
        new ServiceList(new Service[] {
            new StandaloneRequestor(activeMqBroker.getJmsConnection(),
                createProducer(rfc6167), new TimeInterval(1L,
                    TimeUnit.SECONDS)),
            new StandaloneRequestor(activeMqBroker.getJmsConnection(),
                createProducer(rfc6167), new TimeInterval(1L,
                    TimeUnit.SECONDS))});
    Loopback echo = createLoopback(activeMqBroker, getName());
    try {
      echo.start();
      start(serviceList);
      AdaptrisMessage msg1 = createMessage();
      AdaptrisMessage msg2 = createMessage();
      serviceList.doService(msg1);
      serviceList.doService(msg2);
      assertEquals(DEFAULT_PAYLOAD.toUpperCase(), msg1.getContent());
      assertEquals(DEFAULT_PAYLOAD.toUpperCase(), msg2.getContent());
    } finally {
      stop(serviceList);
      echo.stop();
    }
  }


  @Test
  public void testRequest_AsyncReplyTo_Metadata() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    StandaloneRequestor serviceList = new StandaloneRequestor(activeMqBroker.getJmsConnection(),
        createProducer(rfc6167),
        new TimeInterval(1L, TimeUnit.SECONDS));
    Loopback echo = createLoopback(activeMqBroker, getName());
    try {
      echo.start();
      start(serviceList);
      AdaptrisMessage msg = createMessage();
      msg.addMetadata(JmsConstants.JMS_ASYNC_STATIC_REPLY_TO, getName() + "_reply");
      serviceList.doService(msg);
      assertEquals(DEFAULT_PAYLOAD.toUpperCase(), msg.getContent());
    } finally {
      stop(serviceList);
      echo.stop();
    }
  }

  @Test
  public void testRequest_DefinedReplyTo() throws Exception {
    String rfc6167 = String.format("jms:queue:%1$s?replyToName=%1$s_reply", getName());
    StandaloneRequestor serviceList = new StandaloneRequestor(activeMqBroker.getJmsConnection(),
        createProducer(rfc6167),
        new TimeInterval(1L, TimeUnit.SECONDS));
    Loopback echo = createLoopback(activeMqBroker, getName());
    try {
      echo.start();
      start(serviceList);
      AdaptrisMessage msg = createMessage();
      serviceList.doService(msg);
      assertEquals(DEFAULT_PAYLOAD.toUpperCase(), msg.getContent());
    } finally {
      stop(serviceList);
      echo.stop();
    }
  }

  @Test(expected = ServiceException.class)
  public void testRequest_Timeout() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsProducer producer = createProducer(rfc6167);
    producer.setPerMessageProperties(false);
    StandaloneRequestor serviceList = new StandaloneRequestor(activeMqBroker.getJmsConnection(),
        producer, new TimeInterval(1L, TimeUnit.SECONDS));
    try {
      start(serviceList);
      AdaptrisMessage msg1 = createMessage();
      serviceList.doService(msg1);
    } finally {
      stop(serviceList);
    }
  }



  @Override
  protected List retrieveObjectsForSampleConfig() {
    ArrayList result = new ArrayList();
    boolean useQueue = true;
    for (MessageTypeTranslator t : MESSAGE_TRANSLATOR_LIST) {
      StandaloneProducer p = retrieveSampleConfig(useQueue);
      ((JmsProducer) p.getProducer()).setMessageTranslator(t);
      useQueue = !useQueue;
      result.add(p);
    }
    return result;
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String createBaseFileName(Object object) {
    JmsProducer p = (JmsProducer) ((StandaloneProducer) object).getProducer();
    return super.createBaseFileName(object) + "-"
        + p.getMessageTranslator().getClass().getSimpleName();
  }

  private StandaloneProducer retrieveSampleConfig(boolean useQueue) {
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    String dest = "jms:topic:myTopicName?priority=4";
    if (useQueue) {
      dest = "jms:queue:myQueueName?priority=4";
    }
    JmsProducer p = new JmsProducer().withEndpoint(dest);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    NullCorrelationIdSource mcs = new NullCorrelationIdSource();
    p.setCorrelationIdSource(mcs);

    StandaloneProducer result = new StandaloneProducer();

    result.setConnection(c);
    result.setProducer(p);

    return result;
  }

}
