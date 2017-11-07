/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CastorizedListTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testSize() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    assertEquals(0, castor.size());
  }

  @Test
  public void testIterator() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    assertNotNull(castor.iterator());
  }

  @Test
  public void testAdd() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    castor.add(new Object());
    assertEquals(1, list.size());
  }

  @Test
  public void testAddInt() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    castor.add(0, new Object());
    assertEquals(1, list.size());
    assertEquals(list.get(0), castor.get(0));
  }

  @Test
  public void testAddAllInt() {
    ArrayList list = new ArrayList();
    ArrayList toAdd = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    castor.addAll(0, toAdd);
    assertEquals(0, list.size());
  }

  @Test
  public void testGet() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    castor.add(0, new Object());
    assertEquals(1, list.size());
    assertEquals(list.get(0), castor.get(0));
  }

  @Test
  public void testIndexOf() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    castor.add(0, new Object());
    assertEquals(1, list.size());
    assertEquals(list.indexOf(new Object()), castor.indexOf(new Object()));
  }

  @Test
  public void testLastIndexOf() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    castor.add(0, new Object());
    assertEquals(1, list.size());
    assertEquals(list.lastIndexOf(new Object()), castor.lastIndexOf(new Object()));
  }

  @Test
  public void testListIterator() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    assertNotNull(castor.listIterator());
  }

  @Test
  public void testListIteratorInt() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    castor.add(new Object());
    assertNotNull(castor.listIterator(0));
  }

  @Test
  public void testRemoveInt() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    Object o = new Object();
    castor.add(o);
    assertEquals(o, castor.remove(0));
  }

  @Test
  public void testSet() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    Object o = new Object();
    castor.add(o);
    assertEquals(o, castor.set(0, new Object()));
  }

  @Test
  public void testSubList() {
    ArrayList list = new ArrayList();
    CastorizedList castor = new CastorizedList(list);
    Object o = new Object();
    castor.add(new Object());
    castor.add(new Object());
    assertNotNull(castor.subList(0, 1));
  }

}
