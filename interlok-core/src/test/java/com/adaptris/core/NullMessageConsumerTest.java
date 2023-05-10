package com.adaptris.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.adaptris.core.util.LifecycleHelper;

public class NullMessageConsumerTest {

  @Test
  public void testMessageFactory() throws Exception {
    NullMessageConsumer consumer = new NullMessageConsumer();
    assertNull(consumer.getMessageFactory());
  }

  @Test
  public void testNewThreadName() {
    NullMessageConsumer consumer = new NullMessageConsumer();
    assertNotNull(consumer.newThreadName());
  }


  @Test
  public void testLifecycle() throws Exception {
    NullMessageConsumer consumer = new NullMessageConsumer();
    try {
      LifecycleHelper.prepare(consumer);
      LifecycleHelper.initAndStart(consumer);
    } finally {
      LifecycleHelper.stopAndClose(consumer);
    }
  }

}
