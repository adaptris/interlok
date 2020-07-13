package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.stubs.MockMessageListener;

public class DestinationHelperTest extends DestinationHelper {

  @Test
  public void testMustHaveEither() {
    mustHaveEither("x", null);
    mustHaveEither(null, new ConfiguredConsumeDestination("hello"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMustHaveEither_Illegal() {
    mustHaveEither(null, null);
  }


  @Test
  public void testConsumeDestination() {
    assertNull(consumeDestination(null, null));
    assertEquals("x", consumeDestination("x", null));
    assertEquals("dest",
        consumeDestination(null, new ConfiguredConsumeDestination("dest", "filter", "threadname")));
    assertEquals("dest",
        consumeDestination("y", new ConfiguredConsumeDestination("dest", "filter", "threadname")));
  }

  @Test
  public void testFilterExpression() {
    assertNull(filterExpression(null, null));
    assertEquals("x", filterExpression("x", null));
    assertEquals("filter",
        filterExpression(null, new ConfiguredConsumeDestination("dest", "filter", "threadname")));
    assertEquals("filter",
        filterExpression("y", new ConfiguredConsumeDestination("dest", "filter", "threadname")));
  }

  @Test
  public void testThreadNameAdaptrisMessageListenerConsumeDestination() {
    String currentThreadName = Thread.currentThread().getName();
    assertEquals(currentThreadName, threadName(null, null));
    assertEquals("MockMessageListener", threadName(new MockMessageListener(), null));
    assertEquals("threadname",
        threadName(null,
        new ConfiguredConsumeDestination("dest", "filter", "threadname")));
    assertEquals("threadname",
        threadName(new MockMessageListener(),
            new ConfiguredConsumeDestination("dest", "filter", "threadname")));
  }

  @Test
  public void testProduceDestination() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ConfiguredProduceDestination dest = new ConfiguredProduceDestination("dest");
    assertNull(produceDestination(null, null, msg));
    assertEquals("x", produceDestination("x", null, msg));
    assertEquals("dest", produceDestination(null, dest, msg));
    assertEquals("dest", produceDestination("x", dest, msg));
  }

}
