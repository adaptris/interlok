package com.adaptris.core;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsReplyToDestination;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.services.Base64DecodeService;
import com.adaptris.core.services.Base64EncodeService;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.MockRequestReplyProducer;
import com.adaptris.core.stubs.MockSkipProducerService;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;

public class RequestReplyWorkflowTest extends ExampleWorkflowCase {

  public static final String REQUEST_METADATA_VALUE = "RequestMetadataValue";
  public static final String REQUEST_OBJ_METADATA_KEY = "RequestObjectMetadataKey";
  public static final String REQUEST_METADATA_KEY = "RequestMetadataKey";
  // private RequestReplyWorkflow workFlow;
  // private MockMessageConsumer consumer;
  // private MockRequestReplyProducer producer;
  // private MockMessageProducer replyProducer;

  private static Log logR = LogFactory.getLog(RequestReplyWorkflowTest.class);

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
    MockMessageProducer replier = (MockMessageProducer) workflow.getReplyProducer();
    MockRequestReplyProducer requestor = (MockRequestReplyProducer) workflow.getProducer();
    try {
      start(channel);
      submitMessages(workflow, 1);
      doDefaultAssertions(requestor, replier);
      AdaptrisMessage replyMsg = replier.getMessages().get(0);
      assertTrue("Request Metadata", replyMsg.containsKey(REQUEST_METADATA_KEY));
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
      assertTrue("Request Metadata", replyMsg.containsKey(REQUEST_METADATA_KEY));
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
      assertTrue("Request Metadata", replyMsg.containsKey(REQUEST_METADATA_KEY));
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
      msg.getObjectMetadata().put(REQUEST_OBJ_METADATA_KEY, this);
      MockMessageConsumer m = (MockMessageConsumer) workflow.getConsumer();
      m.submitMessage(msg);
      doDefaultAssertions(requestor, replier);
      AdaptrisMessage replyMsg = replier.getMessages().get(0);
      assertTrue("Request Metadata", replyMsg.containsKey(REQUEST_METADATA_KEY));
      assertTrue("Contains object metadata", replyMsg.getObjectMetadata().containsKey(REQUEST_OBJ_METADATA_KEY));
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
      assertTrue("Contains Request Metadata key", reply.containsKey(REQUEST_METADATA_KEY));
      assertFalse("Reply Metadata key", reply.containsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
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
      assertTrue("Contains Request Metadata key", reply.containsKey(REQUEST_METADATA_KEY));
      assertTrue("Reply Metadata key", reply.containsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
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

  @Override
  public void testLicenseCombinations() throws Exception {
    super.testLicenseCombinations();
    assertEquals(true, createReqRepWorkflow(true, true).isEnabled(new LicenseStub()));
    assertEquals(false, createReqRepWorkflow(false, true).isEnabled(new LicenseStub()));
    assertEquals(false, createReqRepWorkflow(false, false).isEnabled(new LicenseStub()));
    assertEquals(false, createReqRepWorkflow(true, false).isEnabled(new LicenseStub()));
  }

  private RequestReplyWorkflow createReqRepWorkflow(boolean replyLicensed, boolean replyServicesLicensed) throws Exception {
    RequestReplyWorkflow wf = (RequestReplyWorkflow) createWorkflowLicenseCombo(true, true, true);

    if (!replyLicensed) {
      wf.setReplyProducer(new MockMessageProducer() {
        @Override
        public boolean isEnabled(License l) {
          return false;
        }
      });

    }
    if (!replyServicesLicensed) {
      wf.setReplyServiceCollection(new ServiceList() {
        @Override
        public boolean isEnabled(License l) {
          return false;
        }
      });
    }
    return wf;
  }

}