/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PropertyHelperTest extends PropertyHelper {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetPropertySubsetPropertiesString() {
    Properties p = getPropertySubset(createTestSample(), "a");
    assertEquals(2, p.size());
  }

  @Test
  public void testGetPropertySubsetPropertiesStringBoolean() {
    Properties p = getPropertySubset(createTestSample(), "A", true);
    assertEquals(2, p.size());
  }

  @Test
  public void testGetPropertyIgnoringCasePropertiesStringString() {
    assertEquals("a.value", getPropertyIgnoringCase(createTestSample(), "A.KEY", "defaultValue"));
    assertEquals("defaultValue", getPropertyIgnoringCase(createTestSample(), "BLAH", "defaultValue"));
  }

  @Test
  public void testAsMap() {
    Map<String, String> map = asMap(createTestSample());
    assertEquals(4, map.size());
    assertEquals("a.value", map.get("a.key"));
    map = asMap(null);
    assertNotNull(map);
    assertEquals(0, map.size());
  }

  @Test
  public void testGetPropertyIgnoringCasePropertiesString() {
    assertEquals("a.value", getPropertyIgnoringCase(createTestSample(), "A.KEY"));
    assertNull(getPropertyIgnoringCase(createTestSample(), "BLAH"));
  }

  private Properties createTestSample() {
    Properties result = new Properties();
    result.setProperty("a.key", "a.value");
    result.setProperty("a.anotherKey", "a.anotherKey");
    result.setProperty("b.key", "b.value");
    result.setProperty("b.anotherKey", "b.anotherKey");
    return result;
  }
}
