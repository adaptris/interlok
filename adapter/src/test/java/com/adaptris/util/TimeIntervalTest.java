package com.adaptris.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;


public class TimeIntervalTest {

  @Test
  public void testDefaultInterval() throws Exception {
    TimeInterval t = new TimeInterval();
    assertNull(t.getInterval());
    assertNull(t.getUnit());
    assertEquals(10000, t.toMilliseconds());
  }

  @Test
  public void testConstructor_String() throws Exception {
    TimeInterval t = new TimeInterval(20L, TimeUnit.MINUTES.name());
    assertNotNull(t.getInterval());
    assertNotNull(t.getUnit());
    assertEquals(TimeUnit.MINUTES, t.getUnit());
    assertEquals(TimeUnit.MINUTES.toMillis(20L), t.toMilliseconds());
    
    TimeInterval t2 = new TimeInterval(20L, "Unknown");
    assertNotNull(t2.getInterval());
    assertNotNull(t2.getUnit());
    assertEquals(TimeUnit.SECONDS, t2.getUnit());
    assertEquals(TimeUnit.SECONDS.toMillis(20L), t2.toMilliseconds());
  }

  @Test
  public void testConstructor_TimeUnit() throws Exception {
    TimeInterval t = new TimeInterval(20L, TimeUnit.MINUTES);
    assertNotNull(t.getInterval());
    assertNotNull(t.getUnit());
    assertEquals(TimeUnit.MINUTES, t.getUnit());
    assertEquals(TimeUnit.MINUTES.toMillis(20L), t.toMilliseconds());

    TimeInterval t2 = new TimeInterval(20L, (TimeUnit) null);
    assertNotNull(t2.getInterval());
    assertNull(t2.getUnit());
    assertEquals(TimeUnit.SECONDS.toMillis(20L), t2.toMilliseconds());
  }

}

