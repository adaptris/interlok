package com.adaptris.core.jms;

import static com.adaptris.core.jms.BasicPtpConsumerActiveErorHandlerTest.DEFAULT_XML_COMMENT;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.util.TimeInterval;

public class BasicPtpProducerActiveErrorHandlerTest extends JmsProducerCase {
  static final String DEFAULT_FILE_SUFFIX = "-JNDI";

  public BasicPtpProducerActiveErrorHandlerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    JmsConnection p = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    ActiveJmsConnectionErrorHandler erHandler = new ActiveJmsConnectionErrorHandler();
    erHandler.setCheckInterval(new TimeInterval(30L, TimeUnit.SECONDS));
    p.setConnectionErrorHandler(erHandler);
    return new StandaloneProducer(p, new PtpProducer(new ConfiguredProduceDestination("TheQueueToProduceTo")));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-" + ActiveJmsConnectionErrorHandler.class.getSimpleName();
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }
}
