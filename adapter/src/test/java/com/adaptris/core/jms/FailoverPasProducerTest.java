package com.adaptris.core.jms;

import static com.adaptris.core.jms.FailoverPtpProducerTest.createFailoverConfigExample;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;

public class FailoverPasProducerTest extends FailoverJmsProducerCase {

  public FailoverPasProducerTest(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createFailoverConfigExample(false),
        new PasProducer(new ConfiguredProduceDestination("TopicName")));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-Failover";
  }
}
