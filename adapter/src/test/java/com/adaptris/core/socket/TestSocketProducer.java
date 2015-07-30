/*
 * $RCSfile: TestSocketProducer.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/14 09:46:34 $
 * $Author: lchan $
 */
package com.adaptris.core.socket;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.StandaloneProducer;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class TestSocketProducer extends ProducerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "SocketProducerExamples.baseDir";

  public TestSocketProducer(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    TcpProduceConnection tcp = new TcpProduceConnection();
    SocketProducer producer = new SocketProducer();
    producer.setDestination(new ConfiguredProduceDestination("tcp://localhost:9099"));
    producer
        .setProtocolImplementation("my.implementation.of.com.adaptris.core."
            + "socket.Protocol");
    return new StandaloneProducer(tcp, producer);
  }

  @Override
  public String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--" + "\n-->";
  }
}
