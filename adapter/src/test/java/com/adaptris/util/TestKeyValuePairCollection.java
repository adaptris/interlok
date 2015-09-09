package com.adaptris.util;

import java.util.HashMap;
import java.util.Map;
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

  public void testMapConstructor() {
    Map<String, String> p = new HashMap<>();
    p.put(KEY1, VALUE1);
    p.put(KEY2, VALUE2);
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