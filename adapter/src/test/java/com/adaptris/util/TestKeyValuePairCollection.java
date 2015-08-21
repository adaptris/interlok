/*
 * $Id: TestKeyValuePairSet.java,v 1.1 2003/10/23 23:04:52 hfraser Exp $
 */
package com.adaptris.util;

import java.util.Properties;


public class TestKeyValuePairCollection extends KeyValuePairCollectionCase {

  public TestKeyValuePairCollection(java.lang.String testName) {
    super(testName);
  }

  public void testAddTheSame() {
    KeyValuePairBag set = createBag();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, ""));
    assertEquals(3, set.size());
  }

  public void testCollectionConstructor() {
    KeyValuePairCollection set = new KeyValuePairCollection();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY2, VALUE2));
    set.addKeyValuePair(new KeyValuePair("key3", "value3"));
    KeyValuePairCollection set2 = new KeyValuePairCollection(set);
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  public void testPropertiesConstructor() {
    Properties p = new Properties();
    p.setProperty(KEY1, VALUE1);
    p.setProperty(KEY2, VALUE2);
    KeyValuePairCollection set = new KeyValuePairCollection(p);
    KeyValuePairCollection set2 = new KeyValuePairCollection(p);
    assertEquals(2, set.size());
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }


  @Override
  protected KeyValuePairBag createBag() {
    return new KeyValuePairCollection();
  }

}