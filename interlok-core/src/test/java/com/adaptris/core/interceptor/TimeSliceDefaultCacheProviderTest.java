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
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Test;

public class TimeSliceDefaultCacheProviderTest {

  private TimeSliceDefaultCacheProvider setUpProvider() throws Exception {
    TimeSliceDefaultCacheProvider cacheProvider = new TimeSliceDefaultCacheProvider();
    cacheProvider.init();
    cacheProvider.start();
    return cacheProvider;
  }

  private void tearDownProvider(TimeSliceDefaultCacheProvider cacheProvider) throws Exception {
    cacheProvider.stop();
  }

  @Test
  public void testPutCacheNameDoesNotExist() throws Exception {
    TimeSliceDefaultCacheProvider cacheProvider = setUpProvider();

    TimeSlice timeSlice = new TimeSlice(new Date().getTime() + 5000, 0);
    cacheProvider.update("default", timeSlice);

    assertNotNull(cacheProvider.get("default"));

    tearDownProvider(cacheProvider);
  }

  @Test
  public void testPutMultipleCacheNames() throws Exception {
    TimeSliceDefaultCacheProvider cacheProvider = setUpProvider();

    TimeSlice timeSlice = new TimeSlice(new Date().getTime() + 5000, 0);
    cacheProvider.update("default", timeSlice);
    cacheProvider.update("default2", timeSlice);
    cacheProvider.update("default3", timeSlice);

    assertNotNull(cacheProvider.get("default"));
    assertNotNull(cacheProvider.get("default2"));
    assertNotNull(cacheProvider.get("default3"));

    tearDownProvider(cacheProvider);
  }

  @Test
  public void testGetWithMultipleCacheNames() throws Exception {
    TimeSliceDefaultCacheProvider cacheProvider = setUpProvider();

    TimeSlice timeSlice1 = new TimeSlice(new Date().getTime() + 5000, 0);
    TimeSlice timeSlice2 = new TimeSlice(new Date().getTime() + 5000, 1);
    TimeSlice timeSlice3 = new TimeSlice(new Date().getTime() + 5000, 2);

    cacheProvider.update("default4", timeSlice1);
    cacheProvider.update("default5", timeSlice2);
    cacheProvider.update("default6", timeSlice3);

    assertEquals(1, cacheProvider.get("default5").getTotalMessageCount());
    assertEquals(2, cacheProvider.get("default6").getTotalMessageCount());
    assertEquals(0, cacheProvider.get("default4").getTotalMessageCount());

    tearDownProvider(cacheProvider);
  }

  @Test
  public void testUpdateCache() throws Exception {
    TimeSliceDefaultCacheProvider cacheProvider = setUpProvider();

    TimeSlice timeSlice1 = new TimeSlice(new Date().getTime() + 5000, 0);
    cacheProvider.update("default7", timeSlice1);
    assertEquals(0, cacheProvider.get("default7").getTotalMessageCount());

    timeSlice1.setTotalMessageCount(10);
    cacheProvider.update("default7", timeSlice1);
    assertEquals(10, cacheProvider.get("default7").getTotalMessageCount());

    tearDownProvider(cacheProvider);
  }

  @Test
  public void testUpdateWithMultipleCaches() throws Exception {
    TimeSliceDefaultCacheProvider cacheProvider = setUpProvider();

    TimeSlice timeSlice1 = new TimeSlice(new Date().getTime() + 5000, 0);
    cacheProvider.update("default8", timeSlice1);
    assertEquals(0, cacheProvider.get("default8").getTotalMessageCount());

    TimeSlice timeSlice2 = new TimeSlice(new Date().getTime() + 5000, 0);
    cacheProvider.update("default9", timeSlice2);
    assertEquals(0, cacheProvider.get("default9").getTotalMessageCount());

    timeSlice1.setTotalMessageCount(10);
    cacheProvider.update("default10", timeSlice1);
    assertEquals(10, cacheProvider.get("default10").getTotalMessageCount());

    timeSlice2.setTotalMessageCount(109);
    cacheProvider.update("default11", timeSlice2);
    assertEquals(109, cacheProvider.get("default11").getTotalMessageCount());

    tearDownProvider(cacheProvider);
  }

}
