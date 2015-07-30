package com.adaptris.core.jms;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.BaseCase;
import com.adaptris.util.TimeInterval;

public abstract class ActiveJmsConnectionErrorHandlerCase extends BaseCase {

  public ActiveJmsConnectionErrorHandlerCase(String name) {
    super(name);
  }

  public void testRetryInterval() {
    ActiveJmsConnectionErrorHandler handler = new ActiveJmsConnectionErrorHandler();
    assertNull(handler.getCheckInterval());
    assertEquals(5000, handler.retryInterval());

    TimeInterval interval = new TimeInterval(1L, TimeUnit.MINUTES);
    TimeInterval bad = new TimeInterval(0L, TimeUnit.MILLISECONDS);

    handler.setCheckInterval(interval);
    assertEquals(interval, handler.getCheckInterval());
    assertEquals(interval.toMilliseconds(), handler.retryInterval());

    handler.setCheckInterval(bad);
    assertEquals(bad, handler.getCheckInterval());
    assertEquals(5000, handler.retryInterval());

    handler.setCheckInterval(null);
    assertNull(handler.getCheckInterval());
    assertEquals(5000, handler.retryInterval());
  }

  public void testAdditionalLogging() {
    ActiveJmsConnectionErrorHandler ajceh = new ActiveJmsConnectionErrorHandler();
    assertNull(ajceh.getAdditionalLogging());
    assertFalse(ajceh.additionalLogging());
    ajceh.setAdditionalLogging(Boolean.TRUE);
    assertNotNull(ajceh.getAdditionalLogging());
    assertEquals(true, ajceh.additionalLogging());
    assertEquals(Boolean.TRUE, ajceh.getAdditionalLogging());
    ajceh.setAdditionalLogging(null);
    assertNull(ajceh.getAdditionalLogging());
    assertFalse(ajceh.additionalLogging());
  }
}
