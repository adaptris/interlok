/*
 * Copyright Adaptris Ltd.
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

import static com.adaptris.core.http.jetty.HttpConsumerTest.JETTY_HTTP_PORT;
import static com.adaptris.core.http.jetty.HttpConsumerTest.URL_TO_POST_TO;
import static com.adaptris.core.http.jetty.HttpConsumerTest.XML_PAYLOAD;
import static com.adaptris.core.http.jetty.JettyHelper.createConnection;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.ExampleWorkflowCase;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.WorkflowImp;
import com.adaptris.core.http.client.net.HttpRequestService;
import com.adaptris.core.services.metadata.PayloadFromMetadataService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class JettyAsyncWorkflowInterceptorTest extends ExampleWorkflowCase {

  static final String DEFAULT_XML_COMMENT = "<!-- This interceptor allows you to handle a single HTTP response\n"
      + "across 2 workflows.\n"
      + "Configure a jetty-async-workflow-interceptor in each workflow; one with mode=REQUEST\n"
      + "one with mode=RESPONSE. When the response workflow finishes, the HTTP response will\n"
      + "be committed.\n"
      + "The request workflow could back off to JMS (and subsequently off to other instances);\n"
      + "the response workflow would listen on a given JMS queue and handle that response when\n"
      + "it arrives."
      + "\n-->\n";

  public JettyAsyncWorkflowInterceptorTest(String testName) {
    super(testName);
  }

  @Override
  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testLifecycle() throws Exception {
    JettyAsyncWorkflowInterceptor interceptor = new JettyAsyncWorkflowInterceptor();
    try {
      LifecycleHelper.initAndStart(interceptor);
      fail();
    } catch (CoreException expected) {

    }
    interceptor.setMode(JettyAsyncWorkflowInterceptor.Mode.REQUEST);
    try {
      LifecycleHelper.initAndStart(interceptor);
    } finally {
      LifecycleHelper.stopAndClose(interceptor, false);
    }
  }

  public void testInterceptor_Cache() throws Exception {
    JettyAsyncWorkflowInterceptor requestor = new JettyAsyncWorkflowInterceptor()
        .withMode(JettyAsyncWorkflowInterceptor.Mode.REQUEST);
    JettyAsyncWorkflowInterceptor responder = new JettyAsyncWorkflowInterceptor()
        .withMode(JettyAsyncWorkflowInterceptor.Mode.RESPONSE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
    try {
      assertFalse(JettyAsyncWorkflowInterceptor.cacheContains("hello"));
      assertFalse(JettyAsyncWorkflowInterceptor.removeEntry("hello"));
      start(responder, requestor);
      requestor.workflowStart(msg);
      assertTrue(JettyAsyncWorkflowInterceptor.cacheContains(msg.getUniqueId()));
      responder.workflowStart(msg);
      responder.workflowStart(AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD));
      responder.workflowEnd(AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD),
          AdaptrisMessageFactory.getDefaultInstance().newMessage());
      assertFalse(JettyAsyncWorkflowInterceptor.cacheContains(msg.getUniqueId()));
    } finally {
      stop(responder, requestor);
    }
  }

  public void testInterceptor_WithShortcut() throws Exception {
    HttpConnection connection = createConnection(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO, getName());
    StandardWorkflow receivingWF = new StandardWorkflow();
    MockMessageProducer producer = new MockMessageProducer();
    receivingWF.addInterceptor(new JettyAsyncWorkflowInterceptor().withMode(JettyAsyncWorkflowInterceptor.Mode.REQUEST));
    receivingWF.getServiceCollection().add(new PayloadFromMetadataService("hello world"));
    receivingWF.getServiceCollection().add(new StandaloneProducer(producer));
    receivingWF.getServiceCollection().add(new JettyResponseService(200, "text/plain"));
    receivingWF.getServiceCollection().add(new ShortCutJettyResponse());
    receivingWF.setConsumer(consumer);
    Channel channel = JettyHelper.createChannel(connection, receivingWF);
    HttpRequestService httpService = createRequestor(connection.getPort());
    try {
      start(channel);
      LifecycleHelper.initAndStart(httpService);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      ServiceCase.execute(httpService, msg);
      assertEquals("hello world", msg.getContent());
      // Should be removed from the static cache.
      waitForMessages(producer, 1);
      // Grab the message that the standardWorkflow handled; and check the msgId.
      // Should be removed from the static cache.
      assertFalse(JettyAsyncWorkflowInterceptor.cacheContains(producer.getMessages().get(0).getUniqueId()));
    } finally {
      stop(channel);
    }
  }

  public void testAcrossMultipleWorkflows() throws Exception {
    HttpConnection connection = createConnection(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO, getName());
    PoolingWorkflow receivingWF = new PoolingWorkflow();
    receivingWF.addInterceptor(new JettyAsyncWorkflowInterceptor().withMode(JettyAsyncWorkflowInterceptor.Mode.REQUEST));
    receivingWF.setShutdownWaitTime(new TimeInterval(1L, TimeUnit.SECONDS));
    receivingWF.setConsumer(consumer);

    StandardWorkflow respondingWF = new StandardWorkflow();
    // Mainly to keep track of the msgID. we use a standard workflow so new objects aren't created.
    MockMessageProducer producer = new MockMessageProducer();
    respondingWF.addInterceptor(new JettyAsyncWorkflowInterceptor().withMode(JettyAsyncWorkflowInterceptor.Mode.RESPONSE));
    respondingWF.getServiceCollection().add(new PayloadFromMetadataService("hello world"));
    respondingWF.getServiceCollection().add(new JettyResponseService(200, "text/plain"));
    respondingWF.getServiceCollection().add(new StandaloneProducer(producer));

    receivingWF.setProducer(new WorkflowProducer(respondingWF));

    Channel channel = JettyHelper.createChannel(connection, receivingWF, respondingWF);
    HttpRequestService httpService = createRequestor(connection.getPort());
    try {
      start(channel);
      LifecycleHelper.initAndStart(httpService);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      ServiceCase.execute(httpService, msg);
      assertEquals("hello world", msg.getContent());      
      waitForMessages(producer, 1);
      // Grab the message that the standardWorkflow handled; and check the msgId.
      // Should be removed from the static cache.
      assertFalse(JettyAsyncWorkflowInterceptor.cacheContains(producer.getMessages().get(0).getUniqueId()));
    }
    finally {
      stop(channel);
    }
  }

  public void testAcrossMultipleWorkflows_WithCacheKey() throws Exception {
    HttpConnection connection = createConnection(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT)));
    JettyMessageConsumer consumer = JettyHelper.createConsumer(URL_TO_POST_TO, getName());
    PoolingWorkflow receivingWF = new PoolingWorkflow();
    // It's a bit lame, but we have to use something that is populated *before entry into the workflow*
    String cacheKey = "%message{" + JettyConstants.JETTY_URI + "}";
    receivingWF.addInterceptor(
        new JettyAsyncWorkflowInterceptor().withMode(JettyAsyncWorkflowInterceptor.Mode.REQUEST).withCacheKey(cacheKey));
    receivingWF.setShutdownWaitTime(new TimeInterval(1L, TimeUnit.SECONDS));
    receivingWF.setConsumer(consumer);

    StandardWorkflow respondingWF = new StandardWorkflow();
    // Mainly to keep track of the msgID. we use a standard workflow so new objects aren't created.
    MockMessageProducer producer = new MockMessageProducer();
    respondingWF.addInterceptor(
        new JettyAsyncWorkflowInterceptor().withMode(JettyAsyncWorkflowInterceptor.Mode.RESPONSE).withCacheKey(cacheKey));
    respondingWF.getServiceCollection().add(new PayloadFromMetadataService("hello world"));
    respondingWF.getServiceCollection().add(new JettyResponseService(200, "text/plain"));
    respondingWF.getServiceCollection().add(new StandaloneProducer(producer));

    receivingWF.setProducer(new WorkflowProducer(respondingWF));

    Channel channel = JettyHelper.createChannel(connection, receivingWF, respondingWF);
    HttpRequestService httpService = createRequestor(connection.getPort());
    try {
      start(channel);
      LifecycleHelper.initAndStart(httpService);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      ServiceCase.execute(httpService, msg);
      assertEquals("hello world", msg.getContent());
      waitForMessages(producer, 1);
      // Grab the message that the standardWorkflow handled; and check the msgId.
      // Should be removed from the static cache.
      assertFalse(JettyAsyncWorkflowInterceptor.cacheContains(producer.getMessages().get(0).getUniqueId()));
    } finally {
      stop(channel);
    }
  }

  @Override
  protected Channel retrieveObjectForSampleConfig() {
    try {
      Channel c = new Channel("JettyHandler");
      PoolingWorkflow wf1 = new PoolingWorkflow("receiveJettyRequest");
      wf1.addInterceptor(new JettyAsyncWorkflowInterceptor().withMode(JettyAsyncWorkflowInterceptor.Mode.REQUEST));
      PoolingWorkflow wf2 = new PoolingWorkflow("respondToJetty");
      wf2.addInterceptor(new JettyAsyncWorkflowInterceptor().withMode(JettyAsyncWorkflowInterceptor.Mode.RESPONSE));
      wf2.getServiceCollection().add(new JettyResponseService(200, "text/plain"));
      c.getWorkflowList().add(wf1);
      c.getWorkflowList().add(wf2);
      return c;
    }
    catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String createBaseFileName(Object object) {
    return "PoolingWorkflow-with-" + JettyAsyncWorkflowInterceptor.class.getSimpleName();
  }

  @Override
  protected PoolingWorkflow createWorkflowForGenericTests() throws CoreException {
    return new PoolingWorkflow();
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }

  private HttpRequestService createRequestor(int port) {
    final HttpRequestService httpService = new HttpRequestService();
    httpService.setUrl("http://localhost:" + port + URL_TO_POST_TO);
    httpService.setContentType("text/xml");
    return httpService;
  }

  private class WorkflowProducer extends MockMessageProducer {
    private WorkflowImp workflow;

    public WorkflowProducer(WorkflowImp p) {
      workflow = p;
    }

    @Override
    public void produce(AdaptrisMessage msg) throws ProduceException {
      super.produce(msg);
      workflow.onAdaptrisMessage(msg);
    }
  }
}
