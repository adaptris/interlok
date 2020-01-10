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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;

public class TestKeyValuePairSet extends KeyValuePairCollectionCase {

  @Test
  public void testAddTheSame() {
    KeyValuePairSet set = new KeyValuePairSet();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, ""));
    assertEquals(1, set.size());
  }

  @Test
  public void testSetEquality() {
    KeyValuePairSet bag1 = new KeyValuePairSet();
    bag1.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    KeyValuePairCollection bag2 = new KeyValuePairCollection();
    bag2.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    // Same size, same entries, but it's not a set
    assertFalse(bag1.equals(bag2));
  }

  @Test
  public void testCollectionConstructor() {
    KeyValuePairSet set = new KeyValuePairSet();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY2, VALUE2));
    set.addKeyValuePair(new KeyValuePair("key3", "value3"));
    KeyValuePairSet set2 = new KeyValuePairSet(set);
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  @Test
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

  @Test
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
