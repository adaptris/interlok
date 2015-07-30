package com.adaptris.core.jms;

import static com.adaptris.core.jms.JmsConfig.MESSAGE_TRANSLATOR_LIST;
import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockMessageListener;

public class JmsConsumerTest extends JmsConsumerCase {

  public JmsConsumerTest(String name) {
    super(name);
  }


  public void testDurableTopicConsume() throws Exception {
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

  public void testTopicConsume() throws Exception {
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
  protected List retrieveObjectsForSampleConfig() {
    ArrayList result = new ArrayList();
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
