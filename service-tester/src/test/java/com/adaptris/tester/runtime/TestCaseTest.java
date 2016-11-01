package com.adaptris.tester.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCaseTest {

  @org.junit.Test
  public void isTestToBeExecuted() throws Exception {
    TestCase tc = new TestCase("testlist.test.testcase");
    assertTrue(tc.isTestToBeExecuted("testlist.test.testcase"));
    assertFalse(tc.isTestToBeExecuted("testlist.test.other"));
    tc = new TestCase("testlist.test.*");
    assertTrue(tc.isTestToBeExecuted("testlist.test.testcase"));
    assertTrue(tc.isTestToBeExecuted("testlist.test.other"));
    tc = new TestCase("*.testcase");
    assertTrue(tc.isTestToBeExecuted("testlist.test.testcase"));
    assertFalse(tc.isTestToBeExecuted("testlist.test.other"));
  }

}