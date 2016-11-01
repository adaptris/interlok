package com.adaptris.tester.runtime.messages.assertion;


public class AssertionResult {

  private final String uniqueId;
  private final String type;
  private final boolean passed;
  private String message;

  public AssertionResult(String uniqueId, String type, boolean passed){
    this.uniqueId = uniqueId;
    this.type = type;
    this.passed = passed;
  }

  public AssertionResult(String uniqueId,String type, boolean passed, String message){
    this(uniqueId, type, passed);
    this.message = message;
  }

  public boolean isPassed() {
    return passed;
  }

  public String getMessage() {
    if(message != null){
      return message;
    } else {
      return "Assertion Failure: [" + type + "]";
    }
  }
}
