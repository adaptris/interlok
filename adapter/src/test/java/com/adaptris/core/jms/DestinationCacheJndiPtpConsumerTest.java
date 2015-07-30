package com.adaptris.core.jms;

import static com.adaptris.core.jms.JndiPtpProducerTest.createJndiVendorImpExample;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.jndi.CachedDestinationJndiImplementation;

public class DestinationCacheJndiPtpConsumerTest extends JmsConsumerCase {

  static final String DEFAULT_XML_COMMENT = "<!-- Note that using CachedDestinationJndiImplementation means that \n"
      + "\nthe JmsConnection fields broker-host, broker-url, port are ignored."
      + "\nCheck your JNDI provider documentation for the correct values."
      + "\nAlso please check the javadocs for more information about the nature of the cache" + "\n-->\n";

  static final String DEFAULT_FILE_SUFFIX = "-CachedDestination-JNDI";

  public DestinationCacheJndiPtpConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneConsumer(createJndiVendorImpExample(new CachedDestinationJndiImplementation(), new JmsConnection()),
        new PtpConsumer(new ConfiguredConsumeDestination("jndiReferenceToQueue")));
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
    return new StandaloneConsumer(new JmsConnection(), new PtpConsumer());
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }
}
