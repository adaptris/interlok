package com.adaptris.core;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.PlainIdGenerator;
import com.adaptris.util.license.License.LicenseType;

public class WorkflowListTest extends BaseCase {

  private IdGenerator idGenerator;
  private Logger log = Logger.getLogger(this.getClass());

  public WorkflowListTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  public void setUp() throws Exception {
    idGenerator = new PlainIdGenerator();
  }

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

  public void testRemoveByObject() throws Exception {
    WorkflowList list = new WorkflowList();
    Workflow workflow1 = createWorkflow(getName());
    list.add(workflow1);
    assertEquals(1, list.size());
    assertTrue(list.removeWorkflow(workflow1));
    assertEquals(0, list.size());
  }

  public void testRedmine5407() throws Exception {
    testRemoveThenAddAgain();
  }

  public void testRemoveNull() throws Exception {
    WorkflowList list = new WorkflowList();
    assertFalse(list.remove(null));
    assertFalse(list.removeWorkflow(null));
  }

  public void testRemovePlainObject() throws Exception {
    WorkflowList list = new WorkflowList();
    assertFalse(list.remove(new Object()));

  }

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


  public void testAddDuplicate() {
    WorkflowList list = new WorkflowList();
    StandardWorkflow toAdd = createWorkflow(idGenerator.create(list));
    toAdd.setUniqueId(null);
    list.add(toAdd);
    list.add(toAdd);
    assertEquals(2, list.size());
  }

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

  public void testSetLifecycleStrategy() throws Exception {
    WorkflowList list = new WorkflowList();
    assertNull(list.getLifecycleStrategy());
    DefaultWorkflowLifecycleStrategy dwls = new DefaultWorkflowLifecycleStrategy();
    list.setLifecycleStrategy(dwls);
    assertEquals(dwls, list.getLifecycleStrategy());
    list.setLifecycleStrategy(null);
    assertNull(list.getLifecycleStrategy());
  }

  public void testGetByInt() {
    WorkflowList list = new WorkflowList();
    Workflow toAdd = createWorkflow(idGenerator.create(list));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(toAdd);
    assertEquals(toAdd, list.get(1));
  }

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

  public void testGetWorkflows() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    assertNotNull(list.getWorkflows());
    assertEquals(1, list.getWorkflows().size());
  }

  public void testIndexOf() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    Workflow toAdd = createWorkflow(idGenerator.create(list));
    list.add(toAdd);
    assertEquals(3, list.size());
    assertEquals(2, list.indexOf(toAdd));
  }

  public void testIterator() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    assertNotNull(list.iterator());
    assertTrue(list.iterator().hasNext());
  }

  public void testLastIndexOf() {
    WorkflowList list = new WorkflowList();
    Workflow toAdd = createWorkflow(idGenerator.create(list));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(toAdd);
    assertEquals(2, list.lastIndexOf(toAdd));
  }

  public void testListIterator() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    assertNotNull(list.listIterator());
    assertNotNull(list.listIterator(1));
  }

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

  public void testSubList() {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    list.add(createWorkflow(idGenerator.create(list)));
    assertNotNull(list.subList(0, 2));
    assertEquals(2, list.subList(0, 2).size());
  }

  public void testIsEnabled() throws CoreException {
    WorkflowList list = new WorkflowList();
    list.add(createWorkflow(idGenerator.create(list)));
    StandardWorkflow wf = createWorkflow(idGenerator.create(list));
    wf.setConsumer(new PtpConsumer());
    list.add(wf);
    assertEquals(true, list.isEnabled(new LicenseStub()));
    assertEquals(false, list.isEnabled(new LicenseStub(EnumSet.of(LicenseType.Restricted))));
  }

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

  private StandardWorkflow createWorkflow(String uid) {
    StandardWorkflow result = new StandardWorkflow();
    result.setUniqueId(uid);
    new MockMessageConsumer(new ConfiguredConsumeDestination(uid));
    return result;
  }
}