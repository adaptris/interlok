/*
 * $Id: TestKeyValuePair.java,v 1.1 2003/10/23 23:04:52 hfraser Exp $
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