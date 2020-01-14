/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adaptris.core.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TimeSlicePersistenceTest {

  private TimeSlicePersistence instance;

  @Before
  public void setUp() throws Exception {
    instance = TimeSlicePersistence.getInstance();
  }

  @After
  public void tearDown() throws Exception {
    instance.clear();
  }

  @Test
  public void testPutCacheNameDoesNotExist() {
    TimeSlice timeSlice = new TimeSlice(new Date().getTime(), 0);
    instance.updateCurrentTimeSlice("default", timeSlice);

    assertTrue(instance.getCacheNames().contains("default"));
  }

  @Test
  public void testPutMultipleCacheNames() {
    TimeSlice timeSlice = new TimeSlice(new Date().getTime(), 0);
    instance.updateCurrentTimeSlice("default", timeSlice);
    instance.updateCurrentTimeSlice("default2", timeSlice);
    instance.updateCurrentTimeSlice("default3", timeSlice);

    assertTrue(instance.getCacheNames().contains("default"));
    assertTrue(instance.getCacheNames().contains("default2"));
    assertTrue(instance.getCacheNames().contains("default3"));
  }

  @Test
  public void testGetWithMultipleCacheNames() {
    TimeSlice timeSlice1 = new TimeSlice(new Date().getTime(), 0);
    TimeSlice timeSlice2 = new TimeSlice(new Date().getTime(), 1);
    TimeSlice timeSlice3 = new TimeSlice(new Date().getTime(), 2);
    instance.updateCurrentTimeSlice("default", timeSlice1);
    instance.updateCurrentTimeSlice("default2", timeSlice2);
    instance.updateCurrentTimeSlice("default3", timeSlice3);

    assertEquals(1, instance.getCurrentTimeSlice("default2").getTotalMessageCount());
    assertEquals(2, instance.getCurrentTimeSlice("default3").getTotalMessageCount());
    assertEquals(0, instance.getCurrentTimeSlice("default").getTotalMessageCount());
  }

  @Test
  public void testUpdateCache() {
    TimeSlice timeSlice1 = new TimeSlice(new Date().getTime(), 0);
    instance.updateCurrentTimeSlice("default", timeSlice1);
    assertEquals(0, instance.getCurrentTimeSlice("default").getTotalMessageCount());

    timeSlice1.setTotalMessageCount(10);
    instance.updateCurrentTimeSlice("default", timeSlice1);
    assertEquals(10, instance.getCurrentTimeSlice("default").getTotalMessageCount());
  }

  @Test
  public void testUpdateWithMultipleCaches() {
    TimeSlice timeSlice1 = new TimeSlice(new Date().getTime(), 0);
    instance.updateCurrentTimeSlice("default", timeSlice1);
    assertEquals(0, instance.getCurrentTimeSlice("default").getTotalMessageCount());

    TimeSlice timeSlice2 = new TimeSlice(new Date().getTime(), 0);
    instance.updateCurrentTimeSlice("default2", timeSlice2);
    assertEquals(0, instance.getCurrentTimeSlice("default2").getTotalMessageCount());

    timeSlice1.setTotalMessageCount(10);
    instance.updateCurrentTimeSlice("default", timeSlice1);
    assertEquals(10, instance.getCurrentTimeSlice("default").getTotalMessageCount());

    timeSlice2.setTotalMessageCount(109);
    instance.updateCurrentTimeSlice("default2", timeSlice2);
    assertEquals(109, instance.getCurrentTimeSlice("default2").getTotalMessageCount());
  }
}
