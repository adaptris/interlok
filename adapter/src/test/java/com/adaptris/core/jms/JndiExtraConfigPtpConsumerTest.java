package com.adaptris.core.jms;

import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.DEFAULT_FILE_SUFFIX;
import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.DEFAULT_XML_COMMENT;
import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.createJndiVendorImpExample;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;

public class JndiExtraConfigPtpConsumerTest extends JmsConsumerCase {



  public JndiExtraConfigPtpConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneConsumer(
        createJndiVendorImpExample(new JmsConnection()), new PtpConsumer(
        new ConfiguredConsumeDestination("jndiReferenceToQueue")));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + DEFAULT_FILE_SUFFIX;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }
}
