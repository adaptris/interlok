package com.adaptris.tester.report.junit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JUnitReportFailureTest {

  private final static String MESSAGE  = "An error occurred";
  private final static String TEXT  = "Details about the error.";

  @Test
  public void testGetType() throws Exception {
    JUnitReportFailure j = createReportFailure();
    assertEquals("failure", j.getType());
  }

  @Test
  public void testGetMessage() throws Exception {
    JUnitReportFailure j = createReportFailure();
    assertEquals(MESSAGE, j.getMessage());
  }

  @Test
  public void testGetText() throws Exception {
    JUnitReportFailure j = createReportFailure();
    assertEquals(TEXT, j.getText());
  }

  private JUnitReportFailure createReportFailure(){
    return new JUnitReportFailure(MESSAGE, TEXT);
  }
}