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

import junit.framework.TestCase;


public class TestKeyValuePair extends TestCase {

  public TestKeyValuePair(java.lang.String testName) {
    super(testName);
  }

  KeyValuePair pair1;
  KeyValuePair pair2;


  @Override
  public void setUp() {
    pair1 = new KeyValuePair("key", "value");
    pair2 = new KeyValuePair("key", "value");
  }

  @Override
  public void tearDown() {
  }

  public void testEquals() {
    assertEquals(pair1, pair2);
    assertEquals(pair2, pair1);
    pair1.setValue("value2");

    // equality is based on key only
    assertEquals(pair1, pair2);
    assertEquals(pair2, pair1);

    pair1.setKey("key2");

    assertNotSame(pair1, pair2);
    assertNotSame(pair2, pair1);
    assertFalse(pair1.equals(new Object()));
  }

  public void testToString() {
    assertNotNull(pair1.toString());
    assertNotNull(pair2.toString());
    assertNotNull(new KeyValuePair().toString());
  }

  public void testSets() {
    try {
      pair1.setKey(null);
      fail("null key doesn't throw Exc.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    try {
      pair1.setValue(null);
      fail("null value doesn't throw Exc.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }
  }
}
