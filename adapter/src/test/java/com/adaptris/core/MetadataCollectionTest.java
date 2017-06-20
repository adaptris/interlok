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

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.util.KeyValuePairCollection;

public class MetadataCollectionTest {

  @Before
  public void setUp() {
  }

  @Test
  public void testContainsKey() {
    MetadataCollection c = new MetadataCollection();
    c.add(new MetadataElement("test", "value"));
    assertTrue(c.containsKey("test"));
    assertFalse(c.containsKey("value"));
  }

  @Test
  public void testConstructor_Set() {
    Set<MetadataElement> elements = new HashSet<>();
    elements.add(new MetadataElement("test", "value"));
    MetadataCollection c = new MetadataCollection(elements);
    assertTrue(c.containsKey("test"));    
  }

  @Test
  public void testConstructor_Collection() {
    MetadataCollection elements = new MetadataCollection();
    elements.add(new MetadataElement("test", "value"));
    MetadataCollection c = new MetadataCollection(elements);
    assertTrue(c.containsKey("test"));    
  }

  @Test
  public void testContructor_KVPS() {
    KeyValuePairCollection elements = new KeyValuePairCollection();
    elements.add(new MetadataElement("test", "value"));
    MetadataCollection c = new MetadataCollection(elements);
    assertTrue(c.containsKey("test"));
  }

  @Test
  public void testContructor_Map() {
    Map<String, String> elements = new HashMap<>();
    elements.put("test", "value");
    MetadataCollection c = new MetadataCollection(elements);
    assertTrue(c.containsKey("test"));
  }

  @Test
  public void testToSet() {
    Set<MetadataElement> elements = new HashSet<>();
    elements.add(new MetadataElement("test", "value"));
    MetadataCollection c = new MetadataCollection(elements);
    assertEquals(elements, c.toSet());
  }

}
