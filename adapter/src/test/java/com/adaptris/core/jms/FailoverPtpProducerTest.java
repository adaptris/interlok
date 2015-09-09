package com.adaptris.core.jms;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;

public class FailoverPtpProducerTest extends FailoverJmsProducerCase {

  private static final Log LOG = LogFactory.getLog(FailoverPtpProducerTest.class);

  public FailoverPtpProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testBug845() throws Exception {
    Object input = retrieveObjectForSampleConfig();
    String xml = defaultMarshaller.marshal(input);
    StandaloneProducer output = (StandaloneProducer) defaultMarshaller.unmarshal(xml);
    FailoverJmsConnection unmarshalled = (FailoverJmsConnection) output.getConnection();
    assertEquals("Connection count == 2", 2, unmarshalled.getConnections().size());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createFailoverConfigExample(true), new PtpProducer(
        new ConfiguredProduceDestination("targetQueue")));

  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-Failover";
  }

  static FailoverJmsConnection createFailoverConfigExample(boolean isPtp) {
    FailoverJmsConnection result = null;
    result = new FailoverJmsConnection(new ArrayList<JmsConnection>(Arrays.asList(new JmsConnection[]
    {
        new JmsConnection(new BasicActiveMqImplementation("tcp://SomeUnclusteredBroker:9999")),
        new JmsConnection(new BasicActiveMqImplementation("tcp://SomeUnclusteredBroker:9998"))
    })));
    result.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    return result;
  }

}
