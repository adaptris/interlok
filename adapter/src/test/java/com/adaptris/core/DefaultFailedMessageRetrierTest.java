package com.adaptris.core;

import java.util.UUID;

import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.stubs.StubEventHandler;

public class DefaultFailedMessageRetrierTest extends FailedMessageRetrierCase {

  public DefaultFailedMessageRetrierTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

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

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result = null;
    try {
      DefaultFailedMessageRetrier fmr = new DefaultFailedMessageRetrier();
      FsConsumer consumer = new FsConsumer();
      consumer.setDestination(new ConfiguredConsumeDestination(
          "/path/to/retry-directory"));
      StandaloneConsumer c = new StandaloneConsumer();
      c.setConsumer(consumer);
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
  protected FailedMessageRetrier create() {
    return new DefaultFailedMessageRetrier();
  }

  @Override
  protected DefaultFailedMessageRetrier createForExamples() {
    DefaultFailedMessageRetrier fmr = new DefaultFailedMessageRetrier();
    FsConsumer consumer = new FsConsumer(new ConfiguredConsumeDestination("/path/to/retry-directory"));
    consumer.setEncoder(new MimeEncoder(true, null, null));
    fmr.setStandaloneConsumer(new StandaloneConsumer(consumer));
    return fmr;
  }
}