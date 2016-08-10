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

package com.adaptris.core.jms.activemq;

import static com.adaptris.core.jms.JmsConfig.DEFAULT_PAYLOAD;
import static com.adaptris.core.jms.JmsConfig.HIGHEST_PRIORITY;
import static com.adaptris.core.jms.JmsConfig.LOWEST_PRIORITY;
import static com.adaptris.core.jms.activemq.AdvancedActiveMqImplementationTest.createImpl;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.addBlobUrlRef;

import javax.jms.DeliveryMode;
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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.jms.BytesMessageTranslator;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.jms.JmsProducerCase;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.TextMessageTranslator;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.AdaptrisMessageStub;
import com.adaptris.core.stubs.ExternalResourcesHelper;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.security.password.Password;
import com.adaptris.util.GuidGenerator;

public class BasicActiveMqProducerTest extends JmsProducerCase {

  private static final int DEFAULT_TIMEOUT = 5000;

  private static final String MY_CLIENT_ID;
  private static final String MY_SUBSCRIPTION_ID;

  static {
    try {
      GuidGenerator guid = new GuidGenerator();
      MY_CLIENT_ID = guid.getUUID().replaceAll(":", "").replaceAll("-", "");
      MY_SUBSCRIPTION_ID = guid.getUUID().replaceAll(":", "").replaceAll("-", "");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public BasicActiveMqProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-BasicActiveMQ";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    JmsConnection connection = new JmsConnection();
    PtpProducer producer = new PtpProducer();
    ConfiguredProduceDestination dest = new ConfiguredProduceDestination();
    dest.setDestination("destination");

    producer.setDestination(dest);
    UrlVendorImplementation vendorImpl = createImpl();
    vendorImpl.setBrokerUrl(BasicActiveMqImplementationTest.PRIMARY);
    connection.setUserName("BrokerUsername");
    connection.setPassword("BrokerPassword");
    connection.setVendorImplementation(vendorImpl);

    StandaloneProducer result = new StandaloneProducer();
    result.setConnection(connection);
    result.setProducer(producer);

    return result;
  }

  public void testTopicRequestReply() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    TopicLoopback echo = new TopicLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
      echo.start();
      StandaloneRequestor standaloneProducer = new StandaloneRequestor(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));
      AdaptrisMessage msg = createMessage();
      ServiceCase.execute(standaloneProducer, msg);
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertNotNull(echo.getLastMessage().getJMSReplyTo());
      assertEquals(DEFAULT_PAYLOAD.toUpperCase(), msg.getContent());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  public void testTopicRequestReplyWithMessageWrongType() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    TopicLoopback echo = new TopicLoopback(broker, getName(), false);
    try {
      broker.start();
      echo.start();
      PasProducer producer = new PasProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BytesMessageTranslator());
      StandaloneRequestor req = new StandaloneRequestor(broker.getJmsConnection(createVendorImpl()), producer);
      AdaptrisMessage msg = createMessage();
      ServiceCase.execute(req, msg);
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertNotNull(echo.getLastMessage().getJMSReplyTo());
    }
    finally {
      echo.stop();
      broker.destroy();
    }
  }

  public void testQueueRequestReply() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    QueueLoopback echo = new QueueLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
      echo.start();
      StandaloneRequestor standaloneProducer = new StandaloneRequestor(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PtpProducer(new ConfiguredProduceDestination(getName())));
      AdaptrisMessage msg = createMessage();
      ServiceCase.execute(standaloneProducer, msg);
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertNotNull(echo.getLastMessage().getJMSReplyTo());
      assertEquals(DEFAULT_PAYLOAD.toUpperCase(), msg.getContent());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  public void testQueueRequestReplyWithMessageWrongType() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    QueueLoopback echo = new QueueLoopback(activeMqBroker, getName(), false);
    try {
      activeMqBroker.start();
      echo.start();
      PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BytesMessageTranslator());
      StandaloneRequestor standaloneProducer = new StandaloneRequestor(activeMqBroker.getJmsConnection(createVendorImpl()),
          producer);
      AdaptrisMessage msg = createMessage();
      ServiceCase.execute(standaloneProducer, msg);
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertNotNull(echo.getLastMessage().getJMSReplyTo());
      assertEquals(DEFAULT_PAYLOAD.toUpperCase(), msg.getContent());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  public void testTopicProduce_WithStaticReplyTo() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    TopicLoopback echo = new TopicLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
      echo.start();
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));
      AdaptrisMessage msg = EmbeddedActiveMq.createMessage(null);
      msg.addMetadata(JmsConstants.JMS_ASYNC_STATIC_REPLY_TO, getName() + "_reply");
      ServiceCase.execute(standaloneProducer, msg);
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertNotNull(echo.getLastMessage().getJMSReplyTo());
      assertEquals("topic://" + getName() + "_reply", echo.getLastMessage().getJMSReplyTo().toString());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  public void testQueueProduce_WithStaticReplyTo() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    QueueLoopback echo = new QueueLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
      echo.start();
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PtpProducer(new ConfiguredProduceDestination(getName())));
      AdaptrisMessage msg = EmbeddedActiveMq.createMessage(null);
      msg.addMetadata(JmsConstants.JMS_ASYNC_STATIC_REPLY_TO, getName() + "_reply");
      ServiceCase.execute(standaloneProducer, msg);
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertNotNull(echo.getLastMessage().getJMSReplyTo());
      assertEquals("queue://" + getName() + "_reply", echo.getLastMessage().getJMSReplyTo().toString());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  public void testTopicProduceWithPerMessagePropertiesDisabled() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    TopicLoopback echo = new TopicLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
      echo.start();
      PasProducer pasProducer = new PasProducer(new ConfiguredProduceDestination(getName()));
      pasProducer.setDeliveryMode(String.valueOf(DeliveryMode.PERSISTENT));
      pasProducer.setPriority(LOWEST_PRIORITY);
      pasProducer.setTtl(0L);
      pasProducer.setPerMessageProperties(false);
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          pasProducer);
      ServiceCase.execute(standaloneProducer, createMessage());
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertEquals(LOWEST_PRIORITY, echo.getLastMessage().getJMSPriority());
      assertEquals(DeliveryMode.PERSISTENT, echo.getLastMessage().getJMSDeliveryMode());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  public void testTopicProduceWithPerMessageProperties() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    TopicLoopback echo = new TopicLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
      echo.start();
      PasProducer pasProducer = new PasProducer(new ConfiguredProduceDestination(getName()));
      pasProducer.setDeliveryMode(String.valueOf(DeliveryMode.PERSISTENT));
      pasProducer.setPriority(LOWEST_PRIORITY);
      pasProducer.setTtl(0L);
      pasProducer.setPerMessageProperties(true);
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          pasProducer);
      ServiceCase.execute(standaloneProducer, createMessage());
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertEquals(HIGHEST_PRIORITY, echo.getLastMessage().getJMSPriority());
      assertEquals(DeliveryMode.NON_PERSISTENT, echo.getLastMessage().getJMSDeliveryMode());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  public void testTopicProduceAndConsume() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PasConsumer consumer = new PasConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));
      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testTopicProduceAndConsume_CustomMessageFactory() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PasConsumer consumer = new PasConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageFactory(new StubMessageFactory());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));
      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
      assertEquals(AdaptrisMessageStub.class, jms.getMessages().get(0).getClass());
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testTopicProduceAndConsume_WithEncoder() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PasConsumer consumer = new PasConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setEncoder(new MimeEncoder());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);
      PasProducer producer = new PasProducer(new ConfiguredProduceDestination(getName()));
      producer.setEncoder(new MimeEncoder());
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);
      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testTopicProduceAndConsume_DurableSubscriber() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PasConsumer consumer = new PasConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setDurable(true);
      consumer.setSubscriptionId(MY_SUBSCRIPTION_ID);
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      JmsConnection conn = activeMqBroker.getJmsConnection(createVendorImpl(), true);
      conn.setClientId(MY_CLIENT_ID);
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(conn, consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      // Start it once to get some durable Action.
      start(standaloneConsumer);
      stop(standaloneConsumer);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));

      int count = 10;
      for (int i = 0; i < count; i++) {
        ServiceCase.execute(standaloneProducer, createMessage());
      }

      start(standaloneConsumer);
      waitForMessages(jms, count);
      assertMessages(jms, 10);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testTopicProduceAndConsumeWrongType() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PasConsumer consumer = new PasConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new BytesMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));
      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testQueueProduceAndConsume() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");

      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PtpProducer(new ConfiguredProduceDestination(getName())));

      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testQueueProduceAndConsume_CustomMessageFactory() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageFactory(new StubMessageFactory());

      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PtpProducer(new ConfiguredProduceDestination(getName())));

      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
      assertEquals(AdaptrisMessageStub.class, jms.getMessages().get(0).getClass());
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testQueueProduceAndConsume_WithEncoder() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setEncoder(new MimeEncoder());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");

      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setEncoder(new MimeEncoder());
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);

      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testQueueProduceAndConsumeWrongType() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new BytesMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PtpProducer(new ConfiguredProduceDestination(getName())));

      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testQueueProduceAndConsumeWithSecurity() throws Exception {
    RequiresCredentialsBroker broker = new RequiresCredentialsBroker();
    try {
      broker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(RequiresCredentialsBroker.DEFAULT_PREFIX + "queue"));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      JmsConnection conn = broker.getJmsConnection(createVendorImpl(), true);
      conn.setUserName(RequiresCredentialsBroker.DEFAULT_USERNAME);
      conn.setPassword(RequiresCredentialsBroker.DEFAULT_PASSWORD);
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(conn, consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      conn = broker.getJmsConnection(createVendorImpl());
      conn.setUserName(RequiresCredentialsBroker.DEFAULT_USERNAME);
      conn.setPassword(RequiresCredentialsBroker.DEFAULT_PASSWORD);
      StandaloneProducer standaloneProducer = new StandaloneProducer(conn, new PtpProducer(new ConfiguredProduceDestination(
          RequiresCredentialsBroker.DEFAULT_PREFIX + "queue")));

      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      broker.destroy();
    }
  }

  public void testQueueProduceAndConsumeWithSecurity_EncryptedPassword() throws Exception {
    RequiresCredentialsBroker broker = new RequiresCredentialsBroker();
    try {
      broker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(RequiresCredentialsBroker.DEFAULT_PREFIX + "queue"));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      JmsConnection conn = broker.getJmsConnection(createVendorImpl(), true);
      conn.setUserName(RequiresCredentialsBroker.DEFAULT_USERNAME);
      conn.setPassword(Password.encode(RequiresCredentialsBroker.DEFAULT_PASSWORD, Password.PORTABLE_PASSWORD));
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(conn, consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      conn = broker.getJmsConnection(createVendorImpl());
      conn.setUserName(RequiresCredentialsBroker.DEFAULT_USERNAME);
      conn.setPassword(RequiresCredentialsBroker.DEFAULT_PASSWORD);
      StandaloneProducer standaloneProducer = new StandaloneProducer(conn, new PtpProducer(new ConfiguredProduceDestination(
          RequiresCredentialsBroker.DEFAULT_PREFIX + "queue")));

      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      broker.destroy();
    }
  }

  // public void testBug2089() throws Exception {
  // EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
  // try {
  // activeMqBroker.start();
  // PtpConsumer consumer = new PtpConsumer(new
  // ConfiguredConsumeDestination(get(QUEUE)));
  // consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
  // consumer.setMessageTranslator(new BytesMessageTranslator());
  // StandaloneConsumer standaloneConsumer = new
  // StandaloneConsumer(activeMqBroker.getPtpConnection(createVendorImpl()),
  // consumer);
  // MockMessageListener jms = new MockMessageListener();
  // standaloneConsumer.registerAdaptrisMessageListener(jms);
  //
  // PtpProducer producer = new PtpProducer(new
  // ConfiguredProduceDestination(get(QUEUE)));
  // producer.setMessageTranslator(new BlobMessageTranslator("blobUrl"));
  // StandaloneProducer standaloneProducer = new
  // StandaloneProducer(activeMqBroker.getPtpConnection(createVendorImpl()),
  // producer);
  //
  // execute(standaloneConsumer, standaloneProducer,
  // addBlobUrlRef(EmbeddedActiveMq.createMessage(null), "blobUrl"), 2, 1000,
  // null);
  //
  // assertMessages(jms, 0, false);
  // assertEquals(2, activeMqBroker.messagesOnQueue(get(QUEUE)));
  //
  // }
  // finally {
  // activeMqBroker.destroy();
  // }
  // }

  public void testBlobConsumeWithNonBlob() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new BlobMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new TextMessageTranslator());
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);

      execute(standaloneConsumer, standaloneProducer, EmbeddedActiveMq.createMessage(null), jms);
      assertMessages(jms, 1, true);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testBlobProduceConsume() throws Exception {
    if (!ExternalResourcesHelper.isExternalServerAvailable()) {
      log.debug("Blob Server not available; skipping test");
      return;
    }

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new BlobMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BlobMessageTranslator("blobUrl"));
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);

      execute(standaloneConsumer, standaloneProducer, addBlobUrlRef(EmbeddedActiveMq.createMessage(null), "blobUrl"), jms);
      assertMessages(jms, 1, false);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testBlobProduceAndConsumeWithFileMessageFactory() throws Exception {
    if (!ExternalResourcesHelper.isExternalServerAvailable()) {
      log.debug("Blob Server not available; skipping test");
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new BlobMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BlobMessageTranslator("blobUrl"));
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);

      execute(standaloneConsumer, standaloneProducer,
          addBlobUrlRef(EmbeddedActiveMq.createMessage(new FileBackedMessageFactory()), "blobUrl"), jms);
      assertMessages(jms, 1, false);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testTopicRequestReply_Bug2277() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    TopicLoopback echo = new TopicLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
      echo.start();
      StandaloneRequestor standaloneProducer = new StandaloneRequestor(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));
      AdaptrisMessage msg = createMessage();
      msg.addMetadata(JmsConstants.JMS_ASYNC_STATIC_REPLY_TO, getName() + "_reply");
      ServiceCase.execute(standaloneProducer, msg);
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertNotNull(echo.getLastMessage().getJMSReplyTo());
      assertEquals("topic://" + getName() + "_reply", echo.getLastMessage().getJMSReplyTo().toString());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  public void testQueueRequestReply_Bug2277() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    QueueLoopback echo = new QueueLoopback(activeMqBroker, getName());
    try {
      activeMqBroker.start();
      echo.start();
      StandaloneRequestor standaloneProducer = new StandaloneRequestor(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PtpProducer(new ConfiguredProduceDestination(getName())));
      AdaptrisMessage msg = createMessage();
      msg.addMetadata(JmsConstants.JMS_ASYNC_STATIC_REPLY_TO, getName() + "_reply");
      ServiceCase.execute(standaloneProducer, msg);
      echo.waitFor(DEFAULT_TIMEOUT);
      assertNotNull(echo.getLastMessage());
      assertNotNull(echo.getLastMessage().getJMSReplyTo());
      assertEquals("queue://" + getName() + "_reply", echo.getLastMessage().getJMSReplyTo().toString());
    }
    finally {
      echo.stop();
      activeMqBroker.destroy();
    }
  }

  protected BasicActiveMqImplementation createVendorImpl() {
    return new BasicActiveMqImplementation();
  }

  private abstract class Loopback implements MessageListener {
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
      if (session != null) {
        session.close();
      }

      if (conn != null) {
        conn.stop();
      }
      if (conn != null) {
        conn.close();
      }
    }

    @Override
    public void onMessage(Message m) {
      try {
        log.debug("Got Message " + m.getJMSMessageID());
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
        log.error("Got exception ", e);

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

  private class TopicLoopback extends Loopback {
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
      if (subscriber != null) {
        subscriber.close();
      }
    }
  }

  private class QueueLoopback extends Loopback {
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
      if (receiver != null) {
        receiver.close();
      }
    }
  }

}
