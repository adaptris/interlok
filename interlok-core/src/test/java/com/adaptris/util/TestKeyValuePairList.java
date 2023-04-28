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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;


public class TestKeyValuePairList extends KeyValuePairCollectionCase {


  @Test
  public void testAddTheSame() {
    KeyValuePairList set = createBag();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, ""));
    assertEquals(3, set.size());
  }

  @Test
  public void testCollectionConstructor() {
    KeyValuePairList set = new KeyValuePairList();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY2, VALUE2));
    set.addKeyValuePair(new KeyValuePair(KEY3, VALUE3));
    KeyValuePairList set2 = new KeyValuePairList(set);
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  @Test
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

  @Test
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

  @Test
  public void testAddAtIndex() {
    KeyValuePairList list = createBag();
    list.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    list.addKeyValuePair(new KeyValuePair(KEY3, VALUE3));
    list.addKeyValuePair(new KeyValuePair(KEY4, VALUE4));
    list.add(1, new KeyValuePair(KEY2, VALUE2));
    assertTrue(list.contains(new KeyValuePair(KEY2, "")));
  }

  @Test
  public void testGetIndex() {
    KeyValuePairList list = createWithEntries();

    KeyValuePair kvp = list.get(1);
    assertEquals(new KeyValuePair(KEY2, VALUE2), kvp);
  }

  @Test
  public void testIndexOf() {
    KeyValuePairList list = createWithEntries();

    assertEquals(1, list.indexOf(new KeyValuePair(KEY2, VALUE2)));
  }

  @Test
  public void testLastIndexOf() {
    KeyValuePairList set = createBag();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, ""));
    assertEquals(2, set.lastIndexOf(new KeyValuePair(KEY1, "")));
  }

  @Test
  public void testListIterator() {
    KeyValuePairList list = createWithEntries();

    assertNotNull(list.listIterator());
    assertNotNull(list.listIterator(1));
  }

  @Test
  public void testSubList() {
    KeyValuePairList list = createWithEntries();
    assertNotNull(list.subList(0, 2));
    assertEquals(2, list.subList(0, 2).size());
    assertTrue(list.subList(0, 2).contains(new KeyValuePair(KEY2, VALUE2)));
  }

  @Test
  public void testAddAllAtIndex() {
    KeyValuePairList list = createWithEntries();
    KeyValuePairList list2 = createWithEntries();
    list.addAll(1, list2);
    assertEquals(8, list.size());
  }

  @Test
  public void testSetAtIndex() {
    KeyValuePairList list = createWithEntries();
    list.set(1, new KeyValuePair(KEY3, VALUE3));
    assertEquals(4, list.size());
    assertFalse(list.contains(new KeyValuePair(KEY2, "")));
  }

  @Test
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
