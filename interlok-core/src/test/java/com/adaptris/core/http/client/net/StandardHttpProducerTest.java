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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoderImp;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.common.MetadataStreamOutputParameter;
import com.adaptris.core.common.PayloadStreamInputParameter;
import com.adaptris.core.http.HttpConstants;
import com.adaptris.core.http.HttpProducerExample;
import com.adaptris.core.http.MetadataContentTypeProvider;
import com.adaptris.core.http.auth.ConfiguredUsernamePassword;
import com.adaptris.core.http.auth.HttpAuthenticator;
import com.adaptris.core.http.auth.MetadataUsernamePassword;
import com.adaptris.core.http.client.ConfiguredRequestMethodProvider;
import com.adaptris.core.http.client.MetadataRequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider;
import com.adaptris.core.http.jetty.ConfigurableSecurityHandler;
import com.adaptris.core.http.jetty.HashLoginServiceFactory;
import com.adaptris.core.http.jetty.HttpConnection;
import com.adaptris.core.http.jetty.HttpConsumerTest;
import com.adaptris.core.http.jetty.JettyHelper;
import com.adaptris.core.http.jetty.JettyMessageConsumer;
import com.adaptris.core.http.jetty.SecurityConstraint;
import com.adaptris.core.http.jetty.StandardResponseProducer;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.services.metadata.PayloadFromTemplateService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.TimeInterval;

public class StandardHttpProducerTest extends HttpProducerExample {
  private static final String TEXT = "ABCDEFG";
  private static final String ALT_TEXT = "HIJKLMNOP";



  @Test
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

  @Test
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

  @Test
  public void testProduceWithContentTypeMetadata() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));
    stdHttp.setContentTypeProvider(new MetadataContentTypeProvider(HttpHelper.CONTENT_TYPE));
    StandaloneProducer producer = new StandaloneProducer(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(HttpHelper.CONTENT_TYPE, "text/complicated");
    try {
      c.requestStart();
      start(producer);
      producer.produce(msg);
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

  @Test
  public void testProduce_MetadataRequestHeaders() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp =
        new StandardHttpProducer().withURL(HttpHelper.createURL(c));
    stdHttp.setRequestHeaderProvider(new MetadataRequestHeaders(new RegexMetadataFilter()));
    StandaloneProducer producer = new StandaloneProducer(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(getName(), getName());
    try {
      c.requestStart();
      // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
      LifecycleHelper.prepare(producer);
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
    assertTrue(m2.headersContainsKey(getName()));
    assertEquals(getName(), m2.getMetadataValue(getName()));
  }

  @Test
  public void testProduce_WithMetadataMethod() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    ServiceList sl = new ServiceList();
    PayloadFromTemplateService pms = new PayloadFromTemplateService().withTemplate(TEXT);
    sl.add(pms);
    sl.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, sl));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));

    stdHttp.setMethodProvider(new MetadataRequestMethodProvider("httpMethod"));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata("httpMethod", "get");
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

  @Test
  public void testRequest_GetMethod_ZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    ServiceList sl = new ServiceList();
    PayloadFromTemplateService pms = new PayloadFromTemplateService().withTemplate(TEXT);
    sl.add(pms);
    sl.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, sl));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));

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
  
  @Test
  public void testRequest_ErrorCode_NullResponseBody() throws Exception {
    // INTERLOK-3527 - make sure we don't get an NPE.
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    ServiceList sl = new ServiceList();
    // To trigger the bug we need the getErrorStream() method to return null.  A response code > 400 will not.
    sl.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.SWITCH_PROTOCOL_101)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, sl));
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));

    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    stdHttp.setIgnoreServerResponseCode(true);
    // Metadata stream is the source of the bug, so lets use it here rather than the payload default.
    stdHttp.setResponseBody(new MetadataStreamOutputParameter());
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
  }

  @Test
  public void testRequest_PostMethod_ZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    PayloadFromTemplateService pms = new PayloadFromTemplateService().withTemplate(TEXT);
    workflow.getServiceCollection().add(pms);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));

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

  @Test
  public void testRequest_Post_ZeroBytes_ReplyToMetadata() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    PayloadFromTemplateService pms = new PayloadFromTemplateService().withTemplate(TEXT);
    workflow.getServiceCollection().add(pms);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));

    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));

    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.POST));
    stdHttp.setResponseBody(new MetadataStreamOutputParameter(getName()));

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
    assertNotSame(TEXT, msg.getContent());
    assertEquals(0, msg.getSize());

    assertTrue(msg.headersContainsKey(getName()));
    assertEquals(TEXT, msg.getMetadataValue(getName()));
  }

  @Test
  public void testRequest_EmptyReply() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setSendPayload(false);
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));

    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.POST));
    stdHttp.setConnectTimeout(new TimeInterval(60L, TimeUnit.SECONDS));
    stdHttp.setReadTimeout(new TimeInterval(60L, TimeUnit.SECONDS));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    producer.setReplyTimeout(new TimeInterval(60L, TimeUnit.SECONDS));
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
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
    assertEquals(0, msg.getSize());
  }

  @Test
  public void testRequest_MetadataResponseHeaders() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));

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
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
    AdaptrisMessage m2 = mock.getMessages().get(0);
    assertTrue(m2.headersContainsKey("Content-Type"));
    assertEquals("text/complicated", m2.getMetadataValue("Content-Type"));
    assertTrue(msg.headersContainsKey("Server"));
  }

  @Test
  public void testRequest_ObjectMetadataResponseHeaders() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));

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
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertFalse(msg.headersContainsKey("Server"));
    assertTrue(msg.getObjectHeaders().containsKey("Server"));
  }

  @Test
  public void testRequest_CompositeMetadataResponseHeaders() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;
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
    }
    finally {
      HttpHelper.stopChannelAndRelease(c);
      stop(producer);
    }
    assertTrue(msg.headersContainsKey("Server"));
    assertTrue(msg.getObjectHeaders().containsKey("Server"));
  }

  @Test
  public void testRequest_GetMethod_NonZeroBytes() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(ALT_TEXT);
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
    assertEquals(0, m2.getSize());
  }

  @Test
  public void testRequest_GetMethod_NonZeroBytes_WithErrorResponse() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);

    ServiceList services = new ServiceList();
    services.add(new PayloadFromTemplateService().withTemplate(TEXT));
    services.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.UNAUTHORIZED_401)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, services));
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    stdHttp.setIgnoreServerResponseCode(true);
    stdHttp.setResponseHeaderHandler(new ResponseHeadersAsMetadata("HTTP_"));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(ALT_TEXT);
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
    return new ConfiguredUsernamePassword(username, password);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testProduce_WithUsernamePassword() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());

    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl = new HashLoginServiceFactory("InterlokJetty",
        PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    csh.setLoginService(hsl);
    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection jc = HttpHelper.createConnection();
    jc.setSecurityHandler(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel channel = JettyHelper.createChannel(jc, consumer, mockProducer);

    HttpAuthenticator auth = getAuthenticator(getName(), getName());

    StandardHttpProducer stdHttp =
        new StandardHttpProducer().withURL(HttpHelper.createURL(channel));
    stdHttp.setIgnoreServerResponseCode(false);
    stdHttp.registerConnection(new NullConnection());
    stdHttp.setAuthenticator(auth);
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      start(stdHttp);
      AdaptrisMessage reply = stdHttp.request(msg);
      waitForMessages(mockProducer, 1);
      assertEquals(TEXT, mockProducer.getMessages().get(0).getContent());
    }
    finally {
      stop(stdHttp);
      HttpHelper.stopChannelAndRelease(channel);
      Thread.currentThread().setName(threadName);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testProduce_WithMetadataUsernamePassword() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl = new HashLoginServiceFactory("InterlokJetty",
        PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    csh.setLoginService(hsl);
    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection jc = HttpHelper.createConnection();
    jc.setSecurityHandler(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel channel = JettyHelper.createChannel(jc, consumer, mockProducer);

    MetadataUsernamePassword auth = new MetadataUsernamePassword();
    auth.setUsernameMetadataKey("user-key");
    auth.setPasswordMetadataKey("pass-key");

    StandardHttpProducer stdHttp =
        new StandardHttpProducer().withURL(HttpHelper.createURL(channel));
    stdHttp.setIgnoreServerResponseCode(false);
    stdHttp.registerConnection(new NullConnection());
    stdHttp.setAuthenticator(auth);
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      msg.addMetadata("user-key", getName());
      msg.addMetadata("pass-key", getName());
      start(stdHttp);
      AdaptrisMessage reply = stdHttp.request(msg);
      waitForMessages(mockProducer, 1);
      assertEquals(TEXT, mockProducer.getMessages().get(0).getContent());
    }
    finally {
      stop(stdHttp);
      HttpHelper.stopChannelAndRelease(channel);
      Thread.currentThread().setName(threadName);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testProduce_WithUsernamePassword_BadCredentials() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl = new HashLoginServiceFactory("InterlokJetty",
        PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    csh.setLoginService(hsl);
    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection jc = HttpHelper.createConnection();
    jc.setSecurityHandler(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel channel = JettyHelper.createChannel(jc, consumer, mockProducer);

    HttpAuthenticator auth = getAuthenticator(getName(), getName());

    StandardHttpProducer stdHttp =
        new StandardHttpProducer().withURL(HttpHelper.createURL(channel));
    stdHttp.setIgnoreServerResponseCode(false);
    stdHttp.registerConnection(new NullConnection());
    stdHttp.setAuthenticator(auth);
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      start(stdHttp);
      AdaptrisMessage reply = stdHttp.request(msg);
      fail();
    }
    catch (ProduceException expected) {

    }
    finally {
      stop(stdHttp);
      HttpHelper.stopChannelAndRelease(channel);
      Thread.currentThread().setName(threadName);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testProduce_WithDynamicUsernamePassword() throws Exception {
    String threadName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());

    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl =
        new HashLoginServiceFactory("InterlokJetty", PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    csh.setLoginService(hsl);
    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    HttpConnection jc = HttpHelper.createConnection();
    jc.setSecurityHandler(csh);
    MockMessageProducer mockProducer = new MockMessageProducer();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel channel = JettyHelper.createChannel(jc, consumer, mockProducer);

    String password = Password.encode(getName(), Password.PORTABLE_PASSWORD);
    HttpAuthenticator auth = new DynamicBasicAuthorizationHeader(getName(), password);

    StandardHttpProducer stdHttp =
        new StandardHttpProducer().withURL(HttpHelper.createURL(channel));
    stdHttp.setIgnoreServerResponseCode(false);
    stdHttp.registerConnection(new NullConnection());
    stdHttp.setAuthenticator(auth);
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEXT);
      start(stdHttp);
      AdaptrisMessage reply = stdHttp.request(msg);
      waitForMessages(mockProducer, 1);
      assertEquals(TEXT, mockProducer.getMessages().get(0).getContent());
    } finally {
      stop(stdHttp);
      HttpHelper.stopChannelAndRelease(channel);
      Thread.currentThread().setName(threadName);
    }
  }

  @Test
  public void testProduceWithAuthorizationHeader() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;

    ConfiguredAuthorizationHeader authenticator = new ConfiguredAuthorizationHeader();
    authenticator.setHeaderValue("some value");
    stdHttp.setAuthenticator(authenticator);

    StandaloneProducer producer = new StandaloneProducer(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);

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
    assertTrue(m2.headersContainsKey(HttpConstants.AUTHORIZATION));
    assertEquals("some value", m2.getMetadataValue(HttpConstants.AUTHORIZATION));
  }

  @Test
  public void testProduceWithMetadataAuthorizationHeader() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;

    MetadataAuthorizationHeader authenticator = new MetadataAuthorizationHeader();
    authenticator.setMetadataKey("ah-key");
    stdHttp.setAuthenticator(authenticator);

    StandaloneProducer producer = new StandaloneProducer(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata("ah-key", "some value");

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
    assertTrue(m2.headersContainsKey(HttpConstants.AUTHORIZATION));
    assertEquals("some value", m2.getMetadataValue(HttpConstants.AUTHORIZATION));
  }

  @Test
  public void testRequest_GetMethod_WithErrorResponse() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);

    ServiceList services = new ServiceList();
    services.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.UNAUTHORIZED_401)));
    Channel c = createChannel(jc, createWorkflow(mc, mock, services));
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(ALT_TEXT);
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      fail();
    }
    catch (ServiceException expected) {

    }
    finally {
      stop(c);
      stop(producer);
    }
    assertEquals(1, mock.messageCount());
  }

  @Test
  public void testRequest_DeleteMethod_AlwaysSendPayload() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.DELETE));
    stdHttp.setRequestBody(new PayloadStreamInputParameter());
    stdHttp.setAlwaysSendPayload(true);
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(ALT_TEXT);
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
    assertEquals("DELETE", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(ALT_TEXT, m2.getContent());
  }

  @Test
  public void testRequest_TraceMethod_AlwaysSendPayload() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    Channel c = createChannel(jc, createWorkflow(mc, mock, new ServiceList()));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.TRACE));
    stdHttp.setAlwaysSendPayload(true);
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(ALT_TEXT);
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
    assertEquals("TRACE", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertEquals(0, m2.getSize());
  }

  @Test
  public void testProduce_WithEncoder() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    Channel c = HttpHelper.createAndStartChannel(mock);
    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;
    stdHttp.setEncoder(new UrlConnectionEncoder());
    StandaloneProducer producer = new StandaloneProducer(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
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
    assertEquals(TEXT, m2.getContent());
  }

  // HttpURLConnection doesn't support the Expect: 102-Processing
  // So this should throw a Produce Exception
  @Test
  public void testRequest_Get_ExpectHeader() throws Exception {
    MockMessageProducer mock = new MockMessageProducer();
    HttpConnection jc = HttpHelper.createConnection();
    JettyMessageConsumer mc = createConsumer(HttpHelper.URL_TO_POST_TO);
    mc.setSendProcessingInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    ServiceList services = new ServiceList();
    services.add(new PayloadFromTemplateService().withTemplate(TEXT));
    services.add(new WaitService(new TimeInterval(2L, TimeUnit.SECONDS)));
    services.add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));

    Channel c = createChannel(jc, createWorkflow(mc, mock, services));
    StandardWorkflow workflow = (StandardWorkflow) c.getWorkflowList().get(0);
    workflow.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));

    StandardHttpProducer stdHttp = new StandardHttpProducer().withURL(HttpHelper.createURL(c));;
    stdHttp.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethodProvider.RequestMethod.GET));
    stdHttp.setRequestHeaderProvider(
        new ConfiguredRequestHeaders().withHeaders(new KeyValuePair(HttpConstants.EXPECT, "102-Processing")));
    StandaloneRequestor producer = new StandaloneRequestor(stdHttp);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(ALT_TEXT);
    try {
      start(c);
      start(producer);
      producer.doService(msg);
      fail();
    }
    catch (ProduceException | ServiceException e) {
      assertTrue(e.getMessage().contains("Failed to send payload, got 102"));
    }
    finally {
      stop(c);
      stop(producer);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    StandardHttpProducer producer =
        new StandardHttpProducer().withURL("http://myhost.com/url/to/post/to");
    CompositeRequestHeaders headers = new CompositeRequestHeaders(
        new MetadataRequestHeaders(new RegexMetadataFilter().withIncludePatterns("X-HTTP.*").withExcludePatterns("X-NotHttp.*")),
        new ConfiguredRequestHeaders().withHeaders(new KeyValuePair("SOAPAction", "urn:hello")));
    producer.setRequestHeaderProvider(headers);
    producer.setResponseHeaderHandler(
        new CompositeResponseHeaderHandler(new ResponseHeadersAsMetadata("resp_hdr_"),
            new ResponseHeadersAsObjectMetadata("resp_hdr_")));
    producer.setAuthenticator(getAuthenticator("username", "password"));

    StandaloneProducer result = new StandaloneProducer(producer);

    return result;
  }

  private class UrlConnectionEncoder extends AdaptrisMessageEncoderImp<HttpURLConnection, HttpURLConnection> {

    @Override
    public void writeMessage(AdaptrisMessage msg, HttpURLConnection target) throws CoreException {
      try {
        copyAndClose(msg.getInputStream(), target.getOutputStream());
      }
      catch (IOException e) {
        throw new CoreException(e);
      }
    }

    @Override
    public AdaptrisMessage readMessage(HttpURLConnection source) throws CoreException {
      AdaptrisMessage msg = currentMessageFactory().newMessage();
      try {
        copyAndClose(source.getInputStream(), msg.getOutputStream());
      }
      catch (IOException e) {
        throw new CoreException(e);
      }
      return msg;
    }

    private void copyAndClose(InputStream input, OutputStream out) throws IOException, CoreException {
      try (InputStream autoCloseIn = new BufferedInputStream(input); OutputStream autoCloseOut = new BufferedOutputStream(out)) {
        IOUtils.copy(autoCloseIn, autoCloseOut);
      }
    }

  }
}
