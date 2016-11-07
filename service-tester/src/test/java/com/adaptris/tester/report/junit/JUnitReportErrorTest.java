package com.adaptris.tester.report.junit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JUnitReportErrorTest {

  private final static String MESSAGE  = "An error occurred";
  private final static String TEXT  = "Details about the error.";

  @Test
  public void testGetType() throws Exception {
    JUnitReportError j = createReportError();
    assertEquals("error", j.getType());
  }

  @Test
  public void testGetMessage() throws Exception {
    JUnitReportError j = createReportError();
    assertEquals(MESSAGE, j.getMessage());
  }

  @Test
  public void testGetText() throws Exception {
    JUnitReportError j = createReportError();
    assertEquals(TEXT, j.getText());
  }

  private JUnitReportError createReportError(){
    return new JUnitReportError(MESSAGE, TEXT);
  }
}