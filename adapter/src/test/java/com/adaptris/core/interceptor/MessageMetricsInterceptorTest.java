package com.adaptris.core.interceptor;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class MessageMetricsInterceptorTest extends TestCase {

  private MessageMetricsInterceptor metricsInterceptor;

  @Override
  public void setUp() throws Exception {
    metricsInterceptor = new MessageMetricsInterceptor();
    metricsInterceptor.setTimesliceDuration(new TimeInterval(5L, TimeUnit.SECONDS));
    metricsInterceptor.setTimesliceHistoryCount(2);
  }

  @Override
  public void tearDown() throws Exception {
    LifecycleHelper.stop(metricsInterceptor);
    LifecycleHelper.close(metricsInterceptor);
  }

  public void testInterceptor() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    assertEquals(0, metricsInterceptor.getCacheArray().size());
    submitMessage(message);
    // Make sure there is 1 message in the cache
    assertEquals(1, metricsInterceptor.getCacheArray().size());
    assertEquals(1, metricsInterceptor.getCacheArray().get(0).getTotalMessageCount());
  }

  public void testInterceptor_WithException() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.getObjectMetadata().put(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception());
    assertEquals(0, metricsInterceptor.getCacheArray().size());
    submitMessage(message);
    assertEquals(1, metricsInterceptor.getCacheArray().size());
    assertEquals(1, metricsInterceptor.getCacheArray().get(0).getTotalMessageCount());
    assertEquals(1, metricsInterceptor.getCacheArray().get(0).getTotalMessageErrorCount());
  }

  public void testCreatesNewTimeSliceAfterTimeDelay() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    assertEquals(0, metricsInterceptor.getCacheArray().size());
    submitMessage(message);

    waitFor(6);

    assertEquals(1, metricsInterceptor.getCacheArray().size());
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getCacheArray().size());
    assertEquals(1, metricsInterceptor.getCacheArray().get(0).getTotalMessageCount());
    assertEquals(2, metricsInterceptor.getCacheArray().get(1).getTotalMessageCount());
  }

  public void testDoesNotCreateMoreHistoryThanSpecified() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    assertEquals(0, metricsInterceptor.getCacheArray().size());
    submitMessage(message);

    waitFor(6);

    assertEquals(1, metricsInterceptor.getCacheArray().size());
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getCacheArray().size());

    waitFor(6);

    submitMessage(message);
    submitMessage(message);
    submitMessage(message);
    // Should still only be 2 time slices
    assertEquals(2, metricsInterceptor.getCacheArray().size());
    assertEquals(2, metricsInterceptor.getCacheArray().get(0).getTotalMessageCount());
    assertEquals(3, metricsInterceptor.getCacheArray().get(1).getTotalMessageCount());
  }


  public void testMultiThreadedSingleMetricsInterceptorInstance() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    // 130 messages in total
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(10).run();
    new MetricsInserterThread(30).run();
    new MetricsInserterThread(50).run();
    new MetricsInserterThread(20).run();

    Thread.sleep(5000); // Lets allow the threads to finish
    assertEquals(130, metricsInterceptor.getCacheArray().get(0).getTotalMessageCount());
  }

  private void waitFor(int seconds) throws Exception {
    Thread.sleep(seconds * 1000);
  }

  private void submitMessage(AdaptrisMessage msg) {
    metricsInterceptor.workflowStart(msg);
    metricsInterceptor.workflowEnd(msg, msg);
  }

  /**
   * Test class that simply whacks messages into the interceptor
   * @author Aaron
   */
  class MetricsInserterThread extends Thread {
    int numMessages;

    MetricsInserterThread(int numMessages) {
      this.numMessages = numMessages;
    }

    @Override
    public void run() {
      for(int counter = 1; counter <= numMessages; counter ++) {
        AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
        submitMessage(message);
      }
    }
  }
}
