package com.adaptris.core;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.TimeInterval;

public class FixedIntervalPollerTest extends BaseCase {

  public FixedIntervalPollerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSetConstructors() throws Exception {
    FixedIntervalPoller p = new FixedIntervalPoller();
    p = new FixedIntervalPoller(new TimeInterval(10L, TimeUnit.SECONDS));
  }

  public void testSetPollInterval() throws Exception {
    TimeInterval defaultInterval = new TimeInterval(20L, TimeUnit.SECONDS);
    TimeInterval interval = new TimeInterval(60L, TimeUnit.SECONDS);

    FixedIntervalPoller p = new FixedIntervalPoller();

    assertNull(p.getPollInterval());
    assertEquals(defaultInterval.toMilliseconds(), p.pollInterval());

    p.setPollInterval(interval);
    assertEquals(interval, p.getPollInterval());
    assertEquals(interval.toMilliseconds(), p.pollInterval());

    p.setPollInterval(null);
    assertNull(p.getPollInterval());
    assertEquals(defaultInterval.toMilliseconds(), p.pollInterval());

  }
  
  
  public void testLifecycle() throws Exception {
    PollingTrigger consumer = new PollingTrigger();
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(100L, TimeUnit.MILLISECONDS)));
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
