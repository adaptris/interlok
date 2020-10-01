/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.http.jetty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.PortManager;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.http.HttpConsumerExample;
import com.adaptris.core.http.HttpProducer;
import com.adaptris.core.http.auth.AdapterResourceAuthenticator;
import com.adaptris.core.http.jetty.HttpConnection.HttpConfigurationProperty;
import com.adaptris.core.http.jetty.HttpConnection.ServerConnectorProperty;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.management.webserver.SecurityHandlerWrapper;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.AdaptrisMessageStub;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.core.stubs.StaticMockMessageProducer;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.http.legacy.HttpProduceConnection;
import com.adaptris.http.legacy.SimpleHttpProducer;
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

  public static final String JETTY_HTTP_PORT = "jetty.http.port";
  public static final String JETTY_USER_REALM = "jetty.user.realm.properties";
  public static final String URL_TO_POST_TO = "/url/to/post/to";
  public static final String XML_PAYLOAD = "<root><document>value</document></root>";

  protected HttpProducer httpProducer;


  @Before
  public void setUp() throws Exception {
    httpProducer = createProducer();
  }

  @After
  public void tearDown() throws Exception {
    stop(httpProducer);
  }

  @Test
  public void testSetAdditionalDebug() throws Exception {
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    consumer.setAdditionalDebug(null);
    assertNull(consumer.getAdditionalDebug());
    assertEquals(false, consumer.additionalDebug());
    consumer.setAdditionalDebug(Boolean.TRUE);
    assertNotNull(consumer.getAdditionalDebug());
    assertEquals(true, consumer.additionalDebug());
  }

  @Test
  public void testSetSendProcessingInterval() throws Exception {
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    assertNull(consumer.getSendProcessingInterval());

    assertEquals(TimeUnit.SECONDS.toMillis(20L), consumer.sendProcessingInterval());

    TimeInterval t = new TimeInterval(10L, TimeUnit.MILLISECONDS);
    consumer.setSendProcessingInterval(t);
    assertEquals(t, consumer.getSendProcessingInterval());
    assertEquals(10, consumer.sendProcessingInterval());
  }

  @Test
  public void testSetTimeoutAction() throws Exception {
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    assertNull(consumer.getTimeoutAction());
    assertNotNull(consumer.timeoutAction());
    assertEquals(TimeUnit.MINUTES.toMillis(10L), consumer.timeoutAction().maxWaitTime());
    assertEquals(HttpStatus.ACCEPTED_202.getStatusCode(), consumer.timeoutAction().status());
    TimeoutAction t = new TimeoutAction(new TimeInterval(100L, TimeUnit.SECONDS), HttpStatus.OK_200);
    consumer.setTimeoutAction(t);
    assertEquals(t, consumer.getTimeoutAction());
    assertNotNull(consumer.timeoutAction());
    assertEquals(TimeUnit.SECONDS.toMillis(100L), consumer.timeoutAction().maxWaitTime());
    assertEquals(HttpStatus.OK_200.getStatusCode(), consumer.timeoutAction().status());
  }

  @Test
  public void testConnection_NonDefaults() throws Exception {
    HttpConnection connection = createConnection(null);
    connection.getServerConnectorProperties().addKeyValuePair(new KeyValuePair(ServerConnectorProperty.AcceptQueueSize.name(), "10"));
    connection.getServerConnectorProperties().addKeyValuePair(new KeyValuePair(ServerConnectorProperty.ReuseAaddress.name(), "true"));
    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), new MockMessageProducer());
    try {
      channel.requestStart();
    }
    finally {
      channel.requestClose();
    }

  }

  @Test
  public void testChannelStarted_WorkflowStopped() throws Exception {
    HttpConnection connection = createConnection(null);
    JettyMessageConsumer consumer1 = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandardWorkflow workflow1 = new StandardWorkflow();
    workflow1.setConsumer(consumer1);
    workflow1.getServiceCollection()
        .add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    Channel channel = JettyHelper.createChannel(connection, workflow1);
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

  @Test
  public void testChannelStarted_MultipleWorkflows_OneWorkflowStopped() throws Exception {
    HttpConnection connection = createConnection(null);

    JettyMessageConsumer consumer1 = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandardWorkflow workflow1 = new StandardWorkflow();
    workflow1.setConsumer(consumer1);
    workflow1.getServiceCollection()
        .add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    Channel channel = JettyHelper.createChannel(connection, workflow1);

    JettyMessageConsumer consumer2 = JettyHelper.createConsumer("/some/other/urlmapping/");
    StandardWorkflow workflow2 = new StandardWorkflow();
    workflow2.setConsumer(consumer2);
    workflow2.getServiceCollection()
        .add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    channel.getWorkflowList().add(workflow2);

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

  @Test
  public void testPoolingWorkflow_WithInterceptor() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    consumer.setWarnAfter(new TimeInterval(10L, TimeUnit.MILLISECONDS));
    PoolingWorkflow workflow = new PoolingWorkflow();
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    workflow.addInterceptor(new JettyPoolingWorkflowInterceptor());
    Channel channel = JettyHelper.createChannel(connection, workflow);
    try {
      channel.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
    }
  }

  @Test
  public void testPoolingWorkflow_WithoutInterceptor() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.addInterceptor(new MockWorkflowInterceptor());
    workflow.setShutdownWaitTime(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    Channel channel = JettyHelper.createChannel(connection, workflow);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      // Because of redmineID #4715 it should just "return immediatel" which flushes the stream so there's no content.
      assertEquals("Reply Payloads", "", reply.getContent());
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
    }
  }

  @Test
  public void testPoolingWorkflow_TimeoutAction_TimeoutExceeded() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    consumer.setAdditionalDebug(false);
    consumer.setTimeoutAction(new TimeoutAction(new TimeInterval(100L, TimeUnit.MILLISECONDS)));
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setShutdownWaitTime(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(5L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    workflow.addInterceptor(new JettyPoolingWorkflowInterceptor());
    Channel channel = JettyHelper.createChannel(connection, workflow);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals(Integer.valueOf(HttpStatus.ACCEPTED_202.getStatusCode()),
          Integer.valueOf(reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE)));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
    }
  }

  @Test
  public void testConsumeWorkflow_NoPreserveHeaders() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*");
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);
      assertFalse(receivedMsg.containsKey("Content-Type"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
  public void testConsumeWorkflow_IncorrectMethod() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.getDestination().setFilterExpression("GET,HEAD");
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      // we should default to post, so restricted to GET/HEAD/OPTIONS will cause a 405...
      httpProducer.setIgnoreReplyMetadata(false);
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("405", reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
  public void testConsumeWorkflow_Options_Automatic() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.getDestination().setFilterExpression("GET,HEAD");
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      httpProducer.setIgnoreReplyMetadata(false);
      if (httpProducer instanceof SimpleHttpProducer) {
        ((SimpleHttpProducer) httpProducer).setMethod("OPTIONS");
      }
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("200", reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
    } finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
  public void testConsumeWorkflow_PreserveHeaders() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.setHeaderHandler(new MetadataHeaderHandler("Http_Header_"));
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);
      assertEquals("text/xml", receivedMsg.getMetadataValue("Http_Header_Content-Type"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
  public void testConsumeWorkflow_PreserveParams_NoPrefix() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.setHeaderHandler(new MetadataHeaderHandler("Http_Header_"));
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
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);

      assertTrue(receivedMsg.containsKey(JettyConstants.JETTY_QUERY_STRING));
      assertEquals("queryParam1=1&queryParam2=2&queryParam3=3", receivedMsg.getMetadataValue(JettyConstants.JETTY_QUERY_STRING));
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

  @Test
  public void testConsumeWorkflow_PreserveObjectParams_NoPrefix() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*");
    consumer.setHeaderHandler(new ObjectMetadataHeaderHandler("Http_Header_"));
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
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);

      System.out.println("XXX - " + receivedMsg);

      assertTrue(receivedMsg.containsKey(JettyConstants.JETTY_QUERY_STRING));
      assertEquals("queryParam1=1&queryParam2=2&queryParam3=3", receivedMsg.getMetadataValue(JettyConstants.JETTY_QUERY_STRING));
      assertEquals("1", receivedMsg.getObjectHeaders().get("queryParam1"));
      assertEquals("2", receivedMsg.getObjectHeaders().get("queryParam2"));
      assertEquals("3", receivedMsg.getObjectHeaders().get("queryParam3"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
  public void testConsumeWorkflow_PreserveParams_WithPrefix() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*", getName());
    consumer.setHeaderHandler(new MetadataHeaderHandler("Http_Header_"));
    consumer.setParameterHandler(new MetadataParameterHandler("Http_Param_"));
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      ConfiguredProduceDestination dest = createProduceDestination(connection.getPort());
      dest.setDestination(dest.getDestination() + "?queryParam1=1&queryParam2=2&queryParam3=3");
      AdaptrisMessage reply = httpProducer.request(msg, dest);
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);
      assertTrue(receivedMsg.containsKey(JettyConstants.JETTY_QUERY_STRING));
      assertEquals("queryParam1=1&queryParam2=2&queryParam3=3", receivedMsg.getMetadataValue(JettyConstants.JETTY_QUERY_STRING));
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

  @Test
  public void testConsumeWorkflow_PreserveObjectParams_WithPrefix() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*", getName());
    consumer.setHeaderHandler(new ObjectMetadataHeaderHandler("Http_Header_"));
    consumer.setParameterHandler(new ObjectMetadataParameterHandler("Http_Param_"));
    Channel channel = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      ConfiguredProduceDestination dest = createProduceDestination(connection.getPort());
      dest.setDestination(dest.getDestination() + "?queryParam1=1&queryParam2=2&queryParam3=3");
      AdaptrisMessage reply = httpProducer.request(msg, dest);
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      AdaptrisMessage receivedMsg = doAssertions(mockProducer);
      assertTrue(receivedMsg.containsKey(JettyConstants.JETTY_QUERY_STRING));
      assertEquals("queryParam1=1&queryParam2=2&queryParam3=3", receivedMsg.getMetadataValue(JettyConstants.JETTY_QUERY_STRING));
      assertFalse(receivedMsg.getObjectHeaders().containsKey("Http_Header_queryParam1"));
      assertFalse(receivedMsg.getObjectHeaders().containsKey("Http_Header_queryParam2"));
      assertFalse(receivedMsg.getObjectHeaders().containsKey("Http_Header_queryParam3"));
      assertEquals("1", receivedMsg.getObjectHeaders().get("Http_Param_queryParam1"));
      assertEquals("2", receivedMsg.getObjectHeaders().get("Http_Param_queryParam2"));
      assertEquals("3", receivedMsg.getObjectHeaders().get("Http_Param_queryParam3"));
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
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

  @Test
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
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
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
      AdaptrisMessage consumedMessage = doAssertions(mockProducer);
      assertFalse(consumedMessage.headersContainsKey(JettyConstants.JETTY_USER_ROLES));
      assertFalse(consumedMessage.headersContainsKey(JettyConstants.JETTY_QUERY_STRING));

    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
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
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
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
      channel.getWorkflowList().add(workflow);
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mock2);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  protected AdaptrisMessage doAssertions(MockMessageProducer mockProducer) throws Exception {
    waitForMessages(mockProducer, 1);
    assertEquals("Only 1 message consumed", 1, mockProducer.getMessages().size());
    AdaptrisMessage msg = mockProducer.getMessages().get(0);
    assertEquals("Consumed Payload", XML_PAYLOAD, msg.getContent());
    assertTrue(msg.containsKey(JettyConstants.JETTY_URI));
    assertEquals(URL_TO_POST_TO, msg.getMetadataValue(JettyConstants.JETTY_URI));
    assertTrue(msg.containsKey(JettyConstants.JETTY_URL));
    Map objMetadata = msg.getObjectHeaders();
    assertNotNull(objMetadata.get(JettyConstants.JETTY_WRAPPER));
    return msg;
  }

  @Test
  public void testBug1549() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();

    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    consumer.setHeaderHandler(new MetadataHeaderHandler());

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
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);

      AdaptrisMessage consumedMsg = mockProducer.getMessages().get(0);
      assertEquals("Consumed Payload", XML_PAYLOAD, consumedMsg.getContent());
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

  @Test
  public void testBasicConsumeWithCustomMessageImpl() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      consumer.setMessageFactory(new StubMessageFactory());
      adapter.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
      assertEquals("Consumed Message Java Class", AdaptrisMessageStub.class, mockProducer.getMessages().get(0).getClass());
    }
    finally {
      stop(httpProducer);
      adapter.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  @Test
  public void testDestinationNotFound() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
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

  @Test
  public void testConsume_WithACL() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl = new HashLoginServiceFactory("InterlokJetty", PROPERTIES.getProperty(JETTY_USER_REALM));
    csh.setLoginService(hsl);
    csh.setAuthenticator(new BasicAuthenticatorFactory());
    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    securityConstraint.setConstraintName(Constraint.__BASIC_AUTH);
    csh.setSecurityConstraints(Arrays.asList(securityConstraint));
    HttpConnection connection = createConnection(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);

    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata("content.type", "text/xml");
      httpProducer.setUserName("user");
      httpProducer.setPassword("password");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      AdaptrisMessage consumedMessage = doAssertions(mockProducer);
      assertTrue(consumedMessage.headersContainsKey(JettyConstants.JETTY_USER_ROLES));
      List<String> roles = Arrays.asList(consumedMessage.getMetadataValue(JettyConstants.JETTY_USER_ROLES).split(","));
      assertTrue(roles.contains("user"));
      assertTrue(roles.contains("anotherRole"));
    } finally {
      stop(httpProducer);
      adapter.requestClose();
      Thread.currentThread().setName(threadName);
      PortManager.release(connection.getPort());
      assertEquals(0, AdapterResourceAuthenticator.getInstance().currentAuthenticators().size());
    }
  }

  @Test
  public void testConsume_WithACL_UnprotectedURL() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl = new HashLoginServiceFactory("InterlokJetty", PROPERTIES.getProperty(JETTY_USER_REALM));
    csh.setLoginService(hsl);

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    securityConstraint.setPaths(Arrays.asList("/secure/path1|/secure/path2"));

    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer("/*");
    Channel adapter = JettyHelper.createChannel(connection, consumer, mockProducer);
    try {
      adapter.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata("content.type", "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
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

  @Test
  public void testConsume_WithACL_IncorrectPassword() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl = new HashLoginServiceFactory("InterlokJetty", PROPERTIES.getProperty(JETTY_USER_REALM));
    csh.setLoginService(hsl);

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    securityConstraint.setPaths(Arrays.asList("/"));

    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
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

  @Test
  public void testConsume_WithACL_WithNoRoles() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl = new HashLoginServiceFactory("InterlokJetty", PROPERTIES.getProperty(JETTY_USER_REALM));
    csh.setLoginService(hsl);

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setPaths(Arrays.asList("/"));

    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
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

  @Test
  public void testLoopbackWithIncorrectRole() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl = new HashLoginServiceFactory("InterlokJetty", PROPERTIES.getProperty(JETTY_USER_REALM));
    csh.setLoginService(hsl);

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("admin");
    securityConstraint.setPaths(Arrays.asList("/"));

    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection connection = createConnection(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
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

  @Test
  public void testBasicConsumeWorkflow_LongLived_Expect() throws Exception {
    HttpConnection connection = createConnection(null);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO, getName());
    consumer.setSendProcessingInterval(new TimeInterval(1L, TimeUnit.SECONDS));
    StandardWorkflow wf = (StandardWorkflow) JettyHelper.createWorkflow(consumer, mockProducer);
    wf.getServiceCollection().add(new WaitService(new TimeInterval(3L, TimeUnit.SECONDS)));
    Channel channel = JettyHelper.createChannel(connection, wf);
    try {
      channel.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      httpProducer.getAdditionalHeaders().add(new KeyValuePair("Expect", "102-Processing"));
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
    }
  }

  protected HttpConnection createConnection(SecurityHandlerWrapper sh) {
    HttpConnection http = new HttpConnection();
    int port = PortManager.nextUnusedPort(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    http.setPort(port);
    if (sh != null) {
      http.setSecurityHandler(sh);
    }

    http.getServerConnectorProperties().clear();
    http.getServerConnectorProperties().add(new KeyValuePair(ServerConnectorProperty.ReuseAaddress.name(), "true"));
    http.getServerConnectorProperties().add(new KeyValuePair("WillNotMatch", "true"));

    http.getHttpConfiguration().clear();
    http.getHttpConfiguration().add(new KeyValuePair(HttpConfigurationProperty.OutputBufferSize.name(), "8192"));
    http.getHttpConfiguration().add(new KeyValuePair(HttpConfigurationProperty.SendServerVersion.name(), "false"));
    http.getHttpConfiguration().add(new KeyValuePair(HttpConfigurationProperty.SendDateHeader.name(), "false"));
    http.getHttpConfiguration().add(new KeyValuePair("WillNotMatch", "false"));

    return http;
  }

  protected ConfiguredProduceDestination createProduceDestination(int port) {
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("http://localhost:" + port + URL_TO_POST_TO);
    return d;
  }

  protected HttpProducer createProducer() {
    SimpleHttpProducer p = new SimpleHttpProducer();
    p.registerConnection(new HttpProduceConnection());
    p.setContentTypeKey("content.type");
    p.setIgnoreServerResponseCode(true);
    return p;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    HttpConnection connection = createConnection(createSecurityHandlerExample());
    connection.getServerConnectorProperties().add(new KeyValuePair(ServerConnectorProperty.ReuseAaddress.name(), "true"));
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandaloneConsumer result = new StandaloneConsumer(connection, consumer);
    return result;
  }

  SecurityHandlerWrapper createSecurityHandlerExample() {
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl =
        new HashLoginServiceFactory().withUserRealm("InterlokJetty").withFilename("/path/to/realm.properties");
    csh.setLoginService(hsl);

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

    csh.setSecurityConstraints(new ArrayList(Arrays.asList(securityConstraint, securityConstraint2, securityConstraint3)));
    return csh;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-HTTP";
  }
}
