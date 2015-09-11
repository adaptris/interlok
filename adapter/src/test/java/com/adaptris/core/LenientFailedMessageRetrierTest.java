package com.adaptris.core;

import java.util.UUID;

import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.stubs.MockMessageProducer;

@SuppressWarnings("deprecation")
public class LenientFailedMessageRetrierTest extends FailedMessageRetrierCase {

  public LenientFailedMessageRetrierTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testDuplicateWorkflows() throws Exception {
    LenientFailedMessageRetrier dfmr = new LenientFailedMessageRetrier();
    try {
      dfmr.addWorkflow(createWorkflow("t1"));
      dfmr.addWorkflow(createWorkflow("t1"));
    }
    catch (CoreException e) {
      fail("Duplicate workflows should NOT throw an Exception");
    }
  }

  public void testDuplicateWorkflowRetry() throws Exception {
    LenientFailedMessageRetrier dfmr = new LenientFailedMessageRetrier();
    StandardWorkflow wf1 = createWorkflow("t1");
    StandardWorkflow wf2 = createWorkflow("t1");
    try {
      MockMessageProducer p1 = (MockMessageProducer) wf1.getProducer();

      MockMessageProducer p2 = (MockMessageProducer) wf2.getProducer();
      dfmr.addWorkflow(wf1);
      dfmr.addWorkflow(wf2);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEF");
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, wf1.obtainWorkflowId());
      start(dfmr);
      start(wf1);
      start(wf2);
      dfmr.onAdaptrisMessage(msg);
      assertEquals(1, p1.getMessages().size());
      assertEquals(0, p2.getMessages().size());
    }
    finally {
      stop(dfmr);
      stop(wf1);
      stop(wf2);
    }

  }

  @Override
  protected LenientFailedMessageRetrier createForExamples() {
    LenientFailedMessageRetrier fmr = new LenientFailedMessageRetrier();
    FsConsumer consumer = new FsConsumer(new ConfiguredConsumeDestination("/path/to/retry-directory"));
    consumer.setEncoder(new MimeEncoder(true, null, null));
    fmr.setStandaloneConsumer(new StandaloneConsumer(consumer));
    return fmr;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return LenientFailedMessageRetrier.class.getCanonicalName();
  }

  @Override
  protected LenientFailedMessageRetrier create() {
    LenientFailedMessageRetrier r = new LenientFailedMessageRetrier();
    r.setUniqueId(UUID.randomUUID().toString());
    return r;
  }
}