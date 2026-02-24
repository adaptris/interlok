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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jms.*;

import com.adaptris.core.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.jms.BasicJmsProducerCase.Loopback;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.TimeInterval;

public class JmsProducerTest extends com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase {

  @Mock
  private ProducerSessionFactory mockSessionFactory;
  @Mock
  private ProducerSession mockProducerSession;
  @Mock
  private Session mockSession;
  @Mock
  private Message mockMessage;

  private AutoCloseable openMocks;

  private static EmbeddedActiveMq activeMqBroker;

  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }

  @AfterAll
  public static void tearDownAll() throws Exception {
    if (activeMqBroker != null) {
      activeMqBroker.destroy();
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  protected JmsConsumerImpl createConsumer(String dest) {
    PtpConsumer ptp = new PtpConsumer();
    ptp.setQueue(dest);
    return ptp;
  }

  protected BasicJmsProducerCase.QueueLoopback createLoopback(EmbeddedActiveMq mq, String dest) {
    return new BasicJmsProducerCase.QueueLoopback(mq, dest);
  }

  protected JmsProducer createProducer(String dest) {
    JmsProducer p = new JmsProducer();
    p.setEndpoint(dest);
    return p;
  }

  @Test
  public void testTransactedCommit() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(true);

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.commit();

    verify(mockSession).commit();
  }

  @Test
  public void testNonTransactedNoCommit() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(false);

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.commit();

    verify(mockSession, times(0)).commit();
  }

  @Test
  public void testTransactedRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(true);

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();

    verify(mockSession).rollback();
  }

  @Test
  public void testAttemptedTransactedRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(true);
    doThrow(new JMSException("expected")).when(mockSession).rollback();

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();

    verify(mockSession).rollback();
  }

  @Test
  public void testSessionDeadOnRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenThrow(new JMSException("expected"));

    JmsProducer producer = createProducer("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();

    verify(mockSession, times(0)).rollback();
  }

  @Test
  public void testNotTransactedNoRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();

    verify(mockSession, times(0)).rollback();
  }

  @Test
  public void testNullMessageAck() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(null);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testMessageAckNotTransacted() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage).acknowledge();
  }

  @Test
  public void testMessageAckTransacted() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(true);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testMessageAckTransactedAutoMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(true);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testMessageAckNonTransactedAutoMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testMessageAckNonTransactedClientMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockSession.getTransacted()).thenReturn(false);

    JmsProducer producer = new JmsProducer().withEndpoint("myDestination");
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("CLIENT_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);

    verify(mockMessage).acknowledge();
  }

  // @Test
  // public void testProduce_JmsReplyToDestination() throws Exception {
  // Topic topic = createTopic(activeMqBroker, getName());
  // AdaptrisMessage msg = createMessage(topic);
  // JmsReplyToDestination d = new JmsReplyToDestination();
  //
  // JmsProducer producer = createProducer(d);
  // producer.setCaptureOutgoingMessageDetails(true);
  // StandaloneProducer sp = new
  // StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
  //
  // ExampleServiceCase.execute(sp, msg);
  // Map<Object, Object> objMd = msg.getObjectHeaders();
  // String prefix = Message.class.getCanonicalName() + ".";
  // assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_MESSAGE_ID));
  // assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_DESTINATION));
  // assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_PRIORITY));
  // assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_TIMESTAMP));
  // }

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
  public void testProduceAndConsume_ObjectEndpoint() throws Exception {
    Queue queue = activeMqBroker.createQueue(getName());

    String endpoint = "%messageObject{objectEndpoint}";

    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    JmsProducer producer = createProducer(endpoint);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);

    AdaptrisMessage msg = createMessage();
    msg.addObjectHeader("objectEndpoint", queue);

    execute(standaloneConsumer, standaloneProducer, msg, jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testProduceAndConsume_ResolvedEndpoint() throws Exception {
    activeMqBroker.createQueue(getName());

    String endpoint = "%message{metadataEndpoint}";
    String rfc6167 = "jms:queue:" + getName();

    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    JmsProducer producer = createProducer(endpoint);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);

    AdaptrisMessage msg = createMessage();
    msg.addMessageHeader("metadataEndpoint", rfc6167);

    execute(standaloneConsumer, standaloneProducer, msg, jms);
    assertMessages(jms, 1);
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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
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
  public void testProducerResolveFromMetadataFactory() throws Exception {
    String resolveString = "%message{endpoint}";
    String rfc6167 = "jms:queue:" + getName() + "";

    AdaptrisMessage adaptrisMessage1 = createMessage();
    adaptrisMessage1.addMessageHeader("endpoint", rfc6167);

    AdaptrisMessage adaptrisMessage2 = createMessage();
    adaptrisMessage2.addMessageHeader("endpoint", rfc6167);

    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(resolveString);
    producer.setSessionFactory(new DefaultProducerSessionFactory());
    StandaloneProducer sp = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    try {
      start(standaloneConsumer, sp);
      sp.doService(adaptrisMessage1);
      sp.doService(adaptrisMessage2);
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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(rfc6167);
    producer.setSessionFactory(new PerMessageProducerSessionFactory());
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    TimedInactivityProducerSessionFactory psf = new TimedInactivityProducerSessionFactory(new TimeInterval(10L, TimeUnit.MILLISECONDS));
    producer.setSessionFactory(psf);
    producer.setRefreshSessionIfProduceException(true);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);

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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    TimedInactivityProducerSessionFactory psf = new TimedInactivityProducerSessionFactory();
    producer.setSessionFactory(psf);
    producer.setRefreshSessionIfProduceException(true);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
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
  public void testRefreshSessionIfException_MetadataTrue() throws Exception {
      String rfc6167 = "jms:queue:" + getName();
      JmsConsumerImpl consumer = createConsumer(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
      MockMessageListener jms = new MockMessageListener();
      JmsProducer producer = spy(createProducer(rfc6167));
      ProducerSessionFactory psf = spy(new DefaultProducerSessionFactory());
      producer.setSessionFactory(psf);
      producer.setRefreshSessionIfProduceException(true);
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      MessageProducer throwsExceptionProducer = mock(MessageProducer.class);
      doAnswer(args -> {
          throw new JMSException("The Session is closed");
      }).when(throwsExceptionProducer).send(isA(Destination.class), any(), anyInt(), anyInt(), anyLong());

      doReturn(new ProducerSession() {
          @Override
          public Session getSession() {
                return producer.producerSession().getSession();
            }

          @Override
          public MessageProducer getProducer() {
                return throwsExceptionProducer;
            }
      }).when(producer).producerSession();

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);

      // Explicitly set JMSRefreshSessionOnException = "true"
      AdaptrisMessage msg = createMessage();
      msg.addMetadata(JmsConstants.JMS_AUTO_REFRESH_SESSION_ON_EXCEPTION, "true");

      assertThrows(ServiceException.class, () -> {
          try {
              start(standaloneConsumer, standaloneProducer);
              standaloneProducer.doService(msg);
          } finally {
              stop(standaloneProducer, standaloneConsumer);
          }
      });

      verify(producer, times(2)).setupSession(any(), eq(false));
      verify(producer, times(1)).setupSession(any(), eq(true));
  }



  @Test
  public void testSetupSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsProducer producer = createProducer(rfc6167);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    ProducerSessionFactory psf = new DefaultProducerSessionFactory();
    producer.setSessionFactory(psf);
    try {
      start(standaloneProducer);
      ProducerSession session1 = producer.setupSession(createMessage(), false);
      ProducerSession session2 = producer.setupSession(createMessage(), false);
      assertEquals(session1, session2);
      ProducerSession session3 = producer.setupSession(createMessage(), true);
      assertNotEquals(session1, session3);
      assertNotEquals(session2, session3);
    } finally {
      stop(standaloneProducer);
    }

  }

  @Test
  public void testMessageCountSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MessageCountProducerSessionFactory psf = new MessageCountProducerSessionFactory(1);
    producer.setSessionFactory(psf);
    producer.setRefreshSessionIfProduceException(true);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MessageCountProducerSessionFactory psf = new MessageCountProducerSessionFactory();
    producer.setSessionFactory(psf);
    producer.setRefreshSessionIfProduceException(true);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MessageSizeProducerSessionFactory psf = new MessageSizeProducerSessionFactory(
        Integer.valueOf(DEFAULT_PAYLOAD.length() - 1).longValue());
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MessageSizeProducerSessionFactory psf = new MessageSizeProducerSessionFactory();
    producer.setSessionFactory(psf);
    producer.setRefreshSessionIfProduceException(true);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(rfc6167);
    MetadataProducerSessionFactory psf = new MetadataProducerSessionFactory(getName());
    producer.setSessionFactory(psf);
    producer.setRefreshSessionIfProduceException(true);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
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
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    ServiceList serviceList = new ServiceList(new StandaloneProducer(activeMqBroker.getJmsConnection(), createProducer(rfc6167)),
        new StandaloneProducer(activeMqBroker.getJmsConnection(), createProducer(rfc6167)));
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
    JmsProducer producer = createProducer(rfc6167);
    producer.setRefreshSessionIfProduceException(true);
    ServiceList serviceList = new ServiceList(
        new StandaloneRequestor(activeMqBroker.getJmsConnection(), producer, new TimeInterval(1L, TimeUnit.SECONDS)),
        new StandaloneRequestor(activeMqBroker.getJmsConnection(), producer, new TimeInterval(1L, TimeUnit.SECONDS)));
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
    JmsProducer producer = createProducer(rfc6167);
    producer.setRefreshSessionIfProduceException(true);
    StandaloneRequestor serviceList = new StandaloneRequestor(activeMqBroker.getJmsConnection(), producer,
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
    JmsProducer producer = createProducer(rfc6167);
    producer.setRefreshSessionIfProduceException(true);
    StandaloneRequestor serviceList = new StandaloneRequestor(activeMqBroker.getJmsConnection(), producer,
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

  @Test
  public void testRequest_Timeout() throws Exception {
    Assertions.assertThrows(ServiceException.class, () -> {
      String rfc6167 = "jms:queue:" + getName() + "";
      JmsProducer producer = createProducer(rfc6167);
      producer.setPerMessageProperties(false);
      StandaloneRequestor serviceList = new StandaloneRequestor(activeMqBroker.getJmsConnection(), producer,
          new TimeInterval(1L, TimeUnit.SECONDS));
      try {
        start(serviceList);
        AdaptrisMessage msg1 = createMessage();
        serviceList.doService(msg1);
      } finally {
        stop(serviceList);
      }
    });
  }

  @Test
  public void testDoProduce_RefreshSessionIfProduceException_ConfigTrue() throws Exception {
    JmsProducer producer = spy(createProducer("producer1"));
    producer.setRefreshSessionIfProduceException(true);
    producer.setSessionFactory(mockSessionFactory);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test");
    JmsDestination mockJmsDestination = mock(JmsDestination.class);
    javax.jms.MessageProducer mockMsgProducer = mock(javax.jms.MessageProducer.class);
    ProducerSession mockProducerSession = mock(ProducerSession.class);
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockProducerSession.getProducer()).thenReturn(mockMsgProducer);
    when(mockJmsDestination.getDestination()).thenReturn(mock(Destination.class));
    when(mockJmsDestination.getReplyToDestination()).thenReturn(null);
    doReturn(mock(Message.class)).when(producer).translate(any(), any());
    doReturn(true).when(producer).perMessageProperties();
    doReturn(1).when(producer).calculateDeliveryMode(any(), any());
    doReturn(1).when(producer).calculatePriority(any(), any());
    doReturn(1L).when(producer).calculateTimeToLive(any(), any());
    // First call throws, second call does not
    doThrow(new JMSException("fail")).doNothing().when(mockMsgProducer)
      .send(any(Destination.class), any(Message.class), anyInt(), anyInt(), anyLong());
    // Should not throw because retry will succeed
    assertDoesNotThrow(() -> producer.doProduce(msg, mockJmsDestination));
    // Should have called send twice (retry)
    verify(mockMsgProducer, times(2)).send(any(Destination.class), any(Message.class), anyInt(), anyInt(), anyLong());
  }

  @Test
  public void testDoProduce_RefreshSessionIfProduceException_ConfigFalse() throws Exception {
    JmsProducer producer = spy(createProducer("producer2"));
    producer.setRefreshSessionIfProduceException(false);
    producer.setSessionFactory(mockSessionFactory);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("test");
    JmsDestination mockJmsDestination = mock(JmsDestination.class);
    javax.jms.MessageProducer mockMsgProducer = mock(javax.jms.MessageProducer.class);
    ProducerSession mockProducerSession = mock(ProducerSession.class);
    when(mockSessionFactory.createProducerSession(any(), any())).thenReturn(mockProducerSession);
    when(mockProducerSession.getSession()).thenReturn(mockSession);
    when(mockProducerSession.getProducer()).thenReturn(mockMsgProducer);
    when(mockJmsDestination.getDestination()).thenReturn(mock(Destination.class));
    when(mockJmsDestination.getReplyToDestination()).thenReturn(null);
    doReturn(mock(Message.class)).when(producer).translate(any(), any());
    doReturn(true).when(producer).perMessageProperties();
    doReturn(1).when(producer).calculateDeliveryMode(any(), any());
    doReturn(1).when(producer).calculatePriority(any(), any());
    doReturn(1L).when(producer).calculateTimeToLive(any(), any());
    doThrow(new JMSException("fail")).when(mockMsgProducer)
      .send(any(Destination.class), any(Message.class), anyInt(), anyInt(), anyLong());
    // Should throw because no retry
    assertThrows(JMSException.class, () -> producer.doProduce(msg, mockJmsDestination));
    // Should have called send only once (no retry)
    verify(mockMsgProducer, times(1)).send(any(Destination.class), any(Message.class), anyInt(), anyInt(), anyLong());
  }

  @Override
  protected List<Object> retrieveObjectsForSampleConfig() {
    ArrayList<Object> result = new ArrayList<>();
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
    return super.createBaseFileName(object) + "-" + p.getMessageTranslator().getClass().getSimpleName();
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
