package com.adaptris.core.jms;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;

public class PasConsumerTest extends JmsConsumerCase {

  public PasConsumerTest(String name) {
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
    JmsConsumerImpl p = (JmsConsumerImpl) ((StandaloneConsumer) object).getConsumer();
    return super.createBaseFileName(object);
  }

  protected StandaloneConsumer retrieveSampleConfig() {
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:16161"));
    PasConsumer pc = new PasConsumer();
    ConfiguredConsumeDestination dest = new ConfiguredConsumeDestination();
    dest.setDestination("destination");

    pc.setDestination(dest);
    NullCorrelationIdSource mcs = new NullCorrelationIdSource();
    pc.setCorrelationIdSource(mcs);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    pc.setSubscriptionId("subscription-id");

    StandaloneConsumer result = new StandaloneConsumer();
    result.setConnection(c);
    result.setConsumer(pc);

    return result;
  }
}
