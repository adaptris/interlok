package com.adaptris.tester.runtime.messages.assertion;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("assert-payload-contains")
public class AssertPayloadContains extends PayloadAssertion {

  @Override
  public AssertionResult execute(String actual) {
    return new AssertionResult(getUniqueId(), "assert-payload-contains", actual.contains(getPayload()));
  }
}
