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

import static com.adaptris.core.jms.JmsConfig.DEFAULT_PAYLOAD;
import static com.adaptris.core.jms.JmsConfig.MESSAGE_TRANSLATOR_LIST;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.jms.BasicJmsProducerCase.Loopback;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.TimeInterval;

public class JmsProducerTest extends JmsProducerCase {
  
  @Mock private ProducerSessionFactory mockSessionFactory;
  @Mock private ProducerSession mockProducerSession;
  @Mock private Session mockSession;
  @Mock private Message mockMessage;

  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
  }
  
  public JmsProducerTest(String name) {
    super(name);
  }

  protected JmsConsumerImpl createConsumer(ConfiguredConsumeDestination dest) {
    return new PtpConsumer(dest);
  }

  protected BasicJmsProducerCase.QueueLoopback createLoopback(EmbeddedActiveMq mq, String dest) {
    return new BasicJmsProducerCase.QueueLoopback(mq, dest);
  }


  protected JmsProducer createProducer(ProduceDestination dest) {
    return new JmsProducer(dest);
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

  public void testTransactedCommit() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);
    
    JmsProducer producer = this.createProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.commit();
    
    verify(mockSession).commit();
  }
  
  public void testNonTransactedNoCommit() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);
    
    JmsProducer producer = this.createProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.commit();
    
    verify(mockSession, times(0)).commit();
  }
  
  public void testTransactedRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);
    
    JmsProducer producer = this.createProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();
    
    verify(mockSession).rollback();
  }
  
  public void testAttemptedTransactedRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);
    doThrow(new JMSException("expected"))
      .when(mockSession).rollback();
    
    JmsProducer producer = this.createProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();
    
    verify(mockSession).rollback();
  }
  
  public void testSessionDeadOnRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenThrow(new JMSException("expected"));
    
    JmsProducer producer = this.createProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();
    
    verify(mockSession, times(0)).rollback();
  }
  
  public void testNotTransactedNoRollback() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);
    
    JmsProducer producer = new JmsProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.rollback();
    
    verify(mockSession, times(0)).rollback();
  }
  
  public void testNullMessageAck() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);
  
    JmsProducer producer = new JmsProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(null);
    
    verify(mockMessage, times(0)).acknowledge();
  }
  
  public void testMessageAckNotTransacted() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);
  
    JmsProducer producer = new JmsProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);
    
    verify(mockMessage).acknowledge();
  }
  
  public void testMessageAckTransacted() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);
  
    JmsProducer producer = new JmsProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);
    
    verify(mockMessage, times(0)).acknowledge();
  }
  
  public void testMessageAckTransactedAutoMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(true);
  
    JmsProducer producer = new JmsProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);
    
    verify(mockMessage, times(0)).acknowledge();
  }
  
  public void testMessageAckNonTransactedAutoMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);
  
    JmsProducer producer = new JmsProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);
    
    verify(mockMessage, times(0)).acknowledge();
  }
  
  public void testMessageAckNonTransactedClientMode() throws Exception {
    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockProducerSession);
    when(mockProducerSession.getSession())
      .thenReturn(mockSession);
    when(mockSession.getTransacted())
      .thenReturn(false);
  
    JmsProducer producer = new JmsProducer(new ConfiguredProduceDestination("myDestination"));
    producer.setSessionFactory(mockSessionFactory);
    producer.setAcknowledgeMode("CLIENT_ACKNOWLEDGE");
    producer.setupSession(AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx"));
    producer.acknowledge(mockMessage);
    
    verify(mockMessage).acknowledge();
  }

  public void testProduce_JmsReplyToDestination() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      Topic topic = createTopic(activeMqBroker, getName());
      AdaptrisMessage msg = createMessage(topic);
      JmsReplyToDestination d = new JmsReplyToDestination();

      JmsProducer producer = createProducer(d);
      producer.setCaptureOutgoingMessageDetails(true);
      StandaloneProducer sp = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);

      ServiceCase.execute(sp, msg);
      Map objMd = msg.getObjectHeaders();
      String prefix = Message.class.getCanonicalName() + ".";
      assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_MESSAGE_ID));
      assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_DESTINATION));
      assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_PRIORITY));
      assertTrue(objMd.containsKey(prefix + JmsConstants.JMS_TIMESTAMP));

    } finally {
      activeMqBroker.destroy();
    }
  }


  public void testProduce_CaptureOutgoingMessageDetails() throws Exception {
    String rfc6167 = "jms:queue:" + getName();

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    producer.setCaptureOutgoingMessageDetails(true);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
      AdaptrisMessage msg = createMessage();
      ServiceCase.execute(standaloneProducer, msg);
      Map objectMetadata = msg.getObjectHeaders();
      assertTrue(objectMetadata.containsKey(Message.class.getCanonicalName() + "." + JmsConstants.JMS_MESSAGE_ID));
      assertTrue(objectMetadata.containsKey(Message.class.getCanonicalName() + "." + JmsConstants.JMS_DESTINATION));
      assertTrue(objectMetadata.containsKey(Message.class.getCanonicalName() + "." + JmsConstants.JMS_PRIORITY));
      assertTrue(objectMetadata.containsKey(Message.class.getCanonicalName() + "." + JmsConstants.JMS_TIMESTAMP));
    } finally {
      stop(standaloneProducer);
      activeMqBroker.destroy();
    }
  }

  public void testProduceAndConsume_DeliveryMode() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "?deliveryMode=PERSISTENT";

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    } finally {
      activeMqBroker.destroy();
    }
  }

  public void testProduceAndConsume_Priority() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "?priority=5";

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    } finally {
      activeMqBroker.destroy();
    }
  }


  public void testProduceAndConsume_TimeToLive() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "?timeToLive=60000";

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    } finally {
      activeMqBroker.destroy();
    }
  }


  public void testSetProducerSessionFactory() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";

    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
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

  public void testDefaultSessionFactory() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    producer.setSessionFactory(new DefaultProducerSessionFactory());
    StandaloneProducer sp = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    try {
      activeMqBroker.start();
      start(standaloneConsumer, sp);
      sp.doService(createMessage());
      sp.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(sp, standaloneConsumer);

      activeMqBroker.destroy();
    }
  }

  public void testPerMessageSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    producer.setSessionFactory(new PerMessageProducerSessionFactory());
    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
      start(standaloneConsumer, standaloneProducer);
      standaloneProducer.doService(createMessage());
      // Should create a new Session now.
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(standaloneProducer, standaloneConsumer);

      activeMqBroker.destroy();
    }
  }

  public void testTimedInactivitySession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    TimedInactivityProducerSessionFactory psf =
        new TimedInactivityProducerSessionFactory(new TimeInterval(10L, TimeUnit.MILLISECONDS));
    producer.setSessionFactory(psf);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);

    try {
      activeMqBroker.start();
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

      activeMqBroker.destroy();
    }
  }

  public void testTimedInactivitySession_SessionStillValid() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    TimedInactivityProducerSessionFactory psf = new TimedInactivityProducerSessionFactory();
    producer.setSessionFactory(psf);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
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
      activeMqBroker.destroy();
    }
  }

  public void testMessageCountSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    MessageCountProducerSessionFactory psf = new MessageCountProducerSessionFactory(1);
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
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

      activeMqBroker.destroy();
    }
  }

  public void testMessageCountSession_SessionStillValid() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    MessageCountProducerSessionFactory psf = new MessageCountProducerSessionFactory();
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      assertFalse(psf.newSessionRequired());
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(standaloneProducer, standaloneConsumer);

      activeMqBroker.destroy();
    }
  }

  public void testMessageSizeSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    MessageSizeProducerSessionFactory psf =
        new MessageSizeProducerSessionFactory(Integer.valueOf(DEFAULT_PAYLOAD.length() - 1)
            .longValue());
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
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

      activeMqBroker.destroy();
    }
  }

  public void testMessageSizeSession_SessionStillValid() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    MessageSizeProducerSessionFactory psf = new MessageSizeProducerSessionFactory();
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      assertFalse(psf.newSessionRequired());
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    } finally {
      stop(standaloneProducer, standaloneConsumer);
      activeMqBroker.destroy();
    }
  }

  public void testMetadataSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    JmsProducer producer = createProducer(new ConfiguredProduceDestination(rfc6167));
    MetadataProducerSessionFactory psf = new MetadataProducerSessionFactory(getName());
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      activeMqBroker.start();
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

      activeMqBroker.destroy();
    }
  }

  public void testMultipleProducersWithSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    JmsConsumerImpl consumer = createConsumer(new ConfiguredConsumeDestination(getName()));
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer =
        new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    ServiceList serviceList =
        new ServiceList(new Service[] {
            new StandaloneProducer(activeMqBroker.getJmsConnection(),
                createProducer(new ConfiguredProduceDestination(rfc6167))),
            new StandaloneProducer(activeMqBroker.getJmsConnection(),
                createProducer(new ConfiguredProduceDestination(rfc6167)))});
    try {
      activeMqBroker.start();
      start(standaloneConsumer, serviceList);
      AdaptrisMessage msg1 = createMessage();
      AdaptrisMessage msg2 = createMessage();
      serviceList.doService(msg1);
      serviceList.doService(msg2);
      waitForMessages(jms, 4);
      assertMessages(jms, 4);
    } finally {
      stop(serviceList, standaloneConsumer);
      activeMqBroker.destroy();
    }
  }

  public void testMultipleRequestorWithSession() throws Exception {
    String rfc6167 = "jms:queue:" + getName() + "";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    ServiceList serviceList =
        new ServiceList(new Service[] {
            new StandaloneRequestor(activeMqBroker.getJmsConnection(),
                createProducer(new ConfiguredProduceDestination(rfc6167)), new TimeInterval(1L,
                    TimeUnit.SECONDS)),
            new StandaloneRequestor(activeMqBroker.getJmsConnection(),
                createProducer(new ConfiguredProduceDestination(rfc6167)), new TimeInterval(1L,
                    TimeUnit.SECONDS))});
    Loopback echo = createLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
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
      activeMqBroker.destroy();
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
    ConfiguredProduceDestination dest = new ConfiguredProduceDestination("jms:topic:myTopicName?priority=4");
    if (useQueue) {
      dest = new ConfiguredProduceDestination("jms:queue:myQueueName?priority=4");
    }
    JmsProducer p = new JmsProducer(dest);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    NullCorrelationIdSource mcs = new NullCorrelationIdSource();
    p.setCorrelationIdSource(mcs);

    StandaloneProducer result = new StandaloneProducer();

    result.setConnection(c);
    result.setProducer(p);

    return result;
  }

}
