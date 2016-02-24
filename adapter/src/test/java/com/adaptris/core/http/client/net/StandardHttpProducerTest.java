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

package com.adaptris.core.http.client.net;

import static com.adaptris.core.http.jetty.JettyHelper.createChannel;
import static com.adaptris.core.http.jetty.JettyHelper.createConsumer;
import static com.adaptris.core.http.jetty.JettyHelper.createWorkflow;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.common.MetadataStreamOutputParameter;
import com.adaptris.core.http.HttpProducerExample;
import com.adaptris.core.http.MetadataContentTypeProvider;
import com.adaptris.core.http.auth.HttpAuthenticator;
import com.adaptris.core.http.auth.ConfiguredUsernamePassword;
import com.adaptris.core.http.client.ConfiguredRequestMethodProvider;
import com.adaptris.core.http.client.MetadataRequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider;
import com.adaptris.core.http.jetty.HashUserRealmProxy;
import com.adaptris.core.http.jetty.HttpConnection;
import com.adaptris.core.http.jetty.HttpConsumerTest;
import com.adaptris.core.http.jetty.JettyHelper;
import com.adaptris.core.http.jetty.MessageConsumer;
import com.adaptris.core.http.jetty.SecurityConstraint;
import com.adaptris.core.http.jetty.StandardResponseProducer;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.metadata.PayloadFromMetadataService;
import com.adaptris.core.stubs.MockMessageProducer;

public class StandardHttpProducerTest extends HttpProducerExample {
  private static final String TEXT = "ABCDEFG";

  public StandardHttpProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testSetHandleRedirection() throws Exception {
    StandardHttpProducer p = new StandardHttpProducer();
    assertTrue(p.handleRedirection());
    p.setAllowRedirect(true);
    assertNotNull(p.getAllowRedirect());
    assertEquals(Boolean.TRUE, p.getAllowRedirect());
    assertTrue(p.handleRedirection());
    p.setAllowRedirect(false);
    assertNotNull(p.getAllowRedirect());
    assertEquals(Boolean.FALSE, p.getAllowRedirect());
    assertFalse(p.handleRedirection());
  }

  public void testSetIgnoreServerResponse() throws Exception {
    StandardHttpProducer p = new StandardHttpProducer();
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


  public void testProduceWithContentTypeMetadata() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setContentTypeProvider(new MetadataContentTypeProvider(HttpHelper.CONTENT_TYPE));
    StandaloneProducer producer = new StandaloneProducer(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(HttpHelper.CONTENT_TYPE, "text/complicated");
    try {
      c.requestStart();
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertTrue(m2.headersContainsKey("Content-Type"));
    assertEquals("text/complicated", m2.getMetadataValue("Content-Type"));
  }


  public void testProduce_MetadataRequestHeaders() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setRequestHeaderProvider(new MetadataRequestHeaders(new RegexMetadataFilter()));
    StandaloneProducer producer = new StandaloneProducer(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(getName(), getName());
    try {
      c.requestStart();
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertTrue(m2.headersContainsKey(getName()));
    assertEquals(getName(), m2.getMetadataValue(getName()));
  }


  public void testProduce_WithMetadataMethod() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    MessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    ServiceList sl = new ServiceList();
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    sl.add(pms);
    sl.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, sl));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setMethodProvider(new MetadataRequestMethodProvider("httpMethod"));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata("httpMethod", "get");
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("GET", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(TEXT, msg.getContent());
  }


  public void testRequest_GetMethod_ZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    MessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    ServiceList sl = new ServiceList();
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    sl.add(pms);
    sl.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, sl));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("GET", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(TEXT, msg.getContent());
  }

  public void testRequest_PostMethod_ZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    MessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    workflow.getServiceCollection().add(pms);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.POST));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("POST", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(TEXT, msg.getContent());
  }

  public void testRequest_Post_ZeroBytes_ReplyToMetadata() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    MessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    workflow.getServiceCollection().add(pms);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));


    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.POST));
    stdHttp.setResponseBody(new MetadataStreamOutputParameter(getName()));

    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertNotSame(TEXT, msg.getContent());
    assertEquals(0, msg.getSize());

    assertTrue(msg.headersContainsKey(getName()));
    assertEquals(TEXT, msg.getMetadataValue(getName()));
  }


  public void testRequest_EmptyReply() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    MessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setSendPayload(false);
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.POST));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertEquals("POST", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(0, msg.getSize());
  }



  public void testRequest_MetadataResponseHeaders() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setContentTypeProvider(new MetadataContentTypeProvider(HttpHelper.CONTENT_TYPE));
    stdHttp.setResponseHeaderHandler(new ResponseHeadersAsMetadata("", "|"));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(HttpHelper.CONTENT_TYPE, "text/complicated");
    assertFalse(msg.headersContainsKey("Server"));
    try {
      c.requestStart();
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertTrue(m2.headersContainsKey("Content-Type"));
    assertEquals("text/complicated", m2.getMetadataValue("Content-Type"));
    assertTrue(msg.headersContainsKey("Server"));
  }


  public void testRequest_ObjectMetadataResponseHeaders() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setContentTypeProvider(new MetadataContentTypeProvider(HttpHelper.CONTENT_TYPE));
    stdHttp.setResponseHeaderHandler(new ResponseHeadersAsObjectMetadata());
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(HttpHelper.CONTENT_TYPE, "text/complicated");
    assertFalse(msg.headersContainsKey("Server"));
    try {
      c.requestStart();
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertFalse(msg.headersContainsKey("Server"));
    assertTrue(msg.getObjectHeaders().containsKey("Server"));
  }

  public void testRequest_CompositeMetadataResponseHeaders() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setContentTypeProvider(new MetadataContentTypeProvider(HttpHelper.CONTENT_TYPE));
    stdHttp.setResponseHeaderHandler(
        new CompositeResponseHeaderHandler(new ResponseHeadersAsMetadata(), new ResponseHeadersAsObjectMetadata()));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(HttpHelper.CONTENT_TYPE, "text/complicated");
    assertFalse(msg.headersContainsKey("Server"));
    try {
      c.requestStart();
      start(producer);
      producer.doService(msg);
      waitForMessages(mock, 1);
    } finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertTrue(msg.headersContainsKey("Server"));
    assertTrue(msg.getObjectHeaders().containsKey("Server"));
  }


  public void testRequest_GetMethod_NonZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    MessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate(TEXT);
    workflow.getServiceCollection().add(pms);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
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
    assertEquals(TEXT, msg.getContent());
  }
  
  public void testRequest_GetMethod_NonZeroBytes_WithErrorResponse() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    jc.setSendServerVersion(true);
    MessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);

    ServiceList services = new ServiceList();
    services.add(new PayloadFromMetadataService(TEXT));
    services.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.UNAUTHORIZED_401)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, services));
    StandardHttpProducer stdHttp = new StandardHttpProducer(HttpHelper.createProduceDestination(c));
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    stdHttp.setIgnoreServerResponseCode(true);
    stdHttp.setResponseHeaderHandler(new ResponseHeadersAsMetadata("HTTP_"));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
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
    assertEquals(TEXT, msg.getContent());
    assertEquals("401", msg.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
    assertNotNull(msg.getMetadata("HTTP_Server"));
  }

  private HttpAuthenticator getAuthenticator(String username, String password) {
    ConfiguredUsernamePassword auth = new ConfiguredUsernamePassword();
    auth.setUsername(username);
    auth.setPassword(password);
    return auth;
  }

  public void testProduce_WithUsernamePassword() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HashUserRealmProxy hr = new HashUserRealmProxy();
    hr.setFilename(PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM));

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");

    hr.setSecurityConstraints(Arrays.asList(securityConstraint));
    HttpConnection jc = HttpHelper.createConnection();
    jc.setSecurityHandler(hr);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel channel = JettyHelper.createChannel(jc, consumer, mockProducer);

    HttpAuthenticator auth = getAuthenticator(getName(), getName());
    
    StandardHttpProducer stdHttp = new StandardHttpProducer();
    stdHttp.setIgnoreServerResponseCode(true);
    stdHttp.registerConnection(new NullConnection());
    stdHttp.setAuthenticator(auth);
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      start(stdHttp);
      AdaptrisMessage reply = stdHttp.request(msg, HttpHelper.createProduceDestination(channel));
      waitForMessages(mockProducer, 1);
      assertEquals(TEXT, mockProducer.getMessages().get(0).getContent());
    } finally {
      stop(stdHttp);
      HttpHelper.stopChannelAndRelease(channel);
      Thread.currentThread().setName(threadName);
    }
  }



  public void testProduce_WithUsernamePassword_BadCredentials() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HashUserRealmProxy hr = new HashUserRealmProxy();
    hr.setFilename(PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM));

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");

    hr.setSecurityConstraints(Arrays.asList(securityConstraint));
    HttpConnection jc = HttpHelper.createConnection();
    jc.setSecurityHandler(hr);
    MockMessageProducer mockProducer = new MockMessageProducer();
    MessageConsumer consumer = JettyHelper.createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel channel = JettyHelper.createChannel(jc, consumer, mockProducer);

    HttpAuthenticator auth = getAuthenticator(getName(), getName());
    
    StandardHttpProducer stdHttp = new StandardHttpProducer();
    stdHttp.setIgnoreServerResponseCode(false);
    stdHttp.registerConnection(new NullConnection());
    stdHttp.setAuthenticator(auth);
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      start(stdHttp);
      AdaptrisMessage reply = stdHttp.request(msg, HttpHelper.createProduceDestination(channel));
      fail();
    } catch (ProduceException expected) {

    } finally {
      stop(stdHttp);
      HttpHelper.stopChannelAndRelease(channel);
      Thread.currentThread().setName(threadName);
    }
  }



  @Override
  protected Object retrieveObjectForSampleConfig() {
    StandardHttpProducer producer = new StandardHttpProducer(new ConfiguredProduceDestination("http://myhost.com/url/to/post/to"));

    producer.setAuthenticator(getAuthenticator("username", "password"));

    StandaloneProducer result = new StandaloneProducer(producer);

    return result;
  }

}
