/*
 * $RCSfile: FailoverPasConsumerTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/02/03 13:30:44 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import static com.adaptris.core.jms.FailoverPtpProducerTest.createFailoverConfigExample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;

public class FailoverPasConsumerTest extends FailoverJmsConsumerCase {

  private static final Log LOG = LogFactory
      .getLog(FailoverPasConsumerTest.class);

  public FailoverPasConsumerTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
  }

  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneConsumer(createFailoverConfigExample(false),
        new PasConsumer(new ConfiguredConsumeDestination("TopicName")));
  }

  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-Failover";
  }
}
