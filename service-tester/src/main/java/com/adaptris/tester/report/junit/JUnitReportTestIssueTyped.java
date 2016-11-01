package com.adaptris.tester.report.junit;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class JUnitReportTestIssueTyped implements JUnitReportTestIssue {

  @XStreamAsAttribute
  private final String message;
  @XStreamAsAttribute
  private final String type;

  public JUnitReportTestIssueTyped(String message, String type){
    this.message = message;
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
