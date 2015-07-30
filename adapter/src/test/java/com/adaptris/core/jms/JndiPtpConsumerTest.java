/*
 * $RCSfile: JndiPtpConsumerTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/02/03 13:31:14 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import static com.adaptris.core.jms.JndiPtpProducerTest.DEFAULT_FILE_SUFFIX;
import static com.adaptris.core.jms.JndiPtpProducerTest.DEFAULT_XML_COMMENT;
import static com.adaptris.core.jms.JndiPtpProducerTest.createJndiVendorImpExample;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;

public class JndiPtpConsumerTest extends JmsConsumerCase {



  public JndiPtpConsumerTest(String name) {
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
