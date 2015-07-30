/*
 * $RCSfile: FailoverPasProducerTest.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/02/17 12:33:10 $
 * $Author: lchan $
 */
package com.adaptris.core.jms.activemq;

import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;

import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.stubs.MockMessageListener;

public class FailoverPasProducerTest extends BaseCase {

  public FailoverPasProducerTest(String name) {
    super(name);
  }

  public void testProduceAndConsume() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(broker.getFailoverJmsConnection(false), new PasConsumer(
        new ConfiguredConsumeDestination(getName())));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getFailoverJmsConnection(false), new PasProducer(
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
