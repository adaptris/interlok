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

package com.adaptris.core.services.jdbc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.ListIterator;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.PlainIdGenerator;

public class StatementParameterListTest extends BaseCase {
  private IdGenerator id;


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    id = new PlainIdGenerator();
  }

  @Test
  public void testClear() throws Exception {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);
    assertEquals(2, list.size());
    list.clear();
    assertEquals(0, list.size());
    assertEquals(0, list.getParameters().size());
  }

  @Test
  public void testRemoveThenAddAgain() throws Exception {
    StatementParameterList list = new StatementParameterList();
    StringStatementParameter sp1 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    list.add(sp1);
    assertEquals(1, list.size());
    assertTrue(list.remove(sp1));
    assertEquals(0, list.size());
    list.add(sp1);
    assertEquals(1, list.size());
  }

  @Test
  public void testRemoveNull() throws Exception {
    StatementParameterList list = new StatementParameterList();
    assertFalse(list.remove(null));
  }

  @Test
  public void testRemovePlainObject() throws Exception {
    StatementParameterList list = new StatementParameterList();
    assertFalse(list.remove(new Object()));
  }

  @Test
  public void testRemoveByPosition() throws Exception {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);
    assertEquals(2, list.size());
    assertNotNull(list.remove(1));
    assertEquals(1, list.size());
  }

  @Test
  public void testSetParameters() {
    StatementParameterList list = new StatementParameterList();
    list.setParameters(Arrays.asList(new JdbcStatementParameter[]
    {
        new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null),
        new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null)
    }));
    assertEquals(2, list.size());
  }

  @Test
  public void testAddAll() {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);
    assertEquals(2, list.size());
    list.addAll(addEntries(new StatementParameterList(), 2));
    assertEquals(4, list.size());
  }

  @Test
  public void testAddAllAtPosition() {
    StatementParameterList list = new StatementParameterList();
    StringStatementParameter sp1 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    StringStatementParameter sp2 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    list.add(sp1);
    list.add(sp2);

    StatementParameterList list2 = addEntries(new StatementParameterList(), 2);
    assertEquals(2, list2.size());
    list2.addAll(1, list);
    assertTrue(list2.contains(sp1));
    assertTrue(list2.contains(sp2));
    assertEquals(4, list2.size());
    assertEquals(sp1, list2.get(1));
  }

  @Test
  public void testCollectionConstructor() {
    StatementParameterList list = new StatementParameterList();
    StringStatementParameter sp1 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    StringStatementParameter sp2 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    list.add(sp1);
    list.add(sp2);

    StatementParameterList list2 = new StatementParameterList(list);
    assertEquals(2, list2.size());
    assertTrue(list2.contains(sp1));
    assertTrue(list2.contains(sp2));
  }

  @Test
  public void testGetByName() {
    StatementParameterList list = new StatementParameterList();
    StringStatementParameter sp1 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, id.create(new Object()));
    StringStatementParameter sp2 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null,
        id.create(new Object()));
    list.add(sp1);
    list.add(sp2);
    assertEquals(sp2, list.getParameterByName(sp2.getName()));
  }

  @Test
  public void testAddAtPosition() {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);
    StringStatementParameter sp1 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);

    list.add(1, sp1);

    assertEquals(3, list.size());
    assertEquals(sp1, list.get(1));
  }

  @Test
  public void testGetByInt() {
    StatementParameterList list = new StatementParameterList();
    StringStatementParameter sp1 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    StringStatementParameter sp2 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    list.add(sp1);
    list.add(sp2);

    assertEquals(sp2, list.get(1));
  }

  @Test
  public void testSetByInt() {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);

    StringStatementParameter sp1 = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    list.set(1, sp1);
    assertEquals(sp1, list.get(1));
    assertEquals(2, list.size());
  }

  @Test
  public void testGetParameters() {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);
    assertNotNull(list.getParameters());
    assertEquals(2, list.getParameters().size());
  }

  @Test
  public void testIndexOf() {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);
    StringStatementParameter toAdd = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    list.add(toAdd);
    assertEquals(3, list.size());
    assertEquals(2, list.indexOf(toAdd));
  }

  @Test
  public void testIterator() {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);
    assertNotNull(list.iterator());
    assertTrue(list.iterator().hasNext());
  }

  @Test
  public void testLastIndexOf() {
    StatementParameterList list = addEntries(new StatementParameterList(), 2);
    StringStatementParameter toAdd = new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null);
    list.add(toAdd);
    assertEquals(2, list.lastIndexOf(toAdd));
  }

  @Test
  public void testListIterator() {
    StatementParameterList list = addEntries(new StatementParameterList(), 4);
    assertNotNull(list.listIterator());
    assertNotNull(list.listIterator(1));
  }

  @Test
  public void testListIterator_hasNextPrevious() {
    StatementParameterList list = addEntries(new StatementParameterList(), 5);
    int count = 0;
    for (ListIterator<JdbcStatementParameter> i = list.listIterator(); i.hasNext();) {
      switch (count) {
      case 0: {
        assertEquals(-1, i.previousIndex());
        assertFalse(i.hasPrevious());
        assertTrue(i.hasNext());
        break;
      }
      case 5: {
        assertEquals(5, i.nextIndex());
        assertTrue(i.hasPrevious());
        assertFalse(i.hasNext());
        break;
      }
      default: {
        assertEquals(count, i.nextIndex());
        assertNotNull(i.previous());
        assertNotNull(i.next());
        assertTrue(i.hasPrevious());
        assertTrue(i.hasNext());
      }
      }
      i.next();
      count++;
    }
  }

  @Test
  public void testSubList() {
    StatementParameterList list = addEntries(new StatementParameterList(), 4);
    assertNotNull(list.subList(0, 2));
    assertEquals(2, list.subList(0, 2).size());
  }

  private StatementParameterList addEntries(StatementParameterList list, int count) {
    for (int i = 0; i < count; i++) {
      list.add(new StringStatementParameter(id.create(new Object()), QueryType.constant, null, null));
    }
    return list;
  }
}
