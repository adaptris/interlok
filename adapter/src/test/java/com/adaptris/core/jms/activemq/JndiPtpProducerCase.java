package com.adaptris.core.jms.activemq;

import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;

import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.jndi.SimpleFactoryConfiguration;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public abstract class JndiPtpProducerCase extends BaseCase {

  public JndiPtpProducerCase(String name) {
    super(name);
  }

  protected abstract StandardJndiImplementation createVendorImplementation();

  public void testProduceAndConsume() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPtpConnection(recvVendorImp, false,
        queueName, topicName), new PtpConsumer(new ConfiguredConsumeDestination(queueName)));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, false,
        queueName, topicName), new PtpProducer(new ConfiguredProduceDestination(queueName)));
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testProduceAndConsume_ExtraConfig() throws Exception {
    SimpleFactoryConfiguration sfc = new SimpleFactoryConfiguration();
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("ClientID", "testProduceAndConsume_ExtraConfig"));
    kvps.add(new KeyValuePair("UseCompression", "true"));
    sfc.setProperties(kvps);
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    sendVendorImp.setExtraFactoryConfiguration(sfc);
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPtpConnection(recvVendorImp, false,
        queueName, topicName), new PtpConsumer(new ConfiguredConsumeDestination(queueName)));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, false,
        queueName, topicName), new PtpProducer(new ConfiguredProduceDestination(queueName)));
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testProduceAndConsumeUsingJndiOnly() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";

    PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(queueName));
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPtpConnection(recvVendorImp, true,
        queueName, topicName), new PtpConsumer(new ConfiguredConsumeDestination(queueName)));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, true,
        queueName, topicName), new PtpProducer(new ConfiguredProduceDestination(queueName)));
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms);
    }
    finally {
      activeMqBroker.destroy();

    }
  }

  public void testProduceJndiOnlyObjectNotFound() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, true,
        queueName, topicName), new PtpProducer(new ConfiguredProduceDestination(this.getClass().getSimpleName())));
    try {
      activeMqBroker.start();
      start(standaloneProducer);
      standaloneProducer.produce(createMessage(null));
      fail("Expected ProduceException");
    }
    catch (ProduceException e) {
      log.trace("Expected Exception", e);
    }
    finally {
      stop(standaloneProducer);
      activeMqBroker.destroy();
    }
  }
}
