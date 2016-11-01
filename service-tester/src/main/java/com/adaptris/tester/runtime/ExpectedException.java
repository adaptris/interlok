package com.adaptris.tester.runtime;

import com.adaptris.tester.report.junit.JUnitReportError;
import com.adaptris.tester.report.junit.JUnitReportFailure;
import com.adaptris.tester.report.junit.JUnitReportTestIssue;
import com.adaptris.tester.report.junit.JUnitReportTestIssueTyped;
import org.apache.commons.lang.exception.ExceptionUtils;

public class ExpectedException {

  private String className;
  private String message;

  public ExpectedException(){
    className = "com.adaptris.core.ServiceException";
    message = null;
  }

  public ExpectedException(String className){
    this.className = className;
    message = null;
  }

  public ExpectedException(String className, String message){
    this.className = className;
    this.message = message;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public JUnitReportTestIssueTyped check(Exception e){
    JUnitReportTestIssueTyped result = null;
    try {
      if (!Class.forName(className).isInstance(e)) {
        return new JUnitReportFailure("Assertion Failure: Expected Exception [" + className + "]", ExceptionUtils.getStackTrace(e));
      }
      if (message != null && !e.getMessage().equals(getMessage())){
        return new JUnitReportFailure("Assertion Failure: Expected Exception [" + className + "] message didn't match [" + message + "]", ExceptionUtils.getStackTrace(e));
      }
    } catch (Exception e1){
      result = new JUnitReportError("Test Error: [" + e1.toString() + "]", ExceptionUtils.getStackTrace(e1));
    }
    return result;
  }
}
