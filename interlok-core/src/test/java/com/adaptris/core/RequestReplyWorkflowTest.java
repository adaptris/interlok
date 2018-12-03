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

package com.adaptris.core;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsReplyToDestination;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.services.Base64DecodeService;
import com.adaptris.core.services.Base64EncodeService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.MockRequestReplyProducer;
import com.adaptris.core.stubs.MockSkipProducerService;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.util.TimeInterval;

public class RequestReplyWorkflowTest extends ExampleWorkflowCase {

  public static final String REQUEST_METADATA_VALUE = "RequestMetadataValue";
  public static final String REQUEST_OBJ_METADATA_KEY = "RequestObjectMetadataKey";
  public static final String REQUEST_METADATA_KEY = "RequestMetadataKey";
  // private RequestReplyWorkflow workFlow;
  // private MockMessageConsumer consumer;
  // private MockRequestReplyProducer producer;
  // private MockMessageProducer replyProducer;

  public RequestReplyWorkflowTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  private Channel createChannel() throws Exception {
    Channel channel = new MockChannel();
    MockMessageConsumer consumer = new MockMessageConsumer();
    MockRequestReplyProducer producer = new MockRequestReplyProducer();
    MockMessageProducer replyProducer = new MockMessageProducer();
    RequestReplyWorkflow workFlow = new RequestReplyWorkflow();
    workFlow.setConsumer(consumer);
    workFlow.setProducer(producer);
    workFlow.setReplyProducer(replyProducer);
    channel.getWorkflowList().addWorkflow(workFlow);
    channel.prepare();
    return channel;
  }

  public void testReplyProducer() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    RequestReplyWorkflow workflow = new RequestReplyWorkflow();
    assertEquals(NullMessageProducer.class, workflow.getReplyProducer().getClass());
    workflow.setReplyProducer(producer);
    assertEquals(producer, workflow.getReplyProducer());
  }


  public void testReplyServiceCollection() throws Exception {
    ServiceList replyServices = new ServiceList();
    RequestReplyWorkflow workflow = new RequestReplyWorkflow();
    assertEquals(ServiceList.class, workflow.getReplyServiceCollection().getClass());
    workflow.setReplyServiceCollection(replyServices);
    assertEquals(replyServices, workflow.getReplyServiceCollection());
  }

  public void testSetReplyTimeout() throws Exception {
    TimeInterval defaultInterval = new TimeInterval(30L, TimeUnit.SECONDS);
    TimeInterval interval = new TimeInterval(60L, TimeUnit.SECONDS);

    RequestReplyWorkflow workflow = new RequestReplyWorkflow();
    assertNull(workflow.getReplyTimeout());
    assertEquals(defaultInterval.toMilliseconds(), workflow.replyTimeout());

    workflow.setReplyTimeout(interval);
    assertEquals(interval, workflow.getReplyTimeout());
    assertEquals(interval.toMilliseconds(), workflow.replyTimeout());

    workflow.setReplyTimeout(null);
    assertNull(workflow.getReplyTimeout());
    assertEquals(defaultInterval.toMilliseconds(), workflow.replyTimeout());

  }

  public void testWorkflow() throws Exception {
    Channel channel = createChannel();
    RequestReplyWorkflow workflow = (RequestReplyWorkflow) channel.getWorkflowList().get(0);
    workflow.setRetainUniqueId(true);
    MockMessageProducer replier = (MockMessageProducer) workflow.getReplyProducer();
    MockRequestReplyProducer requestor = (MockRequestReplyProducer) workflow.getProducer();
    try {
      start(channel);
      submitMessages(workflow, 1);
      doDefaultAssertions(requestor, replier);
      AdaptrisMessage replyMsg = replier.getMessages().get(0);
      assertTrue("Request Metadata", replyMsg.headersContainsKey(REQUEST_METADATA_KEY));
    }
    finally {
      stop(channel);
    }
  }

  public void testWorkflow_ReplyProducerFailure() throws Exception {
    Channel channel = new MockChannel();
    RequestReplyWorkflow workflow = new RequestReplyWorkflow();
    workflow.setConsumer(new MockMessageConsumer());
    MockMessageProducer errorProducer = new MockMessageProducer();
    workflow.setMessageErrorHandler(new StandardProcessingExceptionHandler(new StandaloneProducer(errorProducer)));
    AdaptrisMessage reply = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    AdaptrisMessageProducer replier = mock(AdaptrisMessageProducer.class);
    doThrow(new ProduceException()).when(replier).produce(any(AdaptrisMessage.class));
    doThrow(new ProduceException()).when(replier).produce(any(AdaptrisMessage.class), any(ProduceDestination.class));
    when(replier.createName()).thenReturn("mock");
    when(replier.createQualifier()).thenReturn("mock");
    when(replier.isTrackingEndpoint()).thenReturn(false);

    AdaptrisMessageProducer requestor = mock(AdaptrisMessageProducer.class);

    when(requestor.request(any(AdaptrisMessage.class))).thenReturn(reply);
    when(requestor.request(any(AdaptrisMessage.class), any(ProduceDestination.class))).thenReturn(reply);
    when(requestor.request(any(AdaptrisMessage.class), any(ProduceDestination.class), anyLong())).thenReturn(reply);
    when(requestor.request(any(AdaptrisMessage.class), anyLong())).thenReturn(reply);
    when(requestor.createName()).thenReturn("mock");
    when(requestor.createQualifier()).thenReturn("mock");
    when(requestor.isTrackingEndpoint()).thenReturn(false);

    workflow.setProducer(requestor);
    workflow.setReplyProducer(replier);
    channel.getWorkflowList().add(workflow);
    try {
      start(channel);
      submitMessages(workflow, 1);
      assertEquals(1, errorProducer.getMessages().size());
    }
    finally {
      stop(channel);
    }
  }

  public void testWorkflow_NullReply() throws Exception {
    Channel channel = new MockChannel();
    RequestReplyWorkflow workflow = new RequestReplyWorkflow();
    workflow.setConsumer(new MockMessageConsumer());


    AdaptrisMessageProducer replier = mock(AdaptrisMessageProducer.class);
    doThrow(new ProduceException()).when(replier).produce(any(AdaptrisMessage.class));
    doThrow(new ProduceException()).when(replier).produce(any(AdaptrisMessage.class), any(ProduceDestination.class));
    when(replier.createName()).thenReturn("mock");
    when(replier.createQualifier()).thenReturn("mock");
    when(replier.isTrackingEndpoint()).thenReturn(false);

    AdaptrisMessageProducer requestor = mock(AdaptrisMessageProducer.class);

    when(requestor.request(any(AdaptrisMessage.class))).thenReturn(null);
    when(requestor.request(any(AdaptrisMessage.class), any(ProduceDestination.class))).thenReturn(null);
    when(requestor.request(any(AdaptrisMessage.class), any(ProduceDestination.class), anyLong())).thenReturn(null);
    when(requestor.request(any(AdaptrisMessage.class), anyLong())).thenReturn(null);
    when(requestor.createName()).thenReturn("mock");
    when(requestor.createQualifier()).thenReturn("mock");
    when(requestor.isTrackingEndpoint()).thenReturn(false);

    workflow.setProducer(requestor);
    workflow.setReplyProducer(replier);
    channel.getWorkflowList().add(workflow);
    try {
      start(channel);
      submitMessages(workflow, 1);
    }
    finally {
      stop(channel);
    }
  }

  public void testWorkflow_SkipProducer_HasNoEffect() throws Exception {
    Channel channel = createChannel();
    RequestReplyWorkflow workflow = (RequestReplyWorkflow) channel.getWorkflowList().get(0);
    workflow.getServiceCollection().add(new MockSkipProducerService());
    MockMessageProducer replier = (MockMessageProducer) workflow.getReplyProducer();
    MockRequestReplyProducer requestor = (MockRequestReplyProducer) workflow.getProducer();
    try {
      start(channel);
      submitMessages(workflow, 1);
      doDefaultAssertions(requestor, replier);
      AdaptrisMessage replyMsg = replier.getMessages().get(0);
      assertTrue("Request Metadata", replyMsg.headersContainsKey(REQUEST_METADATA_KEY));
    }
    finally {
      stop(channel);
    }
  }

  public void testWorkflow_HasInterceptor() throws Exception {
    Channel channel = createChannel();
    RequestReplyWorkflow workflow = (RequestReplyWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer replier = (MockMessageProducer) workflow.getReplyProducer();
    MockRequestReplyProducer requestor = (MockRequestReplyProducer) workflow.getProducer();

    createChannel();
    MockWorkflowInterceptor interceptor = new MockWorkflowInterceptor();
    workflow.addInterceptor(interceptor);

    try {
      start(channel);
      submitMessages(workflow, 1);
      doDefaultAssertions(requestor, replier);
      AdaptrisMessage replyMsg = replier.getMessages().get(0);
      assertTrue("Request Metadata", replyMsg.headersContainsKey(REQUEST_METADATA_KEY));
      assertEquals(1, interceptor.messageCount());
    }
    finally {
      stop(channel);
    }
  }

  public void testWorkflow_HasObjectMetadata() throws Exception {

    Channel channel = createChannel();
    RequestReplyWorkflow workflow = (RequestReplyWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer replier = (MockMessageProducer) workflow.getReplyProducer();
    MockRequestReplyProducer requestor = (MockRequestReplyProducer) workflow.getProducer();
    try {
      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx");
      msg.addMetadata(REQUEST_METADATA_KEY, REQUEST_METADATA_VALUE);
      msg.addObjectHeader(REQUEST_OBJ_METADATA_KEY, this);
      MockMessageConsumer m = (MockMessageConsumer) workflow.getConsumer();
      m.submitMessage(msg);
      doDefaultAssertions(requestor, replier);
      AdaptrisMessage replyMsg = replier.getMessages().get(0);
      assertTrue("Request Metadata", replyMsg.headersContainsKey(REQUEST_METADATA_KEY));
      assertTrue("Contains object metadata", replyMsg.getObjectHeaders().containsKey(REQUEST_OBJ_METADATA_KEY));
    }
    finally {
      stop(channel);
    }
  }

  public void testWorkflow_IgnoreReplyMetadata() throws Exception {

    Channel channel = createChannel();
    RequestReplyWorkflow workflow = (RequestReplyWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer replier = (MockMessageProducer) workflow.getReplyProducer();
    MockRequestReplyProducer requestor = (MockRequestReplyProducer) workflow.getProducer();
    try {
      requestor.setIgnoreReplyMetadata(true);
      start(channel);
      submitMessages(workflow, 1);
      doDefaultAssertions(requestor, replier);
      AdaptrisMessage reply = replier.getMessages().get(0);
      assertTrue("Contains Request Metadata key", reply.headersContainsKey(REQUEST_METADATA_KEY));
      assertFalse("Reply Metadata key", reply.headersContainsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    }
    finally {
      stop(channel);
    }
  }

  public void testWorkflow_UseReplyMetadata() throws Exception {

    Channel channel = createChannel();
    RequestReplyWorkflow workflow = (RequestReplyWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer replier = (MockMessageProducer) workflow.getReplyProducer();
    MockRequestReplyProducer requestor = (MockRequestReplyProducer) workflow.getProducer();
    try {
      requestor.setIgnoreReplyMetadata(false);
      start(channel);
      submitMessages(workflow, 1);
      doDefaultAssertions(requestor, replier);
      AdaptrisMessage reply = replier.getMessages().get(0);
      assertTrue("Contains Request Metadata key", reply.headersContainsKey(REQUEST_METADATA_KEY));
      assertTrue("Reply Metadata key", reply.headersContainsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    }
    finally {
      stop(channel);
    }
  }

  private void submitMessages(Workflow wf, int number) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx");
    msg.addMetadata(REQUEST_METADATA_KEY, REQUEST_METADATA_VALUE);
    MockMessageConsumer m = (MockMessageConsumer) wf.getConsumer();
    for (int i = 0; i < number; i++) {
      m.submitMessage(msg);
    }
  }

  private void doDefaultAssertions(MockRequestReplyProducer requestor, MockMessageProducer replier) {
    assertTrue("Make sure all produced", requestor.getProducedMessages().size() == 1);
    assertTrue("Make sure all replies produced", replier.getMessages().size() == 1);
    AdaptrisMessage request = (AdaptrisMessage) requestor.getProducedMessages().get(0);
    AdaptrisMessage reply = replier.getMessages().get(0);
    assertEquals("Compare message ids", request.getUniqueId(), reply.getUniqueId());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    try {
      c.setConsumeConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616")));
      c.setProduceConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:2506")));
      RequestReplyWorkflow workflow = new RequestReplyWorkflow();
      workflow.getServiceCollection().addService(new Base64DecodeService());
      workflow.setConsumer(new PtpConsumer(new ConfiguredConsumeDestination("inputQueue")));
      workflow.setProducer(new PtpProducer(new ConfiguredProduceDestination("outputQueue")));
      workflow.setReplyProducer(new PtpProducer(new JmsReplyToDestination()));
      workflow.getReplyServiceCollection().addService(new Base64EncodeService());
      c.getWorkflowList().add(workflow);
      c.setUniqueId(UUID.randomUUID().toString());
      workflow.setUniqueId(UUID.randomUUID().toString());
    }
    catch (CoreException e) {
      throw new RuntimeException(e);
    }
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return RequestReplyWorkflow.class.getName();
  }

  @Override
  protected RequestReplyWorkflow createWorkflowForGenericTests() {
    return new RequestReplyWorkflow();
  }

}
