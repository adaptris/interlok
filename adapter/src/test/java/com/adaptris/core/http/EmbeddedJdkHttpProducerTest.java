/*
 * $RCSfile: JdkHttpProducerTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/05/22 14:11:00 $
 * $Author: lchan $
 */
package com.adaptris.core.http;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

@SuppressWarnings("all")
public class EmbeddedJdkHttpProducerTest extends HttpProducerExample {
  private static final String TEXT_COMPLICATED = "text/complicated";
  private static final String CONTENT_TYPE = "content.type";
  private static final String JETTY_HTTP_PORT = "jetty.http.port";
  private static final String URL_TO_POST_TO = "/url/to/post/to";
  private static final String TEXT = "ABCDEFG";

  private static final String JETTY_SIMPLE_XML = "jetty.simple.xml";

  public EmbeddedJdkHttpProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testSetSendMetadataAsHeaders() throws Exception {
    JdkHttpProducer p = new JdkHttpProducer();
    assertFalse(p.sendMetadataAsHeaders());
    p.setSendMetadataAsHeaders(true);
    assertNotNull(p.getSendMetadataAsHeaders());
    assertEquals(Boolean.TRUE, p.getSendMetadataAsHeaders());
    assertTrue(p.sendMetadataAsHeaders());
    p.setSendMetadataAsHeaders(false);
    assertNotNull(p.getSendMetadataAsHeaders());
    assertEquals(Boolean.FALSE, p.getSendMetadataAsHeaders());
    assertFalse(p.sendMetadataAsHeaders());
  }

  public void testSetHandleRedirection() throws Exception {
    JdkHttpProducer p = new JdkHttpProducer();
    assertFalse(p.handleRedirection());
    p.setHandleRedirection(true);
    assertNotNull(p.getHandleRedirection());
    assertEquals(Boolean.TRUE, p.getHandleRedirection());
    assertTrue(p.handleRedirection());
    p.setHandleRedirection(false);
    assertNotNull(p.getHandleRedirection());
    assertEquals(Boolean.FALSE, p.getHandleRedirection());
    assertFalse(p.handleRedirection());
  }

  public void testSetIgnoreServerResponse() throws Exception {
    JdkHttpProducer p = new JdkHttpProducer();
    assertFalse(p.ignoreServerResponseCode());
    p.setIgnoreServerResponseCode(true);
    assertNotNull(p.getIgnoreServerResponseCode());
    assertEquals(Boolean.TRUE, p.getIgnoreServerResponseCode());
    assertTrue(p.ignoreServerResponseCode());
    p.setIgnoreServerResponseCode(false);
    assertNotNull(p.getIgnoreServerResponseCode());
    assertEquals(Boolean.FALSE, p.getIgnoreServerResponseCode());
    assertFalse(p.ignoreServerResponseCode());
  }

  public void testInit() throws Exception {
    JdkHttpProducer p = new JdkHttpProducer();
    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
    p.setSendMetadataAsHeaders(true);
    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
    p.setSendMetadataAsHeaders(true);
    p.setSendMetadataRegexp("X-HTTP.*");
    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
  }

  public void testProduceWithContentTypeMetadata() throws Exception {
//  	EmbeddedJettyHelper.initJettyServer(PROPERTIES.getProperty(JETTY_SIMPLE_XML));
//
//    MockMessageProducer mock = new MockMessageProducer();
//    MessageConsumer mc = EmbeddedJettyHelper.createConsumer(URL_TO_POST_TO);
//    mc.setPreserveHeaders(true);
//    mc.setHeaderPrefix("");
//    EmbeddedConnection connection = new EmbeddedConnection();
//    Channel c = EmbeddedJettyHelper.createChannel(connection, EmbeddedJettyHelper.createWorkflow(mc, mock, new ServiceList()));
//    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination());
//    jdkHttp.setContentTypeKey(CONTENT_TYPE);
//    StandaloneProducer producer =  new StandaloneProducer(jdkHttp);
//    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
//    msg.addMetadata(CONTENT_TYPE, TEXT_COMPLICATED);
//    try {
//      c.requestStart();
//    	EmbeddedJettyHelper.startJettyServer();
//      start(producer);
//      producer.doService(msg);
//      waitForMessages(mock, 1);
//    }
//    finally {
//    	EmbeddedJettyHelper.stopJettyServer();
//    	EmbeddedJettyHelper.destroyJettyServer();
//      c.requestClose();
//      stop(producer);
//    }
//    assertEquals(1, mock.messageCount());
//    AdaptrisMessage m2 = mock.getMessages().get(0);
//    assertTrue(m2.containsKey("Content-Type"));
//    assertEquals(TEXT_COMPLICATED, m2.getMetadataValue("Content-Type"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JdkHttpProducer producer = new JdkHttpProducer();

    ConfiguredProduceDestination dest = new ConfiguredProduceDestination();
    dest.setDestination("http://myhost.com/url/to/post/to");

    producer.setDestination(dest);

    producer.setUserName("username");
    producer.setPassword("password");

    KeyValuePair header1 = new KeyValuePair("key1", "val1");
    KeyValuePair header2 = new KeyValuePair("key2", "val2");

    KeyValuePairSet headers = new KeyValuePairSet();
    headers.addKeyValuePair(header1);
    headers.addKeyValuePair(header2);

    producer.setAdditionalHeaders(headers);

    StandaloneProducer result = new StandaloneProducer(producer);

    return result;
  }

  private ConfiguredProduceDestination createProduceDestination() {
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("http://localhost:" + PROPERTIES.getProperty(JETTY_HTTP_PORT)
        + URL_TO_POST_TO);
    return d;
  }

}
