package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.tester.runtime.messages.TestMessage;

public abstract class PayloadAssertion extends Assertion {

  private String payload;

  public PayloadAssertion(){
    setPayload("");
  }

  public PayloadAssertion(String payload){
    setPayload(payload);
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getPayload() {
    return payload;
  }

  protected abstract AssertionResult execute(String actual);

  @Override
  public final AssertionResult execute(TestMessage actual){
    return execute(actual.getPayload());
  }

  @Override
  public String expected() {
    return "Payload: " + getPayload();
  }
}
