package com.adaptris.core.jms.activemq;

import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.jndi.CachedDestinationJndiImplementation;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.MockMessageListener;

public class DestinationCacheJndiPasProducerTest extends JndiPasProducerCase {

  public DestinationCacheJndiPasProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CachedDestinationJndiImplementation createVendorImplementation() {
    return new CachedDestinationJndiImplementation();
  }

  public void testProduceAndConsumeWithCache() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";

    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(broker.getJndiPasConnection(recvVendorImp, false, queueName,
        topicName), new PasConsumer(new ConfiguredConsumeDestination(topicName)));
    MockMessageListener jms = new MockMessageListener();

    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getJndiPasConnection(sendVendorImp, false, queueName,
        topicName), new PasProducer(new ConfiguredProduceDestination(topicName)));
    try {
      broker.start();
      start(standaloneConsumer);
      start(standaloneProducer);
      standaloneProducer.doService(createMessage(null));
      standaloneProducer.doService(createMessage(null));
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    }
    finally {
      stop(standaloneProducer);
      stop(standaloneConsumer);
      broker.destroy();
    }
  }

}
