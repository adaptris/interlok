/*
 * $RCSfile: HttpConsumerTest.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/04/15 12:01:30 $
 * $Author: lchan $
 */
package com.adaptris.core.http.jetty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.PortManager;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.http.AdapterResourceAuthenticator;
import com.adaptris.core.http.HttpConsumerExample;
import com.adaptris.core.http.HttpProducer;
import com.adaptris.core.http.JdkHttpProducer;
import com.adaptris.core.http.MetadataHeaderHandler;
import com.adaptris.core.http.MetadataParameterHandler;
import com.adaptris.core.http.ObjectMetadataHeaderHandler;
import com.adaptris.core.http.ObjectMetadataParameterHandler;
import com.adaptris.core.http.jetty.HttpConnection.HttpProperty;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.AdaptrisMessageStub;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StaticMockMessageProducer;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public class HttpConsumerTest extends HttpConsumerExample {

  static final String METADATA_VALUE2 = "value2";
  static final String METADATA_VALUE1 = "value1";
  static final String IGNORED_METADATA = "WillNotBeSent";
  static final String X_HTTP_KEY2 = "X-HTTP-Key2";
  static final String X_HTTP_KEY1 = "X-HTTP-Key1";
  static final String CONTENT_TYPE_METADATA_KEY = "content.type";

  private static final String JETTY_HTTP_PORT = "jetty.http.port";
  private static final String JETTY_USER_REALM = "jetty.user.realm.properties";
  private static final String URL_TO_POST_TO = "/url/to/post/to";
  private static final String XML_PAYLOAD = "<root><document>value</document></root>";

  protected HttpProducer httpProducer;

  public HttpConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    httpProducer = createProducer();
  }

  @Override
  protected void tearDown() throws Exception {
    stop(httpProducer);
  }

  public void testSetAdditionalDebug() throws Exception {
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    consumer.setAdditionalDebug(null);
    assertNull(consumer.getAdditionalDebug());
    assertEquals(false, consumer.additionalDebug());
    consumer.setAdditionalDebug(Boolean.TRUE);
    assertNotNull(consumer.getAdditionalDebug());
    assertEquals(true, consumer.additionalDebug());
  }

  public void testSetMaxWaitTime() throws Exception {
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    consumer.setMaxWaitTime(null);
    assertNull(consumer.getMaxWaitTime());
    assertEquals(new TimeInterval(600L, TimeUnit.SECONDS).toMilliseconds(), consumer.maxWaitTime());
    TimeInterval t = new TimeInterval(100L, TimeUnit.SECONDS);
    consumer.setMaxWaitTime(t);
    assertEquals(t, consumer.getMaxWaitTime());
    assertEquals(t.toMilliseconds(), consumer.maxWaitTime());
  }

  public void testConnection_NonDefaults() throws Exception {
    HttpConnection connection = createConnection(null);
    connection.getHttpProperties().addKeyValuePair(new KeyValuePair(HttpProperty.MaxIdleTime.name(), "30000"));
    connection.getHttpProperties().addKeyValuePair(new KeyValuePair(HttpProperty.AcceptorPriorityOffset.name(), "0"));
    connection.getHttpProperties().addKeyValuePair(new KeyValuePair(HttpProperty.Acceptors.name(), "10"));
    connection.getHttpProperties().addKeyValuePair(new KeyValuePair(HttpProperty.AcceptQueueSize.name(), "10"));
    connection.getHttpProperties().addKeyValuePair(new KeyValuePair(HttpProperty.SoLingerTime.name(), "-1"));
    connection.getHttpProperties().addKeyValuePair(new KeyValuePair(HttpProperty.LowResourcesMaxIdleTime.name(), "30000"));
    connection.getHttpProperties().addKeyValuePair(new KeyValuePair(HttpProperty.ResolveNames.name(), "true"));
    connection.getHttpProperties().addKeyValuePair(new KeyValuePair(HttpProperty.ReuseAaddress.name(), "true"));
    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), new MockMessageProducer());
    try {
      channel.requestStart();
    }
    finally {
      channel.requestClose();
    }

  }

  public void testChannelStarted_WorkflowStopped() throws Exception {
    HttpConnection connection = createConnection(null);
    MessageConsumer consumer1 = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandardWorkflow workflow1 = new StandardWorkflow();
    workflow1.setConsumer(consumer1);
    workflow1.getServiceCollection().add(new StandaloneProducer(new ResponseProducer(HttpURLConnection.HTTP_OK)));
    Channel channel = JettyHelper.createChannel(connection, workflow1);
    channel.isEnabled(new LicenseStub());
    try {
      channel.requestStart();
      AdaptrisMessage msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg1.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg1, createProduceDestination(connection.getPort()));
      assertEquals("200", reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      workflow1.requestClose();

      AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg1.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      AdaptrisMessage reply2 = httpProducer.request(msg2, createProduceDestination(connection.getPort()));
      assertEquals("404", reply2.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));

    }
    finally {
      channel.requestClose();
    }
  }

  public void testChannelStarted_MultipleWorkflows_OneWorkflowStopped() throws Exception {
    HttpConnection connection = createConnection(null);

    MessageConsumer consumer1 = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandardWorkflow workflow1 = new StandardWorkflow();
    workflow1.setConsumer(consumer1);
    workflow1.getServiceCollection().add(new StandaloneProducer(new ResponseProducer(HttpURLConnection.HTTP_OK)));
    Channel channel = JettyHelper.createChannel(connection, workflow1);

    MessageConsumer consumer2 = JettyHelper.createConsumer("/some/other/urlmapping/");
    StandardWorkflow workflow2 = new StandardWorkflow();
    workflow2.setConsumer(consumer2);
    workflow2.getServiceCollection().add(new StandaloneProducer(new ResponseProducer(HttpURLConnection.HTTP_OK)));
    channel.getWorkflowList().add(workflow2);

    channel.isEnabled(new LicenseStub());
    try {
      channel.requestStart();
      AdaptrisMessage msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg1.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg1, createProduceDestination(connection.getPort()));
      assertEquals("200", reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      workflow2.requestClose();

      // Stopping Workflow 2 means nothing, workflow1 should still be working!
      AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg1.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      AdaptrisMessage reply2 = httpProducer.request(msg2, createProduceDestination(connection.getPort()));
      assertEquals("200", reply2.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));

    }
    finally {
      channel.requestClose();
    }
  }

  public void testPoolingWorkflow_WithInterceptor() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    PoolingWorkflow workflow = new PoolingWorkflow();
    ResponseProducer responder = new ResponseProducer(HttpURLConnection.HTTP_OK);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    workflow.addInterceptor(new JettyPoolingWorkflowInterceptor());
    Channel channel = JettyHelper.createChannel(connection, workflow);
    channel.isEnabled(new LicenseStub());
    try {
      channel.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
    }
  }

  public void testPoolingWorkflow_WithoutInterceptor() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setShutdownWaitTime(new TimeInterval(5L, TimeUnit.SECONDS));
    ResponseProducer responder = new ResponseProducer(HttpURLConnection.HTTP_OK);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    Channel channel = JettyHelper.createChannel(connection, workflow);
    channel.isEnabled(new LicenseStub());
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      // Because of redmineID #4715 it should just "return immediatel" which flushes the stream so there's no content.
      assertEquals("Reply Payloads", "", reply.getStringPayload());
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
    }
  }

  public void testConsumeWorkflow_NoPreserveHeaders() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer("/*");
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);
      assertFalse(receivedMsg.containsKey("Content-Type"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testConsumeWorkflow_IncorrectMethod() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.getDestination().setFilterExpression("GET,HEAD,OPTIONS");
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      // we should default to post, so restricted to GET/HEAD/OPTIONS will cause a 405...
      httpProducer.setIgnoreReplyMetadata(false);
      if (httpProducer instanceof JdkHttpProducer) {
        ((JdkHttpProducer) httpProducer).setReplyHttpHeadersAsMetadata(true);
        ((JdkHttpProducer) httpProducer).setReplyMetadataPrefix(null);
      }
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("405", reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      log.info("ZZLC " + reply.toString());
      if (httpProducer instanceof JdkHttpProducer) {
        assertEquals("GET,HEAD,OPTIONS", reply.getMetadataValue("Allow"));
      }
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testConsumeWorkflow_PreserveHeaders() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.setHeaderHandler(new MetadataHeaderHandler());
    consumer.setHeaderPrefix("Http_Header_");
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);
      assertEquals("text/xml", receivedMsg.getMetadataValue("Http_Header_Content-Type"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testConsumeWorkflow_PreserveParams_NoPrefix() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.setHeaderHandler(new MetadataHeaderHandler());
    consumer.setHeaderPrefix("Http_Header_");
    consumer.setParameterHandler(new MetadataParameterHandler());
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      ConfiguredProduceDestination dest = createProduceDestination(connection.getPort());
      dest.setDestination(dest.getDestination() + "?queryParam1=1&queryParam2=2&queryParam3=3");
      AdaptrisMessage reply = httpProducer.request(msg, dest);
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);

      assertTrue(receivedMsg.containsKey(CoreConstants.JETTY_QUERY_STRING));
      assertEquals("queryParam1=1&queryParam2=2&queryParam3=3", receivedMsg.getMetadataValue(CoreConstants.JETTY_QUERY_STRING));
      assertEquals("1", receivedMsg.getMetadataValue("queryParam1"));
      assertEquals("2", receivedMsg.getMetadataValue("queryParam2"));
      assertEquals("3", receivedMsg.getMetadataValue("queryParam3"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testConsumeWorkflow_PreserveObjectParams_NoPrefix() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.setHeaderHandler(new ObjectMetadataHeaderHandler());
    consumer.setHeaderPrefix("Http_Header_");
    consumer.setParameterHandler(new ObjectMetadataParameterHandler());
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      ConfiguredProduceDestination dest = createProduceDestination(connection.getPort());
      dest.setDestination(dest.getDestination() + "?queryParam1=1&queryParam2=2&queryParam3=3");
      AdaptrisMessage reply = httpProducer.request(msg, dest);
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);

      System.out.println("XXX - " + receivedMsg);

      assertTrue(receivedMsg.containsKey(CoreConstants.JETTY_QUERY_STRING));
      assertEquals("queryParam1=1&queryParam2=2&queryParam3=3", receivedMsg.getMetadataValue(CoreConstants.JETTY_QUERY_STRING));
      assertEquals("1", receivedMsg.getObjectMetadata().get("queryParam1"));
      assertEquals("2", receivedMsg.getObjectMetadata().get("queryParam2"));
      assertEquals("3", receivedMsg.getObjectMetadata().get("queryParam3"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testConsumeWorkflow_PreserveParams_WithPrefix() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.setHeaderHandler(new MetadataHeaderHandler());
    consumer.setHeaderPrefix("Http_Header_");
    consumer.setParameterHandler(new MetadataParameterHandler());
    consumer.setParamPrefix("Http_Param_");
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      ConfiguredProduceDestination dest = createProduceDestination(connection.getPort());
      dest.setDestination(dest.getDestination() + "?queryParam1=1&queryParam2=2&queryParam3=3");
      AdaptrisMessage reply = httpProducer.request(msg, dest);
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);
      assertTrue(receivedMsg.containsKey(CoreConstants.JETTY_QUERY_STRING));
      assertEquals("queryParam1=1&queryParam2=2&queryParam3=3", receivedMsg.getMetadataValue(CoreConstants.JETTY_QUERY_STRING));
      assertFalse(receivedMsg.containsKey("Http_Header_queryParam1"));
      assertFalse(receivedMsg.containsKey("Http_Header_queryParam2"));
      assertFalse(receivedMsg.containsKey("Http_Header_queryParam3"));
      assertEquals("1", receivedMsg.getMetadataValue("Http_Param_queryParam1"));
      assertEquals("2", receivedMsg.getMetadataValue("Http_Param_queryParam2"));
      assertEquals("3", receivedMsg.getMetadataValue("Http_Param_queryParam3"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testConsumeWorkflow_PreserveObjectParams_WithPrefix() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.setHeaderHandler(new ObjectMetadataHeaderHandler());
    consumer.setHeaderPrefix("Http_Header_");
    consumer.setParameterHandler(new ObjectMetadataParameterHandler());
    consumer.setParamPrefix("Http_Param_");
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      ConfiguredProduceDestination dest = createProduceDestination(connection.getPort());
      dest.setDestination(dest.getDestination() + "?queryParam1=1&queryParam2=2&queryParam3=3");
      AdaptrisMessage reply = httpProducer.request(msg, dest);
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);
      assertTrue(receivedMsg.containsKey(CoreConstants.JETTY_QUERY_STRING));
      assertEquals("queryParam1=1&queryParam2=2&queryParam3=3", receivedMsg.getMetadataValue(CoreConstants.JETTY_QUERY_STRING));
      assertFalse(receivedMsg.getObjectMetadata().containsKey("Http_Header_queryParam1"));
      assertFalse(receivedMsg.getObjectMetadata().containsKey("Http_Header_queryParam2"));
      assertFalse(receivedMsg.getObjectMetadata().containsKey("Http_Header_queryParam3"));
      assertEquals("1", receivedMsg.getObjectMetadata().get("Http_Param_queryParam1"));
      assertEquals("2", receivedMsg.getObjectMetadata().get("Http_Param_queryParam2"));
      assertEquals("3", receivedMsg.getObjectMetadata().get("Http_Param_queryParam3"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testBasicConsumeWorkflow_ConsumeDestinationContainsInvalidURL() throws Exception {
    // {} won't be valid for a URL, so this should immediately fail.
    HttpConnection con = createConnection(null);
    Channel c = JettyHelper.createChannel(con, JettyHelper.createConsumer(URL_TO_POST_TO + "?token={}"),
        new MockMessageProducer());
    c.requestInit();
    try {
      c.requestStart();
      fail();
    }
    catch (CoreException expected) {
      expected.printStackTrace();
    }
    finally {
      stop(c);
      PortManager.release(con.getPort());
    }
  }

  public void testBasicConsumeWorkflow_ConsumeDestinationContainsURL() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();

    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer("http://localhost:8080" + URL_TO_POST_TO),
        mockProducer);
    try {
      channel.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testBasicConsumeWorkflow() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();

    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testBasicConsumeWorkflow_AcrossRestarts() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();

    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();
      channel.requestClose();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testBasicConsumeWorkflow_UpdatedConfig() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();

    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    Workflow workflow = JettyHelper.createWorkflow(JettyHelper.createConsumer(URL_TO_POST_TO), mock2);
    try {
      channel.requestStart();
      channel.requestClose();

      channel.getWorkflowList().clear();
      ;
      channel.getWorkflowList().add(workflow);
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mock2);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  protected AdaptrisMessage doAssertions(MockMessageProducer mockProducer) {
    assertEquals("Only 1 message consumed", 1, mockProducer.getMessages().size());
    AdaptrisMessage msg = mockProducer.getMessages().get(0);
    assertEquals("Consumed Payload", XML_PAYLOAD, msg.getStringPayload());
    assertTrue(msg.containsKey(CoreConstants.JETTY_URI));
    assertEquals(URL_TO_POST_TO, msg.getMetadataValue(CoreConstants.JETTY_URI));
    assertTrue(msg.containsKey(CoreConstants.JETTY_URL));
    Map objMetadata = msg.getObjectMetadata();
    assertNotNull(objMetadata.get(CoreConstants.JETTY_REQUEST_KEY));
    assertNotNull(objMetadata.get(CoreConstants.JETTY_RESPONSE_KEY));
    return msg;
  }

  public void testBug1549() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();

    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    consumer.setHeaderHandler(new MetadataHeaderHandler());
    consumer.setHeaderPrefix("");

    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      msg.addMetadata(X_HTTP_KEY1, METADATA_VALUE1);
      msg.addMetadata(X_HTTP_KEY2, METADATA_VALUE2);
      msg.addMetadata(IGNORED_METADATA, METADATA_VALUE1);
      httpProducer.setSendMetadataAsHeaders(true);
      RegexMetadataFilter filter = new RegexMetadataFilter();
      filter.addIncludePattern("X-HTTP.*");
      httpProducer.setMetadataFilter(filter);
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mockProducer);

      AdaptrisMessage consumedMsg = mockProducer.getMessages().get(0);
      assertEquals("Consumed Payload", XML_PAYLOAD, consumedMsg.getStringPayload());
      assertTrue(consumedMsg.containsKey(X_HTTP_KEY1));
      assertEquals(METADATA_VALUE1, consumedMsg.getMetadataValue(X_HTTP_KEY1));

      assertTrue(consumedMsg.containsKey(X_HTTP_KEY2));
      assertEquals(METADATA_VALUE2, consumedMsg.getMetadataValue(X_HTTP_KEY2));

      assertFalse(consumedMsg.containsKey(IGNORED_METADATA));

    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      PortManager.release(connection.getPort());

    }
  }

  public void testBasicConsumeWithCustomMessageImpl() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      consumer.setMessageFactory(new StubMessageFactory());
      adapter.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mockProducer);
      assertEquals("Consumed Message Java Class", AdaptrisMessageStub.class, mockProducer.getMessages().get(0).getClass());
    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testDestinationNotFound() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata("content.type", "text/xml");
      stop(httpProducer);
      ConfiguredProduceDestination pd = createProduceDestination(connection.getPort());
      pd.setDestination(pd.getDestination() + "/some/unknownURL");
      AdaptrisMessage reply = httpProducer.request(msg, pd);
      assertEquals("0 message consumed", 0, mockProducer.getMessages().size());
      assertTrue("Reply Response Code present", reply.containsKey(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      int rc = Integer.valueOf(reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE)).intValue();
      assertEquals("Reply Response Code Value", HttpURLConnection.HTTP_NOT_FOUND, rc);

    }
    catch (ProduceException e) {
      if (!(e.getCause() instanceof FileNotFoundException)) {
        throw e;
      }
    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  public void testConsume_WithACL() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HashUserRealmProxy hr = new HashUserRealmProxy();
    hr.setFilename(PROPERTIES.getProperty(JETTY_USER_REALM));

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");

    hr.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(hr);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);

    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata("content.type", "text/xml");
      httpProducer.setUserName("user");
      httpProducer.setPassword("password");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      Thread.currentThread().setName(threadName);
      PortManager.release(connection.getPort());
      assertEquals(0, AdapterResourceAuthenticator.getInstance().currentAuthenticators().size());
    }
  }

  public void testConsume_WithACL_UnprotectedURL() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HashUserRealmProxy hr = new HashUserRealmProxy();
    hr.setFilename(PROPERTIES.getProperty(JETTY_USER_REALM));

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    securityConstraint.setPaths(Arrays.asList("/secure/path1|/secure/path2"));

    hr.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(hr);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer("/*");
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata("content.type", "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getStringPayload());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      Thread.currentThread().setName(threadName);
      PortManager.release(connection.getPort());
      assertEquals(0, AdapterResourceAuthenticator.getInstance().currentAuthenticators().size());
    }
  }

  public void testConsume_WithACL_IncorrectPassword() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HashUserRealmProxy hr = new HashUserRealmProxy();
    hr.setFilename(PROPERTIES.getProperty(JETTY_USER_REALM));

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    securityConstraint.setPaths(Arrays.asList("/"));

    hr.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(hr);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata("content.type", "text/xml");
      httpProducer.setUserName("user");
      httpProducer.setPassword("badbadpassword");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("0 message consumed", 0, mockProducer.getMessages().size());
      assertTrue("Reply Response Code present", reply.containsKey(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      int rc = Integer.valueOf(reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE)).intValue();
      assertTrue("Reply Response Value 401 || 403", rc == HttpURLConnection.HTTP_UNAUTHORIZED
          || rc == HttpURLConnection.HTTP_FORBIDDEN);
    }
    catch (Exception e) {
      // This is expected actually,.
      if (e.getCause() instanceof IOException) {
        String s = ((IOException) e.getCause()).getMessage();
        String httpString = "Server returned HTTP response code: ";
        if (s.startsWith(httpString)) {
          int rc = Integer.parseInt(s.substring(httpString.length(), httpString.length() + 3));
          assertTrue(rc == HttpURLConnection.HTTP_UNAUTHORIZED || rc == HttpURLConnection.HTTP_FORBIDDEN);
        }
        else {
          throw e;
        }
      }
      else {
        throw e;
      }
    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      Thread.currentThread().setName(threadName);
      PortManager.release(connection.getPort());
      assertEquals(0, AdapterResourceAuthenticator.getInstance().currentAuthenticators().size());
    }
  }

  public void testConsume_WithACL_WithNoRoles() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HashUserRealmProxy hr = new HashUserRealmProxy();
    hr.setFilename(PROPERTIES.getProperty(JETTY_USER_REALM));

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setPaths(Arrays.asList("/"));

    hr.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(hr);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata("content.type", "text/xml");
      httpProducer.setUserName("user");
      httpProducer.setPassword("password");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("0 message consumed", 0, mockProducer.getMessages().size());
      assertTrue("Reply Response Code present", reply.containsKey(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      int rc = Integer.valueOf(reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE)).intValue();
      assertTrue("Reply Response Value 401 || 403", rc == HttpURLConnection.HTTP_UNAUTHORIZED
          || rc == HttpURLConnection.HTTP_FORBIDDEN);
    }
    catch (Exception e) {
      e.printStackTrace();
      // This is expected actually,.
      if (e.getCause() instanceof IOException) {
        String s = ((IOException) e.getCause()).getMessage();
        String httpString = "Server returned HTTP response code: ";
        if (s.startsWith(httpString)) {
          int rc = Integer.parseInt(s.substring(httpString.length(), httpString.length() + 3));
          assertTrue(rc == HttpURLConnection.HTTP_UNAUTHORIZED || rc == HttpURLConnection.HTTP_FORBIDDEN);
        }
        else {
          throw e;
        }
      }
      else {
        throw e;
      }
    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      Thread.currentThread().setName(threadName);
      PortManager.release(connection.getPort());
      assertEquals(0, AdapterResourceAuthenticator.getInstance().currentAuthenticators().size());

    }
  }

  public void testLoopbackWithIncorrectRole() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HashUserRealmProxy hr = new HashUserRealmProxy();
    hr.setFilename(PROPERTIES.getProperty(JETTY_USER_REALM));

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("admin");
    securityConstraint.setPaths(Arrays.asList("/"));

    hr.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(hr);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata("content.type", "text/xml");
      httpProducer.setUserName("user");
      httpProducer.setPassword("password");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("0 message consumed", 0, mockProducer.getMessages().size());
      assertTrue("Reply Response Code present", reply.containsKey(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      int rc = Integer.valueOf(reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE)).intValue();
      assertTrue("Reply Response Value 401 || 403", rc == HttpURLConnection.HTTP_UNAUTHORIZED
          || rc == HttpURLConnection.HTTP_FORBIDDEN);
    }
    catch (ProduceException e) {
      // This is expected actually,.
      if (e.getCause() instanceof IOException) {
        String s = ((IOException) e.getCause()).getMessage();
        String httpString = "Server returned HTTP response code: ";
        if (s.startsWith(httpString)) {
          int rc = Integer.parseInt(s.substring(httpString.length(), httpString.length() + 3));
          assertTrue(rc == HttpURLConnection.HTTP_UNAUTHORIZED || rc == HttpURLConnection.HTTP_FORBIDDEN);
        }
        else {
          throw e;
        }
      }
      else {
        throw e;
      }
    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      PortManager.release(connection.getPort());
      Thread.currentThread().setName(threadName);
      assertEquals(0, AdapterResourceAuthenticator.getInstance().currentAuthenticators().size());
    }
  }

  protected HttpConnection createConnection(SecurityHandlerWrapper sh) {
    HttpConnection c = new HttpConnection();
    int port = PortManager.nextUnusedPort(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    c.setPort(port);
    if (sh != null) {
      c.setSecurityHandler(sh);
    }
    return c;
  }

  protected ConfiguredProduceDestination createProduceDestination(int port) {
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("http://localhost:" + port + URL_TO_POST_TO);
    return d;
  }

  protected HttpProducer createProducer() {
    JdkHttpProducer p = new JdkHttpProducer();
    p.setContentTypeKey("content.type");
    p.setIgnoreServerResponseCode(true);
    p.registerConnection(new NullConnection());
    return p;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    HttpConnection connection = createConnection(createSecurityHandlerExample());
    connection.getHttpProperties().add(new KeyValuePair(HttpProperty.SoLingerTime.name(), "-1"));
    connection.getHttpProperties().add(new KeyValuePair(HttpProperty.ReuseAaddress.name(), "true"));
    connection.getHttpProperties().add(new KeyValuePair(HttpProperty.ResolveNames.name(), "true"));
    MessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandaloneConsumer result = new StandaloneConsumer(connection, consumer);
    return result;
  }

  SecurityHandlerWrapper createSecurityHandlerExample() {
    HashUserRealmProxy hp = new HashUserRealmProxy();
    hp.setFilename("/path/to/realm.properties");

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(false);
    securityConstraint.setPaths(new ArrayList(Arrays.asList("/public")));

    SecurityConstraint securityConstraint2 = new SecurityConstraint();
    securityConstraint2.setMustAuthenticate(true);
    securityConstraint2.setRoles("adminUserRole");
    securityConstraint2.setPaths(new ArrayList(Arrays.asList("/private", "/admin")));

    SecurityConstraint securityConstraint3 = new SecurityConstraint();
    securityConstraint3.setMustAuthenticate(true);
    securityConstraint3.setRoles("serviceUserRole,optionsUserRole,anotherUserRole");
    securityConstraint3.setPaths(new ArrayList(Arrays.asList("/myServices", "/myOptions", "/myOtherURL")));

    hp.setSecurityConstraints(new ArrayList(Arrays.asList(securityConstraint, securityConstraint2, securityConstraint3)));
    return hp;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-HTTP";
  }
}
