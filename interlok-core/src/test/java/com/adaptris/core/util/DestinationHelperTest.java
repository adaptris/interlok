package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.stubs.MockMessageListener;

public class DestinationHelperTest extends DestinationHelper {

  @Test
  public void testMustHaveEither_ConsumeDestination() {
    mustHaveEither("x", (ConsumeDestination) null);
    mustHaveEither(null, new ConfiguredConsumeDestination("hello"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMustHaveEither_ConsumeDestination_Illegal() {
    mustHaveEither(null, (ConsumeDestination) null);
  }


  @Test
  public void testMustHaveEither_ProduceDestination() {
    mustHaveEither("x", (ProduceDestination) null);
    mustHaveEither(null, new ConfiguredProduceDestination("hello"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMustHaveEither_ConfiguredProduceDestination_Illegal() {
    mustHaveEither(null, (ConfiguredProduceDestination) null);
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
  @SuppressWarnings("deprecation")
  public void testConsumeDestinationWarning() {
    logConsumeDestinationWarning(false, () -> { }, new ConfiguredConsumeDestination(), "{} warning", "this is a");
    logConsumeDestinationWarning(false, () -> { }, null, "{} warning", "this is a");
  }

  @Test
  public void testLogWarningIfNotNull() {
    logWarningIfNotNull(false, () -> { }, new Object(), "{} warning", "this is a");
    logWarningIfNotNull(false, () -> { }, null, "{} warning", "this is a");
  }


  @Test
  public void testProduceDestination() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ConfiguredProduceDestination dest = new ConfiguredProduceDestination("dest");
    assertNull(resolveProduceDestination(null, null, msg));
    assertEquals("x", resolveProduceDestination("x", null, msg));
    assertEquals("dest", resolveProduceDestination(null, dest, msg));
    assertEquals("dest", resolveProduceDestination("x", dest, msg));
  }

  @Test(expected = ProduceException.class)
  public void testProduceDestination_WithException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    resolveProduceDestination(null, (m) -> {
      throw new CoreException();
    }, msg);
  }

}
