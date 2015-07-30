package com.adaptris.core.jms;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.util.TimeInterval;

public class BasicPtpConsumerActiveErorHandlerTest extends JmsConsumerCase {

  static final String DEFAULT_XML_COMMENT = "\n<!--"
      + "\nThis is just an example consumer with an ActiveJmsConnectionErrorHandler"
      + "\nThis type of error handler simply uses a temporary queue (or topic) and periodically produces a"
      + "\nnon-persistent message with a TTL of 5000ms; if this fails, then the connection"
      + "\nis deemed to have failed and the connection restarted." + "\n-->\n";

  public BasicPtpConsumerActiveErorHandlerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JmsConnection p = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    ActiveJmsConnectionErrorHandler erHandler = new ActiveJmsConnectionErrorHandler();
    erHandler.setCheckInterval(new TimeInterval(30L, TimeUnit.SECONDS));
    p.setConnectionErrorHandler(erHandler);
    return new StandaloneConsumer(p, new PtpConsumer(new ConfiguredConsumeDestination("TheQueueToConsumeFrom")));
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
