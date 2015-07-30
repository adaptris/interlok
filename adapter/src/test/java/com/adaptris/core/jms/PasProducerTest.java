package com.adaptris.core.jms;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class PasProducerTest extends BasicJmsProducerCase {

  public PasProducerTest(String name) {
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
    PasProducer p = (PasProducer) ((StandaloneProducer) object).getProducer();
    return super.createBaseFileName(object);
  }

  private StandaloneProducer retrieveSampleConfig() {

    PasProducer p = new PasProducer();
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    p.setDestination(new ConfiguredProduceDestination("destination"));
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    NullCorrelationIdSource mcs = new NullCorrelationIdSource();
    p.setCorrelationIdSource(mcs);

    StandaloneProducer result = new StandaloneProducer();

    result.setConnection(c);
    result.setProducer(p);

    return result;
  }


  @Override
  protected DefinedJmsProducer createProducer(ConfiguredProduceDestination dest) {
    return new PasProducer(dest);
  }

  @Override
  protected JmsConsumerImpl createConsumer(ConfiguredConsumeDestination dest) {
    return new PasConsumer(dest);
  }

  @Override
  protected TopicLoopback createLoopback(EmbeddedActiveMq mq, String dest) {
    return new TopicLoopback(mq, dest);
  }

}
