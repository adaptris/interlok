package com.adaptris.core.jms;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class PtpProducerTest extends BasicJmsProducerCase {

  public PtpProducerTest(String name) {
    super(name);
  }


  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return retrieveSampleConfig();
  }

  @Override
  protected String createBaseFileName(Object object) {
    PtpProducer p = (PtpProducer) ((StandaloneProducer) object).getProducer();
    return super.createBaseFileName(object);
  }

  private StandaloneProducer retrieveSampleConfig() {
    JmsConnection c = configureForExamples(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
    c.setClientId(null);
    StandaloneProducer result = new StandaloneProducer(c, configureForExamples(new PtpProducer()));
    return result;
  }

  @Override
  protected DefinedJmsProducer createProducer(ConfiguredProduceDestination dest) {
    return new PtpProducer(dest);
  }

  @Override
  protected JmsConsumerImpl createConsumer(ConfiguredConsumeDestination dest) {
    return new PtpConsumer(dest);
  }

  @Override
  protected QueueLoopback createLoopback(EmbeddedActiveMq mq, String dest) {
    return new QueueLoopback(mq, dest);
  }
}
