package com.adaptris.core.jms;

import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.DEFAULT_FILE_SUFFIX;
import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.DEFAULT_XML_COMMENT;
import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.createJndiVendorImpExample;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;

public class JndiExtraConfigPasProducerTest extends JmsProducerCase {

  private JmsConnection connection;
  private PasProducer producer;

  public JndiExtraConfigPasProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    connection = new JmsConnection();
    producer = new PasProducer();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createJndiVendorImpExample(new JmsConnection()), new PasProducer(
        new ConfiguredProduceDestination("jndiReferenceToTopic")));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + DEFAULT_FILE_SUFFIX;
  }

  /**
   * This is to spoof the round trip testing.
   */
  @Override
  protected Object retrieveObjectForCastorRoundTrip() {
    return new StandaloneProducer(new JmsConnection(), new PasProducer());
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }
}
