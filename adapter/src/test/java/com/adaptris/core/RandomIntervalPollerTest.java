package com.adaptris.core;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.TimeInterval;

public class RandomIntervalPollerTest extends BaseCase {

  public RandomIntervalPollerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSetConstructors() throws Exception {
    RandomIntervalPoller p = new RandomIntervalPoller();
    p = new RandomIntervalPoller(new TimeInterval(10L, TimeUnit.SECONDS));
  }

  
  public void testLifecycle() throws Exception {
    PollingTrigger consumer = new PollingTrigger();
    consumer.setPoller(new RandomIntervalPoller(new TimeInterval(100L, TimeUnit.MILLISECONDS)));
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
