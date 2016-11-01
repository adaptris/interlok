package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.tester.runtime.TestComponent;
import com.adaptris.tester.runtime.messages.TestMessage;

public abstract class Assertion implements TestComponent {

  private String uniqueId;

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  public abstract AssertionResult execute(TestMessage actual);

  public abstract String expected();

}
