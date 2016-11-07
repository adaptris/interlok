package com.adaptris.tester.report.junit;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class JUnitReportTestIssueTyped implements JUnitReportTestIssue {

  @XStreamAsAttribute
  private String message;
  @XStreamAsAttribute
  private final String type;

  public JUnitReportTestIssueTyped(String type){
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage(){
    return message;
  }
}
