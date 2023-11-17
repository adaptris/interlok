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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TimeSliceTest {

  private TimeSlice timeSlice;

  @BeforeEach
  public void setUp() throws Exception {
    timeSlice = new TimeSlice();
  }

  @Test
  public void testSetAndGetEndMillis() {
    timeSlice.setEndMillis(1000);
    assertEquals(1000, timeSlice.getEndMillis());
  }

  @Test
  public void testSetAndGetTotalMessageCount() {
    timeSlice.setTotalMessageCount(10);
    assertEquals(10, timeSlice.getTotalMessageCount());
  }

}
