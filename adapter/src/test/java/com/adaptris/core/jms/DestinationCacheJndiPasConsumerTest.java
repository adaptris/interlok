package com.adaptris.core.jms;

import static com.adaptris.core.jms.DestinationCacheJndiPtpConsumerTest.DEFAULT_FILE_SUFFIX;
import static com.adaptris.core.jms.DestinationCacheJndiPtpConsumerTest.DEFAULT_XML_COMMENT;
import static com.adaptris.core.jms.JndiPtpProducerTest.createJndiVendorImpExample;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.jndi.CachedDestinationJndiImplementation;

public class DestinationCacheJndiPasConsumerTest extends JmsConsumerCase {

  public DestinationCacheJndiPasConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneConsumer(createJndiVendorImpExample(new CachedDestinationJndiImplementation(), new JmsConnection()),
        new PasConsumer(new ConfiguredConsumeDestination("jndiReferenceToTopic")));
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
