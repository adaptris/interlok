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

import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.AdvancedActiveMqImplementationTest.createImpl;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.addBlobUrlRef;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.ChannelList;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.NullProcessingExceptionHandler;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.StartedState;
import com.adaptris.core.Workflow;
import com.adaptris.core.jms.AutoConvertMessageTranslator;
import com.adaptris.core.jms.BytesMessageTranslator;
import com.adaptris.core.jms.DefinedJmsProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConsumerCase;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.lifecycle.NonBlockingChannelStartStrategy;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.ExternalResourcesHelper;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.GuidGenerator;

public class BasicActiveMqConsumerTest extends JmsConsumerCase {

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
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-BasicActiveMQ";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    JmsConnection connection = new JmsConnection();
    PtpConsumer producer = new PtpConsumer();
    ConfiguredConsumeDestination dest = new ConfiguredConsumeDestination("destination");

    producer.setDestination(dest);
    UrlVendorImplementation vendorImpl = createImpl();
    vendorImpl.setBrokerUrl(BasicActiveMqImplementationTest.PRIMARY);
    connection.setUserName("BrokerUsername");
    connection.setPassword("BrokerPassword");
    connection.setVendorImplementation(vendorImpl);

    StandaloneConsumer result = new StandaloneConsumer();
    result.setConnection(connection);
    result.setConsumer(producer);

    return result;
  }

  @Test
  public void testTopicProduceAndConsume() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();

      PasConsumer consumer = new PasConsumer().withTopic(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
      AdaptrisMessage consumed = jms.getMessages().get(0);
      // Since it's not a workflow; this will be false.
      assertFalse(consumed.headersContainsKey(CoreConstants.MESSAGE_CONSUME_LOCATION));
    }
    finally {
      activeMqBroker.destroy();
    }

  }

  @Test
  public void testTopicProduceAndConsumeWithImplicitFallbackMessageTranslation() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PasConsumer consumer = new PasConsumer().withTopic(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);
      DefinedJmsProducer producer = new PasProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BytesMessageTranslator());
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testTopicProduceAndConsumeWithExplicitFallbackMessageTranslation() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PasConsumer consumer = new PasConsumer().withTopic(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new AutoConvertMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);
      DefinedJmsProducer producer = new PasProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BytesMessageTranslator());
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  private Channel createChannel(AdaptrisConnection cc, Workflow wf) throws Exception {
    Channel result = new MockChannel();
    result.setMessageErrorHandler(new NullProcessingExceptionHandler());
    result.setConsumeConnection(cc);
    result.getWorkflowList().add(wf);
    result.prepare();
    return result;
  }

  @Test
  public void testRedmine4902() throws Exception {
    testQueue_ProduceWhenConsumerStopped();
    testTopic_ProduceWhenConsumerStopped();
  }

  @Test
  public void testQueue_ProduceWhenConsumerStopped() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    PtpConsumer consumer = new PtpConsumer().withQueue(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandardWorkflow workflow = new StandardWorkflow();
    MockMessageProducer producer = new MockMessageProducer();
    workflow.setConsumer(consumer);
    workflow.setProducer(producer);
    Channel channel = createChannel(activeMqBroker.getJmsConnection(createVendorImpl()), workflow);

    try {
      activeMqBroker.start();

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PtpProducer(new ConfiguredProduceDestination(getName())));

      channel.requestStart();
      workflow.requestStop();
      start(standaloneProducer);
      AdaptrisMessage msg = createMessage(null);

      standaloneProducer.produce(msg);
      Thread.sleep(250);
      assertEquals(0, producer.messageCount());
      stop(standaloneProducer);
    }
    finally {
      channel.requestClose();
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testTopic_ProduceWhenConsumerStopped() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    PasConsumer consumer = new PasConsumer().withTopic(getName());
    consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
    StandardWorkflow workflow = new StandardWorkflow();
    MockMessageProducer producer = new MockMessageProducer();
    workflow.setConsumer(consumer);
    workflow.setProducer(producer);
    Channel channel = createChannel(activeMqBroker.getJmsConnection(createVendorImpl()), workflow);

    try {
      activeMqBroker.start();

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PasProducer(new ConfiguredProduceDestination(getName())));

      channel.requestStart();
      workflow.requestStop();
      start(standaloneProducer);
      AdaptrisMessage msg = createMessage(null);

      standaloneProducer.produce(msg);
      Thread.sleep(250);
      assertEquals(0, producer.messageCount());
      stop(standaloneProducer);
    }
    finally {
      channel.requestClose();
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testQueueProduceAndConsume() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer().withQueue(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");

      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()),
          new PtpProducer(new ConfiguredProduceDestination(getName())));

      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testQueueProduceAndConsumeWithImplicitFallbackMessageTranslation() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer().withQueue(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);
      DefinedJmsProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BytesMessageTranslator());
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testQueueProduceAndConsumeWithExplicitFallbackMessageTranslation() throws Exception {

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer().withQueue(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new AutoConvertMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);

      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);
      DefinedJmsProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BytesMessageTranslator());
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testBlobProduceConsume() throws Exception {
    if (!ExternalResourcesHelper.isExternalServerAvailable()) {
      log.debug("Blob Server not available; skipping test");
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer().withQueue(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new BlobMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BlobMessageTranslator("blobUrl"));
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);

      execute(standaloneConsumer, standaloneProducer, addBlobUrlRef(createMessage(null), "blobUrl"), jms);
      assertMessages(jms, 1, false);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testBlobProduceAndConsumeWithFileMessageFactory() throws Exception {
    if (!ExternalResourcesHelper.isExternalServerAvailable()) {
      log.debug("Blob Server not available; skipping test");
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer().withQueue(getName());
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");
      consumer.setMessageTranslator(new BlobMessageTranslator());
      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl()), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination(getName()));
      producer.setMessageTranslator(new BlobMessageTranslator("blobUrl"));
      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl()), producer);

      execute(standaloneConsumer, standaloneProducer, addBlobUrlRef(createMessage(new FileBackedMessageFactory()), "blobUrl"), jms);
      assertMessages(jms, 1, false);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testStartWithDurableSubscribers_WithNonBlockingChannelStrategy() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      Adapter adapter = createDurableSubsAdapter(getName(), activeMqBroker);
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), adapter.retrieveComponentState());
      adapter.requestClose();
      assertEquals(ClosedState.getInstance(), adapter.retrieveComponentState());
      adapter.getChannelList().setLifecycleStrategy(new NonBlockingChannelStartStrategy());
      adapter.requestStart();
      // Thread.sleep(1000);
      Channel c = adapter.getChannelList().getChannel(0);
      int maxAttempts = 100;
      int attempts = 0;
      while (attempts < maxAttempts && c.retrieveComponentState() != StartedState.getInstance()) {
        Thread.sleep(500);
        attempts++;
      }
      assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      adapter.requestClose();
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testBugzilla1363() throws Exception {
    testStartWithDurableSubscribers_WithNonBlockingChannelStrategy();
  }

  private Adapter createDurableSubsAdapter(String adapterName, EmbeddedActiveMq activeMqBroker) throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(adapterName);
    Channel c = new Channel();
    JmsConnection conn = activeMqBroker.getJmsConnection(createVendorImpl());
    conn.setClientId(MY_CLIENT_ID);
    c.setConsumeConnection(conn);
    StandardWorkflow swf = new StandardWorkflow();
    PasConsumer pasConsumer = new PasConsumer().withTopic(new GuidGenerator().safeUUID());
    pasConsumer.setDurable(true);
    pasConsumer.setSubscriptionId(MY_SUBSCRIPTION_ID);
    swf.setConsumer(pasConsumer);
    c.getWorkflowList().add(swf);
    ChannelList cl = new ChannelList();
    cl.addChannel(c);
    adapter.setChannelList(cl);
    return adapter;
  }

  protected BasicActiveMqImplementation createVendorImpl() {
    return new BasicActiveMqImplementation();
  }
}
