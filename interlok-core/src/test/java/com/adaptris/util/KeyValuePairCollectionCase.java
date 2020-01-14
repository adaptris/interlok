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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;

public abstract class KeyValuePairCollectionCase {

  protected static final String KEY = "key";
  protected static final String VALUE = "value";

  protected static final String KEY1 = "key1";
  protected static final String KEY2 = "key2";
  protected static final String KEY3 = "key3";
  protected static final String KEY4 = "key4";

  protected static final String VALUE1 = "value1";
  protected static final String VALUE2 = "value2";
  protected static final String VALUE3 = "value3";
  protected static final String VALUE4 = "value4";


  protected abstract KeyValuePairBag createBag();

  @Test
  public void testGetKeyValuePair() {
    KeyValuePairBag set = createWithEntries(3);

    assertTrue(set.contains(new KeyValuePair(KEY1, VALUE1)));
    assertTrue(new KeyValuePair(KEY1, VALUE1).equals(set.getKeyValuePair(KEY1)));
  }

  @Test
  public void testGetValue() {
    KeyValuePairBag set = createWithEntries(3);

    assertTrue(VALUE1.equals(set.getValue(KEY1)));
  }

  @Test
  public void testGetValueIgnoringKeyCase() {
    KeyValuePairBag set = createWithEntries(3);

    assertEquals(VALUE1, set.getValueIgnoringKeyCase("KEY1"));
    set.addKeyValuePair(new KeyValuePair("Key1", "Value1"));
    assertEquals(4, set.size());
  }

  @Test
  public void testNullOperations() {
    KeyValuePairBag set = createWithEntries(3);

    try {
      set.addKeyValuePair(null);
      fail("Cannot not add null to a key value pair set");
    }
    catch (IllegalArgumentException e) {
      ;// expected
    }
    assertNull(set.getValueIgnoringKeyCase(null));
    set.removeKeyValuePair((KeyValuePair) null);
    assertEquals(3, set.size());
  }

  @Test
  public void testRemoveWithKeyValuePair() {
    KeyValuePairBag set = createWithEntries(3);

    set.removeKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    assertEquals(2, set.size());
  }

  @Test
  public void testRemoveByKey() {
    KeyValuePairBag set = createWithEntries(3);

    set.removeKeyValuePair(KEY1);
    assertEquals(2, set.size());
    assertNull(set.getValue(KEY1));
  }

  @Test
  public void testGetNonExistent() {
    KeyValuePairBag set = createWithEntries(3);

    assertNull(set.getKeyValuePair("ABCDEFG"));
    assertNull(set.getValue("ABCDEFG"));
    assertNull(set.getValueIgnoringKeyCase("ABCDEFG"));
  }

  @Test
  public void testIterator() {
    KeyValuePairBag set = createWithEntries(3);

    assertNotNull(set.toString());
    assertNotNull(set.iterator());
    int count = 0;
    for (Iterator<KeyValuePair> i = set.iterator(); i.hasNext();) {
      KeyValuePair kvp = i.next();
      count++;
    }
    assertEquals(3, count);
  }

  @Test
  public void testGetKeyValuePairs() {
    KeyValuePairBag set = createWithEntries(3);
    Collection<KeyValuePair> col = set.getKeyValuePairs();
    assertNotNull(col);
    assertEquals(3, col.size());
  }

  @Test
  public void testSetKeyValuePairs() {
    KeyValuePairBag set = createBag();
    Collection<KeyValuePair> col = Arrays.asList(new KeyValuePair[]
    {
        new KeyValuePair(KEY1, VALUE1), new KeyValuePair(KEY2, VALUE2), new KeyValuePair(KEY3, VALUE3)
    });
    set.setKeyValuePairs(col);
    assertEquals(3, set.size());
    assertEquals(3, set.getKeyValuePairs().size());
    assertTrue(set.contains(new KeyValuePair(KEY1, VALUE1)));
    assertTrue(new KeyValuePair(KEY1, VALUE1).equals(set.getKeyValuePair(KEY1)));
  }

  @Test
  public void testAsProperties() {
    KeyValuePairBag set = createWithEntries(3);

    assertNotNull(set.toString());
    Properties p = KeyValuePairSet.asProperties(set);
    assertTrue(p.containsKey(KEY1));
    assertEquals(VALUE1, p.getProperty(KEY1));
    assertEquals(set.toString(), p.toString());
  }

  @Test
  public void testEquals() {
    KeyValuePairBag set = createWithEntries(3);
    KeyValuePairBag set2 = createWithEntries(3);
    assertTrue(set.equals(set));
    assertTrue(set.equals(set2));
    assertEquals(set.hashCode(), set2.hashCode());
    set2.addKeyValuePair(new KeyValuePair(KEY4, VALUE4));
    assertFalse(set.equals(set2));
    assertFalse(set.equals(new KeyValuePairSet()));
    assertFalse(set.equals(new KeyValuePairCollection()));
    assertFalse(set.equals(new Object()));
    assertFalse(set.equals(null));
    KeyValuePairBag set3 = createBag();
    set3.add(new KeyValuePair(KEY4, VALUE4));
    set3.add(new KeyValuePair(KEY3, VALUE3));
    set3.add(new KeyValuePair(KEY2, VALUE2));
    assertFalse(set.equals(set3));
  }

  @Test
  public void testSize() {
    KeyValuePairBag bag1 = createWithEntries(3);
    assertEquals(3, bag1.size());
  }

  @Test
  public void testClear() {
    KeyValuePairBag bag1 = createWithEntries(3);
    bag1.clear();
    assertEquals(0, bag1.size());
  }

  @Test
  public void testAddAll() {
    KeyValuePairBag bag1 = createWithEntries(3);
    KeyValuePairBag bag2 = createBag();
    bag2.add(new KeyValuePair(KEY4, VALUE4));
    bag1.addAll(bag2);
    assertEquals(4, bag1.size());
  }

  @Test
  public void testAddAll_Properties() {
    Properties p = KeyValuePairBag.asProperties(createWithEntries(3));
    KeyValuePairBag bag2 = createBag();
    bag2.add(new KeyValuePair(KEY4, VALUE4));
    bag2.addAll(p);
    assertEquals(4, bag2.size());
  }

  @Test
  public void testAddAll_Map() {
    Map<String, String> p = toMap(createWithEntries(3));
    KeyValuePairBag bag2 = createBag();
    bag2.add(new KeyValuePair(KEY4, VALUE4));
    bag2.addAll(p);
    assertEquals(4, bag2.size());
  }

  @Test
  public void testContains() {
    KeyValuePairBag bag1 = createWithEntries(5);
    assertTrue(bag1.contains(new KeyValuePair(KEY1, VALUE1)));
  }

  @Test
  public void testContainsAll() {
    KeyValuePairBag bag1 = createWithEntries(5);
    KeyValuePairBag bag2 = createBag();
    bag2.add(new KeyValuePair(KEY1, VALUE1));
    bag2.add(new KeyValuePair(KEY2, VALUE2));
    assertTrue(bag1.containsAll(bag2));
  }

  @Test
  public void testIsEmpty() {
    KeyValuePairBag bag1 = createWithEntries(4);
    bag1.clear();
    assertTrue(bag1.isEmpty());
  }

  @Test
  public void testRemove() {
    KeyValuePairBag bag1 = createWithEntries(3);
    bag1.remove(new KeyValuePair(KEY1, VALUE1));
    assertEquals(2, bag1.size());
  }

  @Test
  public void testRemoveAllSmaller() {
    KeyValuePairBag bag1 = createWithEntries(3);
    bag1.add(new KeyValuePair(KEY + 10, VALUE + 10));
    KeyValuePairBag bag2 = createWithEntries(3);
    bag1.removeAll(bag2);
    assertEquals(1, bag1.size());
    assertEquals(3, bag2.size());
  }

  @Test
  public void testRemoveAllLarger() {
    KeyValuePairBag bag1 = createWithEntries(3);
    bag1.add(new KeyValuePair(KEY + 10, VALUE + 10));
    KeyValuePairBag bag2 = createWithEntries(5);
    bag1.removeAll(bag2);
    assertEquals(1, bag1.size());
    assertEquals(5, bag2.size());
  }

  @Test
  public void testRetainAll() {
    KeyValuePairBag bag1 = createWithEntries(3);
    KeyValuePairBag bag2 = createBag();
    bag2.add(new KeyValuePair(KEY1, VALUE2));
    bag2.add(new KeyValuePair(KEY2, VALUE2));
    bag1.retainAll(bag2);
    assertEquals(2, bag1.size());
  }

  @Test
  public void testToArray() {
    KeyValuePairBag bag1 = createWithEntries(3);
    assertEquals(3, bag1.toArray().length);
  }

  @Test
  public void testToArrayWithRuntimeType() {
    KeyValuePairBag bag1 = createWithEntries(3);
    try {
      bag1.toArray(new String[0]);
      fail();
    }
    catch (ArrayStoreException expected) {
    }
    KeyValuePair[] pairs = bag1.toArray(new KeyValuePair[0]);
    assertEquals(3, pairs.length);
  }

  protected KeyValuePairBag createWithEntries(int count) {
    KeyValuePairBag set = createBag();
    for (int i = 0; i < count; i++) {
      set.addKeyValuePair(new KeyValuePair(KEY + i, VALUE + i));
    }
    return set;
  }
  
  protected Map<String, String> toMap(KeyValuePairBag bag) {
    HashMap<String, String> result = new HashMap<>();
    for (KeyValuePair kvp : bag) {
      result.put(kvp.getKey(), kvp.getValue());
    }
    return result;
  }
}
