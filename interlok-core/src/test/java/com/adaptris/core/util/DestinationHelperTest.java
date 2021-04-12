package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.stubs.MockMessageListener;

@SuppressWarnings("deprecation")
public class DestinationHelperTest extends DestinationHelper {

  @Test
  public void testMustHaveEither() {
    mustHaveEither("x");
  }

  @Test
  public void testConsumeDestination() {
    assertNull(consumeDestination(null));
  }

  @Test
  public void testFilterExpression() {
    assertNull(filterExpression(null));
    assertEquals("x", filterExpression("x"));
  }

  @Test
  public void testThreadNameAdaptrisMessageListenerConsumeDestination() {
    String currentThreadName = Thread.currentThread().getName();
    assertEquals(currentThreadName, threadName(null, currentThreadName));
    assertEquals("MockMessageListener", threadName(new MockMessageListener(), null));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testConsumeDestinationWarning() {
    logConsumeDestinationWarning(false, () -> { }, "{} warning", "this is a");
    logConsumeDestinationWarning(false, () -> { }, null, "{} warning", "this is a");
  }

  @Test
  public void testLogWarningIfNotNull() {
    logWarningIfNotNull(false, () -> { }, "{} warning", "this is a");
    logWarningIfNotNull(false, () -> { }, null, "{} warning", "this is a");
  }

  @Test
  public void testProduceDestination() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNull(resolveProduceDestination(null, msg));
    assertEquals("x", resolveProduceDestination("x", msg));
  }

}
