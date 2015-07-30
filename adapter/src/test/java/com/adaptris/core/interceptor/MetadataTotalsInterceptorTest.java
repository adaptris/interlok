package com.adaptris.core.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class MetadataTotalsInterceptorTest extends TestCase {

  private static final String COUNTER_1 = "counter1";
  private static final String COUNTER_2 = "counter2";
  private static final TimeInterval TIME_INTERVAL = new TimeInterval(1500L, TimeUnit.MILLISECONDS);

  private MetadataTotalsInterceptor metricsInterceptor;

  @Override
  public void setUp() throws Exception {
    metricsInterceptor = new MetadataTotalsInterceptor(new ArrayList(Arrays.asList(new String[]
    {
        COUNTER_1, COUNTER_2, this.getClass().getSimpleName()
    })));
    metricsInterceptor.setTimesliceDuration(TIME_INTERVAL);
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
    AdaptrisMessage message = createMessage();

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    // Make sure there is 1 message in the cache
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(10, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(10, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
    assertEquals(0, metricsInterceptor.getStats().get(0).getValue(this.getClass().getSimpleName()));
    assertEquals(0, metricsInterceptor.getStats().get(0).getValue("blah"));
  }

  public void testCreatesNewTimeSliceAfterTimeDelay() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = createMessage();

    assertEquals(0, metricsInterceptor.getStats().size());
    submitMessage(message);
    waitFor(3);
    assertEquals(1, metricsInterceptor.getStats().size());
    assertEquals(10, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(10, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
    submitMessage(message);
    submitMessage(message);

    assertEquals(2, metricsInterceptor.getStats().size());
    assertEquals(10, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(10, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
    assertEquals(20, metricsInterceptor.getStats().get(1).getValue(COUNTER_1));
    assertEquals(20, metricsInterceptor.getStats().get(1).getValue(COUNTER_2));
  }

  public void testDoesNotCreateMoreHistoryThanSpecified() throws Exception {
    LifecycleHelper.init(metricsInterceptor);
    LifecycleHelper.start(metricsInterceptor);

    AdaptrisMessage message = createMessage();

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

    // 130 messages in total
    // Each adding a counter of 10... So, we should get 1k for the counters.
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(20).run();
    new MetricsInserterThread(20).run();

    Thread.sleep(5000); // Lets allow the threads to finish
    assertEquals(1000, metricsInterceptor.getStats().get(0).getValue(COUNTER_1));
    assertEquals(1000, metricsInterceptor.getStats().get(0).getValue(COUNTER_2));
  }

  private void waitFor(int seconds) {
    try {
    Thread.sleep(seconds * 1000);
    }
    catch (InterruptedException e) {
    }
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata(new MetadataElement(COUNTER_1, "10"));
    message.addMetadata(new MetadataElement(COUNTER_2, "10"));
    return message;
  }

  private void submitMessage(AdaptrisMessage msg) {
    metricsInterceptor.workflowStart(msg);
    metricsInterceptor.workflowEnd(msg, msg);
  }

  class MetricsInserterThread extends Thread {
    int numMessages;

    MetricsInserterThread(int numMessages) {
      this.numMessages = numMessages;
    }

    @Override
    public void run() {
      for(int counter = 1; counter <= numMessages; counter ++) {
        submitMessage(createMessage());
      }
    }
  }
}
