/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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


  @Test
  public void testToMillisecondsDefaultIfNull() throws Exception {
    TimeInterval t = new TimeInterval(20L, TimeUnit.MINUTES);
    TimeInterval defaultTime = new TimeInterval(10L, TimeUnit.SECONDS);
    assertEquals(TimeUnit.MINUTES.toMillis(20L), TimeInterval.toMillisecondsDefaultIfNull(t, 0L));
    assertEquals(TimeUnit.MINUTES.toMillis(20L),
        TimeInterval.toMillisecondsDefaultIfNull(t, defaultTime));
    assertEquals(TimeUnit.SECONDS.toMillis(10L),
        TimeInterval.toMillisecondsDefaultIfNull(null, defaultTime));
    assertEquals(0, TimeInterval.toMillisecondsDefaultIfNull(null, 0));
  }

  @Test
  public void testToSecondsDefaultIfNull() throws Exception {
    TimeInterval t = new TimeInterval(20L, TimeUnit.MINUTES);
    TimeInterval defaultTime = new TimeInterval(10L, TimeUnit.SECONDS);
    assertEquals(TimeUnit.MINUTES.toSeconds(20L), TimeInterval.toSecondsDefaultIfNull(t, 0L));
    assertEquals(TimeUnit.MINUTES.toSeconds(20L),
        TimeInterval.toSecondsDefaultIfNull(t, defaultTime));
    assertEquals(10, TimeInterval.toSecondsDefaultIfNull(null, defaultTime));
    assertEquals(0, TimeInterval.toSecondsDefaultIfNull(null, 0));
  }
}

