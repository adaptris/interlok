package com.adaptris.core.http;

import static com.adaptris.core.http.jetty.JettyHelper.createChannel;
import static com.adaptris.core.http.jetty.JettyHelper.createConsumer;
import static com.adaptris.core.http.jetty.JettyHelper.createWorkflow;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.PortManager;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.http.jetty.HttpConnection;
import com.adaptris.core.http.jetty.MessageConsumer;
import com.adaptris.core.http.jetty.ResponseProducer;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.metadata.PayloadFromMetadataService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

@SuppressWarnings("deprecation")
public class JdkHttpProducerTest extends HttpProducerExample {
  private static final String TEXT_PLAIN = "text/plain";
  private static final String CONTENT_TYPE = "content.type";
  private static final String JETTY_HTTP_PORT = "jetty.http.port";
  private static final String URL_TO_POST_TO = "/url/to/post/to";
  private static final String TEXT = "ABCDEFG";

  public JdkHttpProducerTest(String name) {
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

  public void testInit_MetadataRegexpString() throws Exception {
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
    assertEquals(RegexMetadataFilter.class, p.getMetadataFilter().getClass());
  }

  public void testInit_MetadataFilter() throws Exception {
    JdkHttpProducer p = new JdkHttpProducer();
    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
    p.setSendMetadataAsHeaders(true);
    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
    p.setSendMetadataAsHeaders(true);
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern("X-HTTP.*");
    p.setMetadataFilter(filter);

    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
  }

  public void testInit_MetadataRegexpString_MetadataFilter() throws Exception {
    JdkHttpProducer p = new JdkHttpProducer();
    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
    p.setSendMetadataAsHeaders(true);
    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
    p.setSendMetadataAsHeaders(true);
    p.setSendMetadataRegexp("ABC.*");
    start(new StandaloneProducer(p));
    stop(new StandaloneProducer(p));
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern("X-HTTP.*");
    p.setMetadataFilter(filter);

    assertEquals(RegexMetadataFilter.class, p.getMetadataFilter().getClass());
    assertFalse(filter.getIncludePatterns().contains("ABC.*"));
  }

  public void testProduceWithContentTypeMetadata() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);
    mc.setHeaderHandler(new MetadataHeaderHandler());
    mc.setHeaderPrefix("");
    HttpConnection jc = createConnection();

    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination(jc.getPort()));
    jdkHttp.setContentTypeKey(CONTENT_TYPE);
    StandaloneProducer producer = new StandaloneProducer(jdkHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(CONTENT_TYPE, "text/complicated");
    try {
      c.requestStart();
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    }
    finally {
      c.requestClose();
      stop(producer);
      PortManager.release(jc.getPort());
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertTrue(m2.containsKey("Content-Type"));
    assertEquals("text/complicated", m2.getMetadataValue("Content-Type"));
  }

  public void testProduce_WithMetadataMethod() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = createConnection();
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);
    ServiceList sl = new ServiceList();
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    sl.add(pms);
    sl.add(new StandaloneProducer(new ResponseProducer(200)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, sl));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination(jc.getPort()));
    jdkHttp.setMethodProvider(new MetadataRequestMethodProvider("httpMethod"));
    StandaloneRequestor producer = new StandaloneRequestor(jdkHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata("httpMethod", "get");
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      stop(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("GET", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(TEXT, msg.getStringPayload());
  }


  public void testRequest_GetMethod_ZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = createConnection();
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);
    ServiceList sl = new ServiceList();
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    sl.add(pms);
    sl.add(new StandaloneProducer(new ResponseProducer(200)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, sl));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination(jc.getPort()));
    jdkHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    StandaloneRequestor producer = new StandaloneRequestor(jdkHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    }
    finally {
      stop(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("GET", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(TEXT, msg.getStringPayload());
  }

  public void testRequest_PostMethod_ZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = createConnection();
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    workflow.getServiceCollection().add(pms);
    workflow.getServiceCollection().add(new StandaloneProducer(new ResponseProducer(200)));
    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination(jc.getPort()));
    jdkHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.POST));
    StandaloneRequestor producer = new StandaloneRequestor(jdkHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    }
    finally {
      stop(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("POST", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(TEXT, msg.getStringPayload());
  }

  public void testRequest_EmptyReply_LegacyMethod() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = createConnection();
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    ResponseProducer responder = new ResponseProducer(200);
    responder.setSendPayload(false);
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination(jc.getPort()));
    jdkHttp.setMethod("POST");
    StandaloneRequestor producer = new StandaloneRequestor(jdkHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      stop(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("POST", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(0, msg.getSize());
  }


  public void testRequest_EmptyReply() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = createConnection();
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    ResponseProducer responder = new ResponseProducer(200);
    responder.setSendPayload(false);
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination(jc.getPort()));
    jdkHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.POST));
    StandaloneRequestor producer = new StandaloneRequestor(jdkHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      stop(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("POST", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(0, msg.getSize());
  }

  public void testRequest_GetMethod_NonZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = createConnection();
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    workflow.getServiceCollection().add(pms);
    workflow.getServiceCollection().add(new StandaloneProducer(new ResponseProducer(200)));
    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination(jc.getPort()));
    jdkHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    StandaloneRequestor producer = new StandaloneRequestor(jdkHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    }
    finally {
      stop(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("GET", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(TEXT, msg.getStringPayload());
  }
  
  public void testRequest_GetMethod_NonZeroBytes_WithErrorResponse() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = createConnection();
    jc.setSendServerVersion(true);
    MessageConsumer mc = createConsumer(URL_TO_POST_TO);

    ServiceList services = new ServiceList();
    services.add(new PayloadFromMetadataService(TEXT));
    services.add(new StandaloneProducer(new ResponseProducer(401)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, services));
    JdkHttpProducer jdkHttp = new JdkHttpProducer(createProduceDestination(jc.getPort()));
    jdkHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    jdkHttp.setIgnoreServerResponseCode(true);
    jdkHttp.setReplyHttpHeadersAsMetadata(true);
    jdkHttp.setReplyMetadataPrefix("HTTP_");
    StandaloneRequestor producer = new StandaloneRequestor(jdkHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      start(c);
      start(producer);
      producer.doService(msg); // msg will now contain the response!
      waitForMessages(mock, 1);
    }
    finally {
      stop(c);
      stop(producer);
    }
    
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("GET", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(TEXT, msg.getStringPayload());
    assertEquals("401", msg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
    assertNotNull(msg.getMetadata("HTTP_Server"));
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

  private HttpConnection createConnection() {
    HttpConnection c = new HttpConnection();
    int port = PortManager.nextUnusedPort(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    c.setPort(port);
    c.getHttpProperties().add(new KeyValuePair(HttpConnection.HttpProperty.MaxIdleTime.name(), "30000"));
    return c;
  }

  private ConfiguredProduceDestination createProduceDestination(int port) {
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("http://localhost:" + port + URL_TO_POST_TO);
    return d;
  }

}
