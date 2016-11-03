package com.adaptris.tester.runtime.messages.assertion;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("assert-payload-equals")
public class AssertPayloadEquals extends PayloadAssertion {

  public AssertPayloadEquals(){
    super();
  }

  public AssertPayloadEquals(String payload){
    super(payload);
  }

  @Override
  public AssertionResult execute(String actual) {
    return new AssertionResult(getUniqueId(), "assert-payload-equals", getPayload().equals(actual));
  }
}
