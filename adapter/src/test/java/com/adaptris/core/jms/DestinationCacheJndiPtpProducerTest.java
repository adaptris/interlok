package com.adaptris.core.jms;

import static com.adaptris.core.jms.DestinationCacheJndiPtpConsumerTest.DEFAULT_FILE_SUFFIX;
import static com.adaptris.core.jms.DestinationCacheJndiPtpConsumerTest.DEFAULT_XML_COMMENT;
import static com.adaptris.core.jms.JndiPtpProducerTest.createJndiVendorImpExample;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.jndi.CachedDestinationJndiImplementation;

public class DestinationCacheJndiPtpProducerTest extends JmsProducerCase {

  public DestinationCacheJndiPtpProducerTest(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createJndiVendorImpExample(new CachedDestinationJndiImplementation(), new JmsConnection()),
        new PtpProducer(new ConfiguredProduceDestination("jndiReferenceToQueue")));
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
