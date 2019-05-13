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

import static com.adaptris.core.http.jetty.EmbeddedJettyHelper.URL_TO_POST_TO;
import static com.adaptris.core.http.jetty.EmbeddedJettyHelper.XML_PAYLOAD;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.NullConnection;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.http.HttpConsumerExample;
import com.adaptris.core.http.MetadataContentTypeProvider;
import com.adaptris.core.http.auth.ConfiguredUsernamePassword;
import com.adaptris.core.http.client.net.StandardHttpProducer;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StaticMockMessageProducer;
import com.adaptris.util.TimeInterval;

public class EmbeddedHttpConsumerTest extends HttpConsumerExample {

  static final String METADATA_VALUE2 = "value2";
  static final String METADATA_VALUE1 = "value1";
  static final String IGNORED_METADATA = "WillNotBeSent";
  static final String X_HTTP_KEY2 = "X-HTTP-Key2";
  static final String X_HTTP_KEY1 = "X-HTTP-Key1";
  static final String CONTENT_TYPE_METADATA_KEY = "content.type";

  protected StandardHttpProducer httpProducer;

  public EmbeddedHttpConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    httpProducer = createProducer();
  }

  @Override
  protected void tearDown() throws Exception {

  }

  public void testMaxStartupWaitTime() throws Exception {
    EmbeddedConnection c = new EmbeddedConnection();
    TimeInterval newInterval = new TimeInterval(10L, TimeUnit.SECONDS);
    TimeInterval defaultInterval = new TimeInterval(600L, TimeUnit.SECONDS);
    assertNull(c.getMaxStartupWait());
    assertEquals(defaultInterval.toMilliseconds(), c.maxStartupWaitTimeMs());
    c.setMaxStartupWait(newInterval);
    assertEquals(newInterval, c.getMaxStartupWait());
    assertEquals(newInterval.toMilliseconds(), c.maxStartupWaitTimeMs());
    c.setMaxStartupWait(null);
    assertNull(c.getMaxStartupWait());
    assertEquals(defaultInterval.toMilliseconds(), c.maxStartupWaitTimeMs());
  }


  public void testBasicConsumeWorkflow_ConsumeDestinationContainsURL() throws Exception {
    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(),
        JettyHelper.createConsumer("http://localhost:8080" + URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, helper.createProduceDestination());
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      helper.stopServer();
    }
  }

  public void testBasicConsumeWorkflow() throws Exception {
    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(), JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, helper.createProduceDestination());
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      helper.stopServer();
    }
  }


  public void testBasicConsumeWorkflow_WithACL() throws Exception {

    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    MockMessageProducer mockProducer = new MockMessageProducer();
    EmbeddedConnection embedded = new EmbeddedConnection();
    ConfigurableSecurityHandler csh = new ConfigurableSecurityHandler();
    HashLoginServiceFactory hsl =
        new HashLoginServiceFactory("InterlokJetty", PROPERTIES.getProperty(HttpConsumerTest.JETTY_USER_REALM));
    csh.setLoginService(hsl);

    SecurityConstraint securityConstraint = new SecurityConstraint();
    securityConstraint.setMustAuthenticate(true);
    securityConstraint.setRoles("user");
    securityConstraint.setPaths(Arrays.asList("/"));

    csh.setSecurityConstraints(Arrays.asList(securityConstraint));

    embedded.setSecurityHandler(csh);
    Channel channel = JettyHelper.createChannel(embedded, JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      httpProducer.setAuthenticator(new ConfiguredUsernamePassword("user", "password"));
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, helper.createProduceDestination());
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    } finally {
      stop(httpProducer);
      stop(channel);
      helper.stopServer();
    }
  }


  public void testBasicConsumeWorkflow_AcrossRestarts() throws Exception {
    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(), JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();
      channel.requestClose();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, helper.createProduceDestination());
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      helper.stopServer();
    }
  }

  // INTERLOK-201
  public void testBasicConsumeWorkflow_UpdatedConfig() throws Exception {
    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    MockMessageProducer mockProducer = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(), JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    Workflow workflow = JettyHelper.createWorkflow(JettyHelper.createConsumer(URL_TO_POST_TO), mock2);

    try {
      channel.requestStart();
      channel.requestClose();

      // Update the configuration.
      channel.getWorkflowList().clear();
      channel.getWorkflowList().add(workflow);
      // Now restart
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, helper.createProduceDestination());
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mock2);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      helper.stopServer();
    }
  }

  public void testChannelStarted_WorkflowStopped() throws Exception {
    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    JettyMessageConsumer consumer1 = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandardWorkflow workflow1 = new StandardWorkflow();
    workflow1.setConsumer(consumer1);
    workflow1.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(), workflow1);
    try {
      channel.requestStart();
      AdaptrisMessage msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg1.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg1, helper.createProduceDestination());
      assertEquals("200", reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      workflow1.requestClose();

      AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg1.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      AdaptrisMessage reply2 = httpProducer.request(msg2, helper.createProduceDestination());
      assertEquals("404", reply2.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));

    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      helper.stopServer();

    }
  }

  public void testChannelStarted_MultipleWorkflows_OneWorkflowStopped() throws Exception {
    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    JettyMessageConsumer consumer1 = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandardWorkflow workflow1 = new StandardWorkflow();
    workflow1.setConsumer(consumer1);
    workflow1.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(), workflow1);

    JettyMessageConsumer consumer2 = JettyHelper.createConsumer("/some/other/urlmapping/");
    StandardWorkflow workflow2 = new StandardWorkflow();
    workflow2.setConsumer(consumer2);
    workflow2.getServiceCollection().add(new StandaloneProducer(new StandardResponseProducer(HttpStatus.OK_200)));
    channel.getWorkflowList().add(workflow2);

    try {
      channel.requestStart();
      AdaptrisMessage msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg1.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg1, helper.createProduceDestination());
      assertEquals("200", reply.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));
      workflow2.requestClose();

      // Stopping Workflow 2 means nothing, workflow1 should still be working!
      AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg1.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      AdaptrisMessage reply2 = httpProducer.request(msg2, helper.createProduceDestination());
      assertEquals("200", reply2.getMetadataValue(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE));

    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      helper.stopServer();

    }
  }

  public void testPoolingWorkflow_WithInterceptor() throws Exception {

    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    PoolingWorkflow workflow = new PoolingWorkflow();
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    workflow.addInterceptor(new JettyPoolingWorkflowInterceptor());
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(), workflow);
    try {
      channel.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, helper.createProduceDestination());
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      helper.stopServer();

    }
  }

  public void testPoolingWorkflow_WithoutInterceptor() throws Exception {
    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    MockMessageProducer mockProducer = new StaticMockMessageProducer();
    mockProducer.getMessages().clear();
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setShutdownWaitTime(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
    workflow.getServiceCollection().add(new StandaloneProducer(mockProducer));
    workflow.getServiceCollection().add(new StandaloneProducer(responder));
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(), workflow);
    try {
      channel.requestStart();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(httpProducer);
      AdaptrisMessage reply = httpProducer.request(msg, helper.createProduceDestination());
      // Because of redmineID #4715 it should just "return immediatel" which flushes the stream so there's no content.
      assertEquals("Reply Payloads", "", reply.getContent());
    }
    finally {
      stop(httpProducer);
      channel.requestClose();
      helper.stopServer();
    }
  }

  protected void doAssertions(MockMessageProducer mockProducer) throws Exception {
    waitForMessages(mockProducer, 1);
    assertEquals("Only 1 message consumed", 1, mockProducer.getMessages().size());
    assertEquals("Consumed Payload", XML_PAYLOAD, mockProducer.getMessages().get(0).getContent());
    Map objMetadata = mockProducer.getMessages().get(0).getObjectHeaders();
    assertNotNull(objMetadata.get(JettyConstants.JETTY_WRAPPER));
  }

  protected StandardHttpProducer createProducer() {
    StandardHttpProducer p = new StandardHttpProducer();
    p.setContentTypeProvider(new MetadataContentTypeProvider("content.type"));
    p.setIgnoreServerResponseCode(true);
    p.registerConnection(new NullConnection());
    return p;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO);
    StandaloneConsumer result = new StandaloneConsumer(new EmbeddedConnection(), consumer);
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-EmbeddedConnection";
  }
}
