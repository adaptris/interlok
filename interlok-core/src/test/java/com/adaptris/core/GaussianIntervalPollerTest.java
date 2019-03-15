package com.adaptris.core;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.TimeInterval;

public class GaussianIntervalPollerTest extends BaseCase {


  public GaussianIntervalPollerTest(String name) {
    super(name);
  }

  public void testSetConstructors() throws Exception {
    GaussianIntervalPoller p = new GaussianIntervalPoller();
    p = new GaussianIntervalPoller(new TimeInterval(0L, TimeUnit.SECONDS), new TimeInterval(10L, TimeUnit.SECONDS));
  }

  public void testInit_standardDeviation() throws Exception{
    GaussianIntervalPoller p = new GaussianIntervalPoller(new TimeInterval(0L, TimeUnit.SECONDS), new TimeInterval(0L, TimeUnit.SECONDS));
    try {
      p.init();
      fail("Expected exception not thrown");
    }
    catch (CoreException expected) {

    }
    p.setStandardDeviationInterval(new TimeInterval(-2L, TimeUnit.SECONDS));
    try {
      p.init();
      fail("Expected exception not thrown");
    }
    catch (CoreException expected) {

    }
    p.setStandardDeviationInterval(new TimeInterval(10L, TimeUnit.SECONDS));
    p.init();
  }
  public void testInit_mean() throws Exception{
    GaussianIntervalPoller p = new GaussianIntervalPoller(new TimeInterval(-2L, TimeUnit.SECONDS), new TimeInterval(10L, TimeUnit.SECONDS));
    try {
      p.init();
      fail("Expected exception not thrown");
    }
    catch (CoreException expected) {

    }
    p.setMeanInterval(new TimeInterval(0L, TimeUnit.SECONDS));
    p.init();
    p.setMeanInterval(new TimeInterval(10L, TimeUnit.SECONDS));
    p.init();
  }

  public void testSetMeanInterval() throws Exception {
    TimeInterval defaultMeanInterval = new TimeInterval(0L, TimeUnit.SECONDS);
    TimeInterval meanInterval = new TimeInterval(60L, TimeUnit.SECONDS);

    GaussianIntervalPoller p = new GaussianIntervalPoller();

    assertNull(p.getMeanInterval());
    assertEquals(defaultMeanInterval.toMilliseconds(), p.meanInterval());

    p.setMeanInterval(meanInterval);
    assertEquals(meanInterval, p.getMeanInterval());
    assertEquals(meanInterval.toMilliseconds(), p.meanInterval());

    p.setMeanInterval(null);
    assertNull(p.getMeanInterval());
    assertEquals(defaultMeanInterval.toMilliseconds(), p.meanInterval());

  }

  public void testSetStandardDeviationInterval() throws Exception {
    TimeInterval defaultStandardDeviationInterval = new TimeInterval(20L, TimeUnit.SECONDS);
    TimeInterval standardDeviationInterval = new TimeInterval(60L, TimeUnit.SECONDS);

    GaussianIntervalPoller p = new GaussianIntervalPoller();

    assertNull(p.getStandardDeviationInterval());
    assertEquals(defaultStandardDeviationInterval.toMilliseconds(), p.standardDeviationInterval());

    p.setStandardDeviationInterval(standardDeviationInterval);
    assertEquals(standardDeviationInterval, p.getStandardDeviationInterval());
    assertEquals(standardDeviationInterval.toMilliseconds(), p.standardDeviationInterval());

    p.setStandardDeviationInterval(null);
    assertNull(p.getStandardDeviationInterval());
    assertEquals(defaultStandardDeviationInterval.toMilliseconds(), p.standardDeviationInterval());

  }

  public void testLifecycle() throws Exception {
    PollingTrigger consumer = new PollingTrigger();
    consumer.setPoller(new GaussianIntervalPoller(new TimeInterval(0L, TimeUnit.SECONDS), new TimeInterval(100L, TimeUnit.MILLISECONDS)));
    MockMessageProducer producer = new MockMessageProducer();

    MockChannel channel = new MockChannel();
    StandardWorkflow workflow = new StandardWorkflow();
    workflow.setConsumer(consumer);
    workflow.setProducer(producer);
    channel.getWorkflowList().add(workflow);
    try {
      channel.requestClose();
      channel.requestStart();
      waitForMessages(producer, 1);

      channel.requestStop();
      producer.getMessages().clear();

      channel.requestStart();
      waitForMessages(producer, 1);

      channel.requestClose();
      producer.getMessages().clear();

      channel.requestStart();
      waitForMessages(producer, 1);
    }
    finally {
      channel.requestClose();
    }
  }
}
