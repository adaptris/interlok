/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RuleListTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGet() throws Exception {
    RuleList rules = new RuleList();
    Source source = new Source(new StringReader("hello"));
    rules.add(source, "hello");
    assertEquals(1, rules.size());

    assertEquals(source, rules.getKey(0));
    assertEquals("hello", rules.getValue(source));
    assertEquals("hello", rules.getValue(0));
    assertNull(rules.getValue(new Source()));
  }

  @Test
  public void testRemove() throws Exception {
    RuleList rules = new RuleList();
    Source source = new Source(new StringReader("hello"));
    rules.add(source, "hello");
    assertEquals(1, rules.size());
    assertTrue(rules.remove(source));
    assertFalse(rules.remove(null));
    rules.add(source, "hello");
    rules.remove(0);
    assertNull(rules.getValue(source));
  }

  @Test
  public void testRemoveAll() throws Exception {
    RuleList rules = new RuleList();
    Source source = new Source(new StringReader("hello"));
    rules.add(source, "hello");
    rules.removeAll();
    assertTrue(rules.isEmpty());
    assertEquals(0, rules.size());
  }

}
