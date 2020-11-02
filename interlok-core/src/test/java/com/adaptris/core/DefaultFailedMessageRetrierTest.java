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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.UUID;
import org.junit.Test;
import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.stubs.FailFirstMockMessageProducer;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubEventHandler;

@SuppressWarnings("deprecation")
public class DefaultFailedMessageRetrierTest
    extends com.adaptris.interlok.junit.scaffolding.FailedMessageRetrierCase {


  @Test
  public void testSetter() throws Exception {
    DefaultFailedMessageRetrier retrier = create();
    try {
      retrier.setStandaloneConsumer(null);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testDuplicateWorkflows() throws Exception {
    DefaultFailedMessageRetrier dfmr = new DefaultFailedMessageRetrier();
    try {
      dfmr.addWorkflow(createWorkflow("t1"));
      dfmr.addWorkflow(createWorkflow("t1"));
      fail("Duplicate workflows should throw an Exception");
    }
    catch (CoreException e) {
      ; // expected.
    }
  }


  @Test
  public void testRetry() throws Exception {
    DefaultFailedMessageRetrier retrier = create();
    StandardWorkflow wf = createWorkflow();
    try {
      MockMessageProducer p = (MockMessageProducer) wf.getProducer();
      retrier.addWorkflow(wf);
      retrier.addWorkflow(createWorkflow());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEF");
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, wf.obtainWorkflowId());
      start(wf);
      start(retrier);
      retrier.onAdaptrisMessage(msg);
      assertEquals(1, p.getMessages().size());
    } finally {
      stop(retrier);
      stop(wf);
    }
  }

  @Test
  public void testRetryNoWorkflowId() throws Exception {
    DefaultFailedMessageRetrier retrier = create();
    StandardWorkflow wf = createWorkflow();
    try {
      MockMessageProducer p = (MockMessageProducer) wf.getProducer();
      retrier.addWorkflow(wf);
      retrier.addWorkflow(createWorkflow());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEF");
      start(wf);
      start(retrier);
      retrier.onAdaptrisMessage(msg);
      assertFalse(msg.containsKey(CoreConstants.RETRY_COUNT_KEY));
      assertEquals(0, p.getMessages().size());
    } finally {
      stop(retrier);
      stop(wf);
    }
  }

  @Test
  public void testRetryNoMatchForWorkflowId() throws Exception {
    DefaultFailedMessageRetrier retrier = create();
    StandardWorkflow wf = createWorkflow();
    try {
      MockMessageProducer p = (MockMessageProducer) wf.getProducer();
      retrier.addWorkflow(wf);
      retrier.addWorkflow(createWorkflow());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEF");
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, createWorkflow().obtainWorkflowId());
      start(retrier);
      start(wf);
      retrier.onAdaptrisMessage(msg);
      assertEquals(0, p.getMessages().size());
      assertFalse(msg.containsKey(CoreConstants.RETRY_COUNT_KEY));
    } finally {
      stop(retrier);
      stop(wf);
    }
  }

  @Test
  public void testRetryInvalidCounter() throws Exception {
    DefaultFailedMessageRetrier retrier = create();
    StandardWorkflow wf = createWorkflow();
    try {
      MockMessageProducer p = (MockMessageProducer) wf.getProducer();
      retrier.addWorkflow(wf);
      retrier.addWorkflow(createWorkflow());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEF");
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, wf.obtainWorkflowId());
      msg.addMetadata(CoreConstants.RETRY_COUNT_KEY, "fred");
      start(retrier);
      start(wf);
      retrier.onAdaptrisMessage(msg);
      assertTrue(msg.containsKey(CoreConstants.RETRY_COUNT_KEY));
      assertEquals("1", msg.getMetadataValue(CoreConstants.RETRY_COUNT_KEY));

    } finally {
      stop(retrier);
      stop(wf);
    }
  }

  @Test
  public void testMultipleRetries() throws Exception {
    DefaultFailedMessageRetrier retrier = create();
    StandardWorkflow wf = createWorkflow();
    try {
      wf.setProducer(new FailFirstMockMessageProducer());
      FailFirstMockMessageProducer p = (FailFirstMockMessageProducer) wf.getProducer();
      retrier.addWorkflow(wf);
      retrier.addWorkflow(createWorkflow());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEF");
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, wf.obtainWorkflowId());
      start(retrier);
      start(wf);
      retrier.onAdaptrisMessage(msg);
      assertTrue(msg.containsKey(CoreConstants.RETRY_COUNT_KEY));
      assertEquals("1", msg.getMetadataValue(CoreConstants.RETRY_COUNT_KEY));
      retrier.onAdaptrisMessage(msg);
      assertEquals(1, p.getMessages().size());
      assertEquals("2", msg.getMetadataValue(CoreConstants.RETRY_COUNT_KEY));

    } finally {
      stop(retrier);
      stop(wf);
    }
  }

  @Test
  public void testAdapterRetry() throws Exception {
    DefaultFailedMessageRetrier retrier = create();
    MockMessageProducer errProd = new MockMessageProducer();
    StandardProcessingExceptionHandler speh =
        new StandardProcessingExceptionHandler(new StandaloneProducer(errProd));
    Adapter adapter = createAdapterForRetry(retrier, speh);
    try {
      FailFirstMockMessageProducer workflowProducer = (FailFirstMockMessageProducer) adapter
          .getChannelList().get(0).getWorkflowList().get(0).getProducer();
      MockMessageConsumer consumer = (MockMessageConsumer) adapter.getChannelList().get(0)
          .getWorkflowList().get(0).getConsumer();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEF");
      start(adapter);
      consumer.submitMessage(msg);
      // SHould have failed
      assertEquals(1, errProd.messageCount());
      retrier.onAdaptrisMessage(errProd.getMessages().get(0));
      assertTrue(msg.containsKey(CoreConstants.RETRY_COUNT_KEY));
      assertEquals("1", msg.getMetadataValue(CoreConstants.RETRY_COUNT_KEY));
      assertEquals(1, workflowProducer.getMessages().size());
    } finally {
      stop(adapter);
    }
  }

  @Test
  public void testRoundTrip_AdapterRetry() throws Exception {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    Adapter adapter = (Adapter) marshaller.unmarshal(
        marshaller.marshal(createAdapterForRetry(create(), new StandardProcessingExceptionHandler(
            new StandaloneProducer(new MockMessageProducer())))));
    DefaultFailedMessageRetrier retrier =
        (DefaultFailedMessageRetrier) adapter.getFailedMessageRetrier();
    MockMessageProducer errProd = getErrorHandler(adapter);
    try {
      FailFirstMockMessageProducer workflowProducer = (FailFirstMockMessageProducer) adapter
          .getChannelList().get(0).getWorkflowList().get(0).getProducer();
      MockMessageConsumer consumer = (MockMessageConsumer) adapter.getChannelList().get(0)
          .getWorkflowList().get(0).getConsumer();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEF");
      start(adapter);
      consumer.submitMessage(msg);
      // SHould have failed
      assertEquals(1, errProd.messageCount());
      retrier.onAdaptrisMessage(errProd.getMessages().get(0));
      assertTrue(msg.containsKey(CoreConstants.RETRY_COUNT_KEY));
      assertEquals("1", msg.getMetadataValue(CoreConstants.RETRY_COUNT_KEY));
      assertEquals(1, workflowProducer.getMessages().size());
    } finally {
      stop(adapter);
    }
  }

  private Adapter createAdapterForRetry(FailedMessageRetrier retrier,
      ProcessingExceptionHandler errorHandler) throws Exception {
    Adapter adapter = AdapterTest.createAdapter(getName());
    adapter.setFailedMessageRetrier(retrier);
    adapter.setMessageErrorHandler(errorHandler);
    adapter.getChannelList().clear();
    Channel c = new Channel();
    c.setUniqueId(getName());
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId(getName());
    wf.setConsumer(new MockMessageConsumer());
    wf.setProducer(new FailFirstMockMessageProducer());
    c.getWorkflowList().add(wf);
    adapter.getChannelList().add(c);
    return adapter;
  }

  private MockMessageProducer getErrorHandler(Adapter adapter) {
    ServiceList list =
        (ServiceList) ((StandardProcessingExceptionHandler) adapter.getMessageErrorHandler())
            .getProcessingExceptionService();
    StandaloneProducer producer = (StandaloneProducer) list.get(0);
    return (MockMessageProducer) producer.getProducer();
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result = null;
    try {
      DefaultFailedMessageRetrier fmr = new DefaultFailedMessageRetrier();
      FsConsumer consumer = new FsConsumer().withBaseDirectoryUrl("/path/to/retry-directory");
      StandaloneConsumer c = new StandaloneConsumer(consumer);
      fmr.setStandaloneConsumer(c);
      result = new Adapter();
      result.setFailedMessageRetrier(fmr);
      result.setChannelList(new ChannelList());
      result.setEventHandler(new StubEventHandler());
      result.setUniqueId(UUID.randomUUID().toString());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return DefaultFailedMessageRetrier.class.getCanonicalName();
  }

  @Override
  protected DefaultFailedMessageRetrier create() {
    return new DefaultFailedMessageRetrier();
  }

  @Override
  protected DefaultFailedMessageRetrier createForExamples() {
    DefaultFailedMessageRetrier fmr = new DefaultFailedMessageRetrier();
    FsConsumer consumer = new FsConsumer();
    consumer.setBaseDirectoryUrl("/path/to/retry-directory");
    consumer.setEncoder(new MimeEncoder(true, null, null));
    fmr.setStandaloneConsumer(new StandaloneConsumer(consumer));
    return fmr;
  }
}
