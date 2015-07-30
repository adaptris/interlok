/*
 * $RCSfile: ProduceOnlyProducerImpTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/04/17 15:24:05 $
 * $Author: lchan $
 */
package com.adaptris.core;

import com.adaptris.core.fs.FsMessageProducerTest;
import com.adaptris.core.fs.FsProducer;

public class ProduceOnlyProducerImpTest extends BaseCase {

  public ProduceOnlyProducerImpTest(java.lang.String testName) {
    super(testName);
  }

  private ProduceOnlyProducerImp producer;

  @Override
  protected void setUp() throws Exception {
    String destinationString = "/tgt";
    String baseString = PROPERTIES.getProperty(FsMessageProducerTest.BASE_KEY);
    // create producer
    producer = new FsProducer();
    producer.setDestination(new ConfiguredProduceDestination(baseString
        + destinationString));
    ((FsProducer) producer).setCreateDirs(true);
  }

  public void testRequestThrowsException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("dummy");
    start(producer);
    try {
      try {
        producer.request(msg);
        fail("Request reply succeeded");
      }
      catch (UnsupportedOperationException expected) {
        assertEquals("Request Reply is not supported", expected.getMessage());
      }
      try {
        producer.request(msg, 10000L);
        fail("Request reply succeeded");
      }
      catch (UnsupportedOperationException expected) {
        assertEquals("Request Reply is not supported", expected.getMessage());
      }
      try {
        producer.request(msg, new ConfiguredProduceDestination(PROPERTIES
            .getProperty(FsMessageProducerTest.BASE_KEY)));
        fail("Request reply succeeded");
      }
      catch (UnsupportedOperationException expected) {
        assertEquals("Request Reply is not supported", expected.getMessage());
      }
      try {
        producer.request(msg, new ConfiguredProduceDestination(PROPERTIES
            .getProperty(FsMessageProducerTest.BASE_KEY)), 10000L);
        fail("Request reply succeeded");
      }
      catch (UnsupportedOperationException expected) {
        assertEquals("Request Reply is not supported", expected.getMessage());
      }
    }
    finally {
      stop(producer);
    }

  }
}