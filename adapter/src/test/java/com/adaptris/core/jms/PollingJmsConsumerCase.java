package com.adaptris.core.jms;

import java.util.concurrent.TimeUnit;

import com.adaptris.util.TimeInterval;

public abstract class PollingJmsConsumerCase extends JmsConsumerCase {

  public PollingJmsConsumerCase(String name) {
    super(name);
  }

  protected abstract JmsPollingConsumerImpl createConsumer();

  public void testSetReceiveWait() throws Exception {
    JmsPollingConsumerImpl consumer = createConsumer();
    assertNull(consumer.getReceiveTimeout());
    assertEquals(2000, consumer.receiveTimeout());

    TimeInterval interval = new TimeInterval(1L, TimeUnit.MINUTES);
    TimeInterval bad = new TimeInterval(-1L, TimeUnit.MILLISECONDS);

    consumer.setReceiveTimeout(interval);
    assertEquals(interval, consumer.getReceiveTimeout());
    assertEquals(interval.toMilliseconds(), consumer.receiveTimeout());

    consumer.setReceiveTimeout(bad);
    assertEquals(bad, consumer.getReceiveTimeout());
    assertEquals(2000L, consumer.receiveTimeout());

    consumer.setReceiveTimeout(null);
    assertNull(consumer.getReceiveTimeout());
    assertEquals(2000, consumer.receiveTimeout());

  }
}
