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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;


public class TestKeyValuePairCollection extends KeyValuePairCollectionCase {


  @Test
  public void testAddTheSame() {
    KeyValuePairBag set = createBag();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY1, ""));
    assertEquals(3, set.size());
  }

  @Test
  public void testCollectionConstructor() {
    KeyValuePairCollection set = new KeyValuePairCollection();
    set.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    set.addKeyValuePair(new KeyValuePair(KEY2, VALUE2));
    set.addKeyValuePair(new KeyValuePair("key3", "value3"));
    KeyValuePairCollection set2 = new KeyValuePairCollection(set);
    assertEquals(set, set2);
    assertEquals(set.hashCode(), set2.hashCode());
  }

  @Test
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

  @Test
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
