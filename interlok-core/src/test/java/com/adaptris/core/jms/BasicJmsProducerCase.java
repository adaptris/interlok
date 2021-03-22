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

import static com.adaptris.core.jms.JmsUtils.closeQuietly;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsConfig.DEFAULT_PAYLOAD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQQueueSender;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.ActiveMQTopicPublisher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.TimeInterval;

public abstract class BasicJmsProducerCase
    extends com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase {

  protected abstract DefinedJmsProducer createProducer(String dest);

  protected abstract JmsConsumerImpl createConsumer(String dest);

  protected abstract Loopback createLoopback(EmbeddedActiveMq mq, String dest);

  private static final Logger logger = LoggerFactory.getLogger(BasicJmsProducerCase.class);

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
  
  // INTERLOK-2121
  @Test
  public void testProducerSession_Invalided() throws Exception {
    DefinedJmsProducer producer = createProducer(getName());
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneProducer);
      AdaptrisMessage msg = createMessage();
      standaloneProducer.doService(msg);
      assertNotNull(producer.producerSession());
      stop(standaloneProducer);
      assertNull(producer.producerSession());
    } finally {
      stop(standaloneProducer);
    }
  }

  @Test
  public void testProduce_CaptureOutgoingMessageDetails() throws Exception {
    DefinedJmsProducer producer = createProducer(getName());
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
    }
    finally {
      stop(standaloneProducer);
    }
  }

  @Test
  public void testProduceAndConsume_IntegerAcknowledgementMode_IntegerDeliveryMode() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode(String.valueOf(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.acknowledgeMode()));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    DefinedJmsProducer producer = createProducer(getName());
    producer.setDeliveryMode(String.valueOf(com.adaptris.core.jms.DeliveryMode.Mode.PERSISTENT.deliveryMode()));

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    
    execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testSetProducerSessionFactory() throws Exception {
    DefinedJmsProducer producer = createProducer(getName());
    assertEquals(DefaultProducerSessionFactory.class, producer.getSessionFactory().getClass());
    try {
      producer.setSessionFactory(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    TimedInactivityProducerSessionFactory psf = new TimedInactivityProducerSessionFactory();
    producer.setSessionFactory(psf);
    assertEquals(psf, producer.getSessionFactory());
  }

  @Test
  public void testDefaultSessionFactory() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(getName());
    producer.setSessionFactory(new DefaultProducerSessionFactory());
    StandaloneProducer sp = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    try {
      start(standaloneConsumer, sp);
      sp.doService(createMessage());
      sp.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    }
    finally {
      stop(sp, standaloneConsumer);
    }
  }
  
  @Test
  public void testResolvableEndpoint() throws Exception {
    String resolveString = "%message{endpoint}";
    
    AdaptrisMessage adaptrisMessage1 = createMessage();
    adaptrisMessage1.addMessageHeader("endpoint", getName());
    
    AdaptrisMessage adaptrisMessage2 = createMessage();
    adaptrisMessage2.addMessageHeader("endpoint", getName());
    
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(resolveString);
    producer.setSessionFactory(new DefaultProducerSessionFactory());
    StandaloneProducer sp = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    standaloneConsumer.registerAdaptrisMessageListener(jms);

    try {
      start(standaloneConsumer, sp);
      sp.doService(adaptrisMessage1);
      sp.doService(adaptrisMessage2);
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    }
    finally {
      stop(sp, standaloneConsumer);
    }
  }

  @Test
  public void testPerMessageSession() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    DefinedJmsProducer producer = createProducer(getName());
    producer.setSessionFactory(new PerMessageProducerSessionFactory());
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);
      standaloneProducer.doService(createMessage());
      // Should create a new Session now.
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    }
    finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testTimedInactivitySession() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(getName());
    TimedInactivityProducerSessionFactory psf = new TimedInactivityProducerSessionFactory(new TimeInterval(10L,
        TimeUnit.MILLISECONDS));
    producer.setSessionFactory(psf);
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
    }
    finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testTimedInactivitySession_SessionStillValid() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(getName());
    TimedInactivityProducerSessionFactory psf = new TimedInactivityProducerSessionFactory();
    producer.setSessionFactory(psf);
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
    }
    finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMessageCountSession() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(getName());
    MessageCountProducerSessionFactory psf = new MessageCountProducerSessionFactory(1);
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
    }
    finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMessageCountSession_SessionStillValid() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(getName());
    MessageCountProducerSessionFactory psf = new MessageCountProducerSessionFactory();
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      assertFalse(psf.newSessionRequired());
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    }
    finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMessageSizeSession() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(getName());
    MessageSizeProducerSessionFactory psf = new MessageSizeProducerSessionFactory(Integer.valueOf(DEFAULT_PAYLOAD.length() - 1)
        .longValue());
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
    }
    finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMessageSizeSession_SessionStillValid() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(getName());
    MessageSizeProducerSessionFactory psf = new MessageSizeProducerSessionFactory();
    producer.setSessionFactory(psf);

    standaloneConsumer.registerAdaptrisMessageListener(jms);

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(), producer);
    try {
      start(standaloneConsumer, standaloneProducer);

      standaloneProducer.doService(createMessage());
      assertFalse(psf.newSessionRequired());
      standaloneProducer.doService(createMessage());
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    }
    finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMetadataSession() throws Exception {

    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    DefinedJmsProducer producer = createProducer(getName());
    MetadataProducerSessionFactory psf = new MetadataProducerSessionFactory(getName());
    producer.setSessionFactory(psf);

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
    }
    finally {
      stop(standaloneProducer, standaloneConsumer);
    }
  }

  @Test
  public void testMultipleProducersWithSession() throws Exception {
    JmsConsumerImpl consumer = createConsumer(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(), consumer);
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    ServiceList serviceList = new ServiceList(new Service[]
    {
        new StandaloneProducer(activeMqBroker.getJmsConnection(), createProducer(getName())),
        new StandaloneProducer(activeMqBroker.getJmsConnection(), createProducer(getName()))
    });
    try {
      start(standaloneConsumer, serviceList);
      AdaptrisMessage msg1 = createMessage();
      AdaptrisMessage msg2 = createMessage();
      serviceList.doService(msg1);
      serviceList.doService(msg2);
      waitForMessages(jms, 4);
      assertMessages(jms, 4);
    }
    finally {
      stop(serviceList, standaloneConsumer);
    }
  }

  @Test
  public void testMultipleRequestorWithSession() throws Exception {
    ServiceList serviceList = new ServiceList(new Service[]
    {
        new StandaloneRequestor(activeMqBroker.getJmsConnection(), createProducer(getName()),
            new TimeInterval(1L, TimeUnit.SECONDS)),
        new StandaloneRequestor(activeMqBroker.getJmsConnection(), createProducer(getName()),
            new TimeInterval(1L, TimeUnit.SECONDS))
    });
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
    }
    finally {
      stop(serviceList);
      echo.stop();
    }
  }

  protected static abstract class Loopback implements MessageListener {
    protected String listenQueueOrTopic;
    protected EmbeddedActiveMq broker;
    protected ActiveMQSession session;
    protected Message lastMsg = null;
    protected ActiveMQConnection conn;
    private boolean isTextMessage = true;

    Loopback(EmbeddedActiveMq mq, String dest) {
      listenQueueOrTopic = dest;
      broker = mq;
    }

    Loopback(EmbeddedActiveMq mq, String dest, boolean isText) {
      listenQueueOrTopic = dest;
      broker = mq;
      isTextMessage = isText;
    }

    public void start() throws Exception {
      conn = broker.createConnection();
      session = (ActiveMQSession) conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
      startListener(listenQueueOrTopic);
      conn.start();
    }

    public void stop() throws Exception {
      stopListener();
      closeQuietly(session);
      closeQuietly(conn, true);
    }

    @Override
    public void onMessage(Message m) {
      try {
        logger.debug("Got Message " + m.getJMSMessageID());
        TextMessage reply = session.createTextMessage();
        if (isTextMessage) {
          reply.setText(((TextMessage) m).getText().toUpperCase());
        }
        else {
          reply.setText(DEFAULT_PAYLOAD.toUpperCase());
        }
        try {
          Destination replyTo = m.getJMSReplyTo();
          if (replyTo != null) {
            reply(reply, replyTo);
          }
        }
        catch (Exception e) {
          ;
        }
        lastMsg = m;
      }
      catch (Exception e) {
        logger.error("Got exception ", e);

      }
    }

    Message getLastMessage() {
      return lastMsg;
    }

    void waitFor(long timeout) {
      int count = 0;
      while (getLastMessage() == null && count <= timeout) {
        try {
          Thread.sleep(100);
          count += 100;
        }
        catch (InterruptedException e) {

        }
      }
    }

    abstract void startListener(String listenOn) throws Exception;

    abstract void stopListener() throws Exception;

    abstract void reply(Message reply, Destination replyTo) throws Exception;
  }

  protected static class TopicLoopback extends Loopback {
    private TopicSubscriber subscriber;

    TopicLoopback(EmbeddedActiveMq mq, String dest) {
      super(mq, dest);
    }

    TopicLoopback(EmbeddedActiveMq mq, String dest, boolean b) {
      super(mq, dest, b);
    }

    @Override
    void reply(Message reply, Destination replyTo) throws Exception {
      if (replyTo != null) {
        ActiveMQTopicPublisher pub = (ActiveMQTopicPublisher) session.createPublisher((Topic) replyTo);
        pub.publish(reply);
        pub.close();
      }
    }

    @Override
    void startListener(String listenOn) throws Exception {
      Topic d = session.createTopic(listenOn);
      subscriber = session.createSubscriber(d);
      subscriber.setMessageListener(this);
    }

    @Override
    void stopListener() throws Exception {
      closeQuietly(subscriber);
    }
  }

  protected static class QueueLoopback extends Loopback {
    private QueueReceiver receiver;

    QueueLoopback(EmbeddedActiveMq mq, String dest) {
      super(mq, dest);
    }

    QueueLoopback(EmbeddedActiveMq mq, String dest, boolean b) {
      super(mq, dest, b);
    }

    @Override
    void reply(Message reply, Destination replyTo) throws Exception {
      if (replyTo != null) {
        ActiveMQQueueSender pub = (ActiveMQQueueSender) session.createSender((Queue) replyTo);
        pub.send(reply);
        pub.close();
      }
    }

    @Override
    void startListener(String listenOn) throws Exception {
      Queue d = session.createQueue(listenOn);
      receiver = session.createReceiver(d);
      receiver.setMessageListener(this);
    }

    @Override
    void stopListener() throws Exception {
      closeQuietly(receiver);
    }
  }
}
