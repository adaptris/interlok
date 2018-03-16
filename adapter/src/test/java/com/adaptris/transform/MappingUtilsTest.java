/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class MappingUtilsTest extends MappingUtils {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testPadValue() {
    assertEquals("  1", MappingUtils.padValue("1", 3, ' ', 'R'));
    assertEquals("  1", MappingUtils.padValue("1", 3, ' ', 'r'));
    assertEquals("1  ", MappingUtils.padValue("1", 3, ' ', 'l'));
    assertEquals("1  ", MappingUtils.padValue("1", 3, ' ', 'L'));
    assertEquals("1", MappingUtils.padValue("1", 3, ' ', 'B'));
  }

  @Test
  public void testLeftJust() {
    assertEquals("1  ", leftJust("1", 3));
  }

  @Test
  public void testRightJust() {
    assertEquals("  1", rightJust("1", 3));
  }

  @Test
  public void testGetSystemDate() {
    assertNotNull(getSystemDate());
  }

  @Test
  public void testGetSystemTime() {
    assertNotNull(getSystemTime());
  }

  @Test
  public void testGetSystemDateTime() {
    assertNotNull(getSystemDateTime());
  }

  @Test
  public void testGetMinValue() {
    assertEquals("1", getMinValue("1", "2"));
    assertEquals("1", getMinValue("2", "1"));
  }

  @Test
  public void testGetMaxValue() {
    assertEquals("2", getMaxValue("1", "2"));
    assertEquals("2", getMaxValue("2", "1"));
  }

  @Test
  public void testGetMinDate() {
    assertEquals("20011225 12:01", getMinDate("20011225 12:01", "20011226"));
    assertEquals("20011225 12:01", getMinDate("20011226", "20011225 12:01"));
    assertEquals("20011225", getMinDate("20011226 12:01", "20011225"));
    assertEquals("20011225", getMinDate("20011225", "20011226 12:01"));
  }

  @Test
  public void testGetMaxDate() {
    assertEquals("20011226", getMaxDate("20011225 12:01", "20011226"));
    assertEquals("20011226", getMaxDate("20011226", "20011225 12:01"));
    assertEquals("20011226 12:01", getMaxDate("20011225", "20011226 12:01"));
    assertEquals("20011226 12:01", getMaxDate("20011226 12:01", "20011225"));
  }

}
