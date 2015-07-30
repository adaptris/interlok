package com.adaptris.core.interceptor;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class MetadataCountInterceptorTest extends TestCase {

  private static final long MAX_WAIT = 65000L;
  private static final long DEFAULT_WAIT_INTERVAL = 500L;

  private static final TimeInterval TIME_INTERVAL = new TimeInterval(1500L, TimeUnit.MILLISECONDS);

  private static final String COUNTER_1 = "counter1";
  private static final String COUNTER_2 = "counter2";
  private static final String METADATA_KEY = "key";

  private MetadataCountInterceptor metricsInterceptor;

  @Override
  public void setUp() throws Exception {
    metricsInterceptor = new MetadataCountInterceptor(METADATA_KEY);
    metricsInterceptor.setTimesliceDuration(TIME_INTERVAL);
    metricsInterceptor.setTimesliceHistoryCount(2);
  }

  @Override
  public void tearDown() throws Exception {
    LifecycleHelper.stop(metricsInterceptor);
    LifecycleHelper.close(metricsInterceptor);
  }

  public void testInit_NoMetadataKey() throws Exception {
    metricsInterceptor = new MetadataCountInterceptor();
    try {
      LifecycleHelper.init(metricsInterceptor);
      fail();
    }
    catch (CoreException expected) {
    }

  }

  public void testInterceptor() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    AdaptrisMessage message = createMessage(COUNTER_1);

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    // Make sure there is 1 message in the cache
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(1, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(0, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
  }

  public void testInterceptor_NoMetadataValue() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    // Make sure there is 1 message in the cache
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(0, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(0, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
  }

  public void testCreatesNewTimeSliceAfterTimeDelay() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = createMessage(COUNTER_1);

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(1, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(0, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
    waitFor(3);
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getStats().size());
    assertEquals(1, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(0, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
    assertEquals(2, metricsInterceptor.getStats().get(1).getValue(COUNTER_1));
    assertEquals(0, metricsInterceptor.getStats().get(1).getValue(COUNTER_2));
  }

  public void testDoesNotCreateMoreHistoryThanSpecified() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = createMessage(COUNTER_1);

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);

    waitFor(3);

    assertEquals(1, metricsInterceptor.getStats().size());
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getStats().size());

    waitFor(3);

    submitMessage(message);
    submitMessage(message);
    submitMessage(message);
    // Should still only be 2 time slices
    assertEquals(2, metricsInterceptor.getStats().size());
  }


  public void testMultiThreadedSingleMetricsInterceptorInstance() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    new MetricsInserterThread(20, COUNTER_1).run();
    new MetricsInserterThread(20, COUNTER_2).run();
    new MetricsInserterThread(20, COUNTER_1).run();
    new MetricsInserterThread(20, COUNTER_2).run();
    new MetricsInserterThread(20, COUNTER_1).run();

    Thread.sleep(5000); // Lets allow the threads to finish
    assertEquals(60, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(40, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
  }

  private void waitFor(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    }
    catch (InterruptedException e) {
    }
  }

  private AdaptrisMessage createMessage(String metadataValue) {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata(new MetadataElement(METADATA_KEY, metadataValue));
    return message;
  }

  private void submitMessage(AdaptrisMessage msg) {
    metricsInterceptor.workflowStart(msg);
    metricsInterceptor.workflowEnd(msg, msg);
  }

  class MetricsInserterThread extends Thread {
    int numMessages;
    String value;
    MetricsInserterThread(int numMessages, String value) {
      this.numMessages = numMessages;
      this.value = value;
    }

    @Override
    public void run() {
      for(int counter = 1; counter <= numMessages; counter ++) {
        submitMessage(createMessage(value));
      }
    }
  }
}
