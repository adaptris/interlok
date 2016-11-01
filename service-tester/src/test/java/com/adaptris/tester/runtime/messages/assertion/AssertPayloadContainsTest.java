package com.adaptris.tester.runtime.messages.assertion;

import org.junit.Test;

import static org.junit.Assert.*;

public class AssertPayloadContainsTest {
  @Test
  public void execute() throws Exception {
    String actual = "1234hello1234";
    PayloadAssertion matcher = new AssertPayloadContains();
    matcher.setPayload("hello");
    assertTrue(matcher.execute(actual).isPassed());
    matcher.setPayload("notthis");
    assertFalse(matcher.execute(actual).isPassed());
  }

}