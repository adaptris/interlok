package com.adaptris.core.jms.activemq;

import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;

import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.stubs.MockMessageListener;

public class FailoverPtpProducerTest extends BaseCase {

  public FailoverPtpProducerTest(String name) {
    super(name);
  }

  public void testProduceAndConsume() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(broker.getFailoverJmsConnection(true), new PtpConsumer(
        new ConfiguredConsumeDestination(getName())));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getFailoverJmsConnection(true), new PtpProducer(
        new ConfiguredProduceDestination(getName())));
    try {
      broker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      broker.destroy();
    }
  }

}
