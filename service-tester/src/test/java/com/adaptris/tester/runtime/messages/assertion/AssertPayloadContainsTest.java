package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.tester.runtime.messages.TestMessage;
import org.junit.Test;

import java.util.HashMap;

public class AssertPayloadContainsTest extends AssertionCase {

  public AssertPayloadContainsTest(String name) {
    super(name);
  }

  @Test
  public void testExecute() throws Exception {
    String actual = "1234hello1234";
    PayloadAssertion matcher = new AssertPayloadContains();
    matcher.setPayload("hello");
    assertTrue(matcher.execute(new TestMessage(new HashMap<String, String>(),actual)).isPassed());

    matcher.setPayload("notthis");
    assertFalse(matcher.execute(new TestMessage(new HashMap<String, String>(),actual)).isPassed());
  }

  @Test
  public void testExpected(){
    assertEquals("Payload: hello", createAssertion().expected());
  }

  @Test
  public void testGetMessage(){
    AssertionResult result  = createAssertion().execute(new TestMessage());
    assertEquals("Assertion Failure: [assert-payload-contains]", result.getMessage());
  }

  @Override
  protected Assertion createAssertion() {
    return new AssertPayloadContains("hello");
  }
}