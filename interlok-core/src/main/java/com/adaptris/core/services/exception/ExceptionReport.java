package com.adaptris.core.services.exception;

import java.util.Map;
import java.util.TreeMap;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Object that wraps all the information for an exception report.
 */
@XStreamAlias("exception-report")
public class ExceptionReport {

  public static final String STACKTRACE = "stacktrace";
  public static final String EXCEPTION_MESSAGE = "exceptionMessage";
  public static final String EXCEPTION_LOCATION = "exceptionLocation";
  public static final String WORKFLOW = "workflow";

  private String exceptionMessage;
  private StackTraceElement[] stacktrace;
  private String exceptionLocation;
  private String workflow;


  public ExceptionReport() {

  }

  public ExceptionReport(Exception e, boolean withStackTrace) {
    this();
    exceptionMessage = e.getMessage();
    if (withStackTrace) {
      stacktrace = e.getStackTrace();
    }
  }

  public ExceptionReport withExceptionLocation(String s) {
    exceptionLocation = s;
    return this;
  }

  public ExceptionReport withWorkflow(String s) {
    workflow = s;
    return this;
  }

  // Refactor so that we can just use this in ExceptionAsJson etc.
  public Map<String, Object> asMap() {
    Map<String, Object> result = new TreeMap<String, Object>();
    result.put(WORKFLOW, workflow);
    result.put(EXCEPTION_LOCATION, exceptionLocation);
    result.put(EXCEPTION_MESSAGE, exceptionMessage);
    if (stacktrace != null) {
      result.put(STACKTRACE, stacktrace);
    }
    return result;
  }
}
