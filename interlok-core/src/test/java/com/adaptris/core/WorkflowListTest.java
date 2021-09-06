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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.PlainIdGenerator;

public class WorkflowListTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  private IdGenerator idGenerator;

  @Before
  public void setUp() throws Exception {
    idGenerator = new PlainIdGenerator();
  }

  @Test
  public void testRemoveThenAddAgain() throws Exception {
    WorkflowList list = new WorkflowList();
    Workflow workflow1 = createWorkflow(getName());
    list.add(workflow1);
    assertEquals(1, list.size());
    assertTrue(list.removeWorkflow(workflow1));
    assertEquals(0, list.size());
    list.add(workflow1);
    assertEquals(1, list.size());
  }

  @Test
  public void testRemoveByObject() throws Exception {
    WorkflowList list = new WorkflowList();
    Workflow workflow1 = createWorkflow(getName());
    list.add(workflow1);
    assertEquals(1, list.size());
    assertTrue(list.removeWorkflow(workflow1));
    assertEquals(0, list.size());
  }

  @Test
  public void testRedmine5407() throws Exception {
    testRemoveThenAddAgain();
  }

  @Test
  public void testRemoveNull() throws Exception {
    WorkflowList list = new WorkflowList();
    assertFalse(list.remove(null));
    assertFalse(list.removeWorkflow(null));
  }

  @Test
  public void testRemovePlainObject() throws Exception {
    WorkflowList list = new WorkflowList();
    assertFalse(list.remove(new Object()));

  }

  @Test
  public void testSetWorkflows() {
    WorkflowList list = new WorkflowList();
    try {
      list.setWorkflows(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    list.setWorkflows(Arrays.asList(new Workflow[]
    {
      createWorkflow(idGenerator.create(list))
    }));
    assertEquals(1, list.size());
  }

  @Test
  public void testSetWorkflowsWithDuplicateID() {
    WorkflowList list = new WorkflowList();
    String id = idGenerator.create(this);
    try {
    list.setWorkflows(Arrays.asList(new Workflow[]
    {
          createWorkflow(id), createWorkflow(id)
    }));
    fail();
    } catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testAddAllWorkflowsWithDuplicateID() {
    WorkflowList list = new WorkflowList();
    String id = idGenerator.create(this);
    try {
      list.addAll(Arrays.asList(new Workflow[]
      {
          createWorkflow(id), createWorkflow(id)
      }));
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testAddAllAtPosition() {
    WorkflowList list = new WorkflowList();
    Workflow workflow1 = createWorkflow(idGenerator.create(list));
    Workflow workflow2 = createWorkflow(idGenerator.create(list));
    list.add(workflow1);
    list.add(workflow2);

    WorkflowList list2 = new WorkflowList();
    list2.add(createWorkflow(idGenerator.create(list)));
    list2.add(createWorkflow(idGenerator.create(list)));
    assertEquals(2, list2.size());
    list2.addAll(1, list);
    assertTrue(list2.contains(workflow1));
    assertTrue(list2.contains(workflow1));
    assertEquals(4, list2.size());
    assertEquals(workflow1, list2.get(1));
  }

  @Test
  public void testAddDuplicate() {
    WorkflowList list = new WorkflowList();
    StandardWorkflow toAdd = createWorkflow(idGenerator.create(list));
    toAdd.setUniqueId(null);
    list.add(toAdd);
    list.add(toAdd);
    assertEquals(2, list.size());
  }

  @Test
  public void testCollectionConstructor() {
    WorkflowList list = new WorkflowList();
    Workflow workflow1 = createWorkflow(idGenerator.create(list));
    Workflow workflow2 = createWorkflow(idGenerator.create(list));
    list.add(workflow1);
    list.add(workflow2);
    WorkflowList list2 = new WorkflowList(list);
    assertEquals(2, list2.size());
    assertTrue(list2.contains(workflow1));
    assertTrue(list2.contains(workflow1));
  }

  @Test
  public void testAdd() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    assertEquals(1, list.size());
    try {
      list.add(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(1, list.size());
  }

  @Test
  public void testAddAtPosition() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    Workflow toAdd = createWorkflow(idGenerator.create(list));
    list.add(1, toAdd);

    assertEquals(3, list.size());
    assertEquals(toAdd, list.get(1));
    try {
      list.add(1, null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(3, list.size());
    assertEquals(toAdd, list.get(1));
  }

  @Test
  public void testSetLifecycleStrategy() throws Exception {
    WorkflowList list = new WorkflowList();
    assertNull(list.getLifecycleStrategy());
    DefaultWorkflowLifecycleStrategy dwls = new DefaultWorkflowLifecycleStrategy();
    list.setLifecycleStrategy(dwls);
    assertEquals(dwls, list.getLifecycleStrategy());
    list.setLifecycleStrategy(null);
    assertNull(list.getLifecycleStrategy());
  }

  @Test
  public void testGetByInt() {
    WorkflowList list = new WorkflowList();
    Workflow toAdd = createWorkflow(idGenerator.create(list));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(toAdd);
    assertEquals(toAdd, list.get(1));
  }

  @Test
  public void testSetByInt() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow("replacedWorkflow"));
    Workflow toAdd = createWorkflow(idGenerator.create(list));
    list.add(createWorkflow(idGenerator.create(list)));
    list.set(1, toAdd);
    assertEquals(toAdd, list.get(1));
    assertNull(list.getWorkflow("replacedWorkflow"));
  }

  @Test
  public void testGetWorkflows() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    assertNotNull(list.getWorkflows());
    assertEquals(1, list.getWorkflows().size());
  }

  @Test
  public void testIndexOf() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    Workflow toAdd = createWorkflow(idGenerator.create(list));
    list.add(toAdd);
    assertEquals(3, list.size());
    assertEquals(2, list.indexOf(toAdd));
  }

  @Test
  public void testIterator() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    assertNotNull(list.iterator());
    assertTrue(list.iterator().hasNext());
  }

  @Test
  public void testLastIndexOf() {
    WorkflowList list = new WorkflowList();
    Workflow toAdd = createWorkflow(idGenerator.create(list));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(toAdd);
    assertEquals(2, list.lastIndexOf(toAdd));
  }

  @Test
  public void testListIterator() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    assertNotNull(list.listIterator());
    assertNotNull(list.listIterator(1));
  }

  @Test
  public void testListIterator_hasNextPrevious() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    int count = 0;
    for (ListIterator<Workflow> i = list.listIterator(); i.hasNext();) {
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
  public void testRemove() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    Workflow workflow = createWorkflow("Workflow1");
    list.add(workflow);
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(null));
    assertEquals(5, list.size());
    list.remove(4); // Remove the workflow w/o a unique-id
    assertEquals(4, list.size());
    list.remove(2);
    assertFalse(list.contains(workflow));
    assertEquals(3, list.size());
    assertNull(list.getWorkflow(workflow.getUniqueId()));
  }

  @Test
  public void testSubList() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    assertNotNull(list.subList(0, 2));
    assertEquals(2, list.subList(0, 2).size());
  }

  @Test
  public void testGetWorkflowById() throws Exception {
    WorkflowList list = new WorkflowList();
    StandardWorkflow wf1 = createWorkflow("wf1");
    StandardWorkflow wf2 = createWorkflow("wf2");
    list.add(wf1);
    list.add(wf2);
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    assertEquals(wf1, list.getWorkflow("wf1"));
    try {
      list.getWorkflow("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      list.getWorkflow((String) null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertNull(list.getWorkflow("WF3"));
  }

  @Test
  public void testXmlRoundTrip() throws Exception {
    WorkflowList input = new WorkflowList();
    input.add(createWorkflow(idGenerator.create(input)));
    input.add(createWorkflow(idGenerator.create(input)));

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    log.debug(xml);
    WorkflowList output = (WorkflowList) m.unmarshal(xml);
    log.debug("Unmarshalled " + m.marshal(output));
    assertRoundtripEquality(input, output);
  }

  @Test
  public void testJavaxValidation() throws Exception {
    Channel channel = new Channel("testJavaxValidation");
    WorkflowList list = new WorkflowList();
    list.add(new StandardWorkflow()); // this is invalid, cos no UID.
    channel.setWorkflowList(list);
    Set<ConstraintViolation<Object>> violations = validate(null, channel);
    logViolations(violations);
    // We expect 2 violations, one from channel, one from workflowList
    assertEquals(2, violations.size());
    list.clear();
    violations = validate(null, channel);
    assertEquals(0, violations.size());
  }

  private StandardWorkflow createWorkflow(String uid) {
    StandardWorkflow result = new StandardWorkflow();
    result.setUniqueId(uid);
    result.setConsumer(new MockMessageConsumer());
    return result;
  }
}
