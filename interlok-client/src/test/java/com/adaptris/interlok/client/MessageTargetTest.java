package com.adaptris.interlok.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MessageTargetTest {

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testAdapter() {
    MessageTarget t = new MessageTarget();
    assertNull(t.getAdapter());
    t.setAdapter(testName.getMethodName());
    assertEquals(testName.getMethodName(), t.getAdapter());
  }

  @Test
  public void testChannel() {
    MessageTarget t = new MessageTarget();
    assertNull(t.getChannel());
    t.setChannel(testName.getMethodName());
    assertEquals(testName.getMethodName(), t.getChannel());
  }


  @Test
  public void testWorkflow() {
    MessageTarget t = new MessageTarget();
    assertNull(t.getWorkflow());
    t.setWorkflow(testName.getMethodName());
    assertEquals(testName.getMethodName(), t.getWorkflow());
  }

  @Test
  public void testConvenience() {
    MessageTarget t =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
            .withWorkflow(testName.getMethodName());
    assertEquals(testName.getMethodName(), t.getAdapter());
    assertEquals(testName.getMethodName(), t.getChannel());
    assertEquals(testName.getMethodName(), t.getWorkflow());
  }


}
