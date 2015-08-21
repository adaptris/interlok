/*
 * $Id: TestKeyValuePairSet.java,v 1.1 2003/10/23 23:04:52 hfraser Exp $
 */
package com.adaptris.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class TestKeyValuePairSet extends KeyValuePairCollectionCase {

  public TestKeyValuePairSet(java.lang.String testName) {
    super(testName);
  }

  public void testAddTheSame() {
    KeyValuePairSet set = new KeyValuePairSet();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, ""));
    assertEquals(1, set.size());
  }

  public void testSetEquality() {
    KeyValuePairSet bag1 = new KeyValuePairSet();
    bag1.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    KeyValuePairCollection bag2 = new KeyValuePairCollection();
    bag2.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    // Same size, same entries, but it's not a set
    assertFalse(bag1.equals(bag2));
  }

  public void testCollectionConstructor() {
    KeyValuePairSet set = new KeyValuePairSet();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY2, VALUE2));
    set.addKeyValuePair(new KeyValuePair("key3", "value3"));
    KeyValuePairSet set2 = new KeyValuePairSet(set);
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  public void testPropertiesConstructor() {
    Properties p = new Properties();
    p.setProperty(KEY1, VALUE1);
    p.setProperty(KEY2, VALUE2);
    KeyValuePairSet set = new KeyValuePairSet(p);
    KeyValuePairSet set2 = new KeyValuePairSet(p);
    assertEquals(2, set.size());
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  public void testMapConstructor() {
    Map<String, String> p = new HashMap<>();
    p.put(KEY1, VALUE1);
    p.put(KEY2, VALUE2);
    KeyValuePairSet set = new KeyValuePairSet(p);
    KeyValuePairSet set2 = new KeyValuePairSet(p);
    assertEquals(2, set.size());
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }


  @Override
  protected KeyValuePairBag createBag() {
    return new KeyValuePairSet();
  }

}