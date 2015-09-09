package com.adaptris.core.socket;

import static com.adaptris.core.PortManager.nextUnusedPort;
import static com.adaptris.core.PortManager.release;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.stubs.MockMessageListener;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class TestSocketConsumer extends ConsumerCase {
  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "SocketConsumerExamples.baseDir";

  /**
   *
   */
  private static final String PAYLOAD = "The quick brown fox jumps over the lazy dog.";

  public TestSocketConsumer(String s) {
    super(s);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    TcpConsumeConnection tcp = new TcpConsumeConnection();
    SocketConsumer consumer = new SocketConsumer();
    consumer.setSendImmediateReply(false);
    consumer.setProtocolImplementation("my.implementation.of.com.adaptris.core." + "socket.Protocol");
    return new StandaloneConsumer(tcp, consumer);
  }

  @Override
  public String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--" + "\n-->";
  }

  public void testBasicSendReciver() throws Exception {
    Integer port = nextUnusedPort(19000);
    MockMessageListener stub = new MockMessageListener();
    StandaloneConsumer consumer = createConsumer(port);
    consumer.registerAdaptrisMessageListener(stub);
    StandaloneProducer producer = createProducer(port);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(PAYLOAD);
    execute(consumer, producer, msg, stub);
    release(port);
    assertEquals(1, stub.getMessages().size());
    assertEquals(PAYLOAD, stub.getMessages().get(0).getStringPayload());
  }

  private static StandaloneConsumer createConsumer(Integer port) {
    SocketConsumer consumer = new SocketConsumer();
    ConfiguredConsumeDestination ccd = new ConfiguredConsumeDestination("blah");
    consumer.setDestination(ccd);
    consumer.setProtocolImplementation(SimpleProtocol.class.getName());
    consumer.setSendImmediateReply(true);
    TcpConsumeConnection consumeConnection = new TcpConsumeConnection();
    consumeConnection.setListenPort(port);
    consumeConnection.setServerSocketTimeout(100);
    return new StandaloneConsumer(consumeConnection, consumer);
  }

  private static StandaloneProducer createProducer(Integer port) {
    SocketProducer producer = new SocketProducer();
    ConfiguredProduceDestination ccd = new ConfiguredProduceDestination("tcp://localhost:" + port);
    producer.setDestination(ccd);
    producer.setProtocolImplementation(SimpleProtocol.class.getName());
    return new StandaloneProducer(new TcpProduceConnection(), producer);
  }

}
