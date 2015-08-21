/*
 * $Id: TestKeyValuePairSet.java,v 1.1 2003/10/23 23:04:52 hfraser Exp $
 */
package com.adaptris.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class TestKeyValuePairList extends KeyValuePairCollectionCase {

  public TestKeyValuePairList(java.lang.String testName) {
    super(testName);
  }

  public void testAddTheSame() {
    KeyValuePairList set = createBag();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, ""));
    assertEquals(3, set.size());
  }

  public void testCollectionConstructor() {
    KeyValuePairList set = new KeyValuePairList();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY2, VALUE2));
    set.addKeyValuePair(new KeyValuePair(KEY3, VALUE3));
    KeyValuePairList set2 = new KeyValuePairList(set);
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  public void testPropertiesConstructor() {
    Properties p = new Properties();
    p.setProperty(KEY1, VALUE1);
    p.setProperty(KEY2, VALUE2);
    KeyValuePairList set = new KeyValuePairList(p);
    KeyValuePairList set2 = new KeyValuePairList(p);
    assertEquals(2, set.size());
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  public void testMapConstructor() {
    Map<String, String> p = new HashMap<>();
    p.put(KEY1, VALUE1);
    p.put(KEY2, VALUE2);
    KeyValuePairList set = new KeyValuePairList(p);
    KeyValuePairList set2 = new KeyValuePairList(p);
    assertEquals(2, set.size());
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  public void testAddAtIndex() {
    KeyValuePairList list = createBag();
    list.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    list.addKeyValuePair(new KeyValuePair(KEY3, VALUE3));
    list.addKeyValuePair(new KeyValuePair(KEY4, VALUE4));
    list.add(1, new KeyValuePair(KEY2, VALUE2));
    assertTrue(list.contains(new KeyValuePair(KEY2, "")));
  }

  public void testGetIndex() {
    KeyValuePairList list = createWithEntries();

    KeyValuePair kvp = list.get(1);
    assertEquals(new KeyValuePair(KEY2, VALUE2), kvp);
  }

  public void testIndexOf() {
    KeyValuePairList list = createWithEntries();

    assertEquals(1, list.indexOf(new KeyValuePair(KEY2, VALUE2)));
  }

  public void testLastIndexOf() {
    KeyValuePairList set = createBag();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, ""));
    assertEquals(2, set.lastIndexOf(new KeyValuePair(KEY1, "")));
  }

  public void testListIterator() {
    KeyValuePairList list = createWithEntries();

    assertNotNull(list.listIterator());
    assertNotNull(list.listIterator(1));
  }

  public void testSubList() {
    KeyValuePairList list = createWithEntries();
    assertNotNull(list.subList(0, 2));
    assertEquals(2, list.subList(0, 2).size());
    assertTrue(list.subList(0, 2).contains(new KeyValuePair(KEY2, VALUE2)));
  }

  public void testAddAllAtIndex() {
    KeyValuePairList list = createWithEntries();
    KeyValuePairList list2 = createWithEntries();
    list.addAll(1, list2);
    assertEquals(8, list.size());
  }

  public void testSetAtIndex() {
    KeyValuePairList list = createWithEntries();
    list.set(1, new KeyValuePair(KEY3, VALUE3));
    assertEquals(4, list.size());
    assertFalse(list.contains(new KeyValuePair(KEY2, "")));
  }

  public void testRemoveByIntPosition() {
    KeyValuePairList list = createWithEntries();
    list.remove(1);
    assertFalse(list.contains(new KeyValuePair(KEY2, "")));
    assertEquals(3, list.size());
  }

  @Override
  protected KeyValuePairList createBag() {
    return new KeyValuePairList();
  }

  private KeyValuePairList createWithEntries() {
    return new KeyValuePairList(Arrays.asList(new KeyValuePair[]
    {
        new KeyValuePair(KEY1, VALUE1), new KeyValuePair(KEY2, VALUE2), new KeyValuePair(KEY3, VALUE3),
        new KeyValuePair(KEY4, VALUE4)
    }));
  }
}