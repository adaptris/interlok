package com.adaptris.core.jms;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;

public class PtpConsumerTest extends JmsConsumerCase {

  public PtpConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
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
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    PtpConsumer pc = new PtpConsumer();

    ConfiguredConsumeDestination dest = new ConfiguredConsumeDestination();
    dest.setDestination("destination");

    pc.setDestination(dest);
    MetadataCorrelationIdSource mcs = new MetadataCorrelationIdSource();
    mcs.setMetadataKey("MetadataKey");
    pc.setCorrelationIdSource(mcs);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    StandaloneConsumer result = new StandaloneConsumer();
    result.setConnection(c);
    result.setConsumer(pc);

    return result;
  }
}
