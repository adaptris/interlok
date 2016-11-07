package com.adaptris.tester.report.junit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JUnitReportPropertyTest {

  @Test
  public void testGet() throws Exception {
    JUnitReportProperty p = new JUnitReportProperty("name", "value");
    assertEquals("name", p.getName());
    assertEquals("value", p.getValue());
  }

}