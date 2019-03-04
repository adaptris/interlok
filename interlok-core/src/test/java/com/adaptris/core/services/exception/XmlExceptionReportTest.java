package com.adaptris.core.services.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import com.adaptris.util.XmlUtils;

public class XmlExceptionReportTest {

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testNoStacktrace() throws Exception {
    XmlExceptionReport reporter = new XmlExceptionReport();
    Document d =
        reporter.create(new Exception("testNoStacktrace"), "myWorkflow", "ThrowExceptionService");
    assertNotNull(d);
    new XmlUtils().writeDocument(d, System.err);
  }
  
  @Test
  public void testNoStacktraceNoWorkflow() throws Exception {
    XmlExceptionReport reporter = new XmlExceptionReport();
    Document d =
        reporter.create(new Exception("testNoStacktraceNoWorkflow"), null, "ThrowExceptionService");
    assertNotNull(d);
    new XmlUtils().writeDocument(d, System.err);
  }

  @Test
  public void testWithStacktrace() throws Exception {
    XmlReportWithStacktrace reporter = new XmlReportWithStacktrace();
    Document d =
        reporter.create(new Exception("testWithStacktrace"), "myWorkflow", "ThrowExceptionService");
    assertNotNull(d);
    new XmlUtils().writeDocument(d, System.err);
  }
  
  @Test
  public void testWithStacktraceNoWorkflow() throws Exception {
    XmlReportWithStacktrace reporter = new XmlReportWithStacktrace();
    Document d =
        reporter.create(new Exception("testWithStacktraceNoWorkflow"), null, "ThrowExceptionService");
    assertNotNull(d);
    new XmlUtils().writeDocument(d, System.err);
  }

  @Test
  public void testExceptionReport_NoStacktrace() throws Exception {
    ExceptionReport report =
        new ExceptionReport(new Exception("testExceptionReport_NoStacktrace"), false)
        .withWorkflow("myWorkflow").withExceptionLocation("ThrowExceptionService");
    Map<String, Object> map = report.asMap();
    assertTrue(map.containsKey(ExceptionReport.EXCEPTION_LOCATION));
    assertTrue(map.containsKey(ExceptionReport.WORKFLOW));
    assertTrue(map.containsKey(ExceptionReport.EXCEPTION_MESSAGE));
    assertFalse(map.containsKey(ExceptionReport.STACKTRACE));
    assertEquals("myWorkflow", map.get(ExceptionReport.WORKFLOW));
  }
  
  @Test
  public void testExceptionReport_NoStacktrace_NoWorkflow() throws Exception {
    ExceptionReport report =
        new ExceptionReport(new Exception("testExceptionReport_NoStacktrace_NoWorkflow"), false)
        .withWorkflow(null).withExceptionLocation("ThrowExceptionService");
    Map<String, Object> map = report.asMap();
    assertTrue(map.containsKey(ExceptionReport.EXCEPTION_LOCATION));
    assertTrue(map.containsKey(ExceptionReport.WORKFLOW));
    assertTrue(map.containsKey(ExceptionReport.EXCEPTION_MESSAGE));
    assertFalse(map.containsKey(ExceptionReport.STACKTRACE));
    assertEquals(null, map.get(ExceptionReport.WORKFLOW));
  }

  @Test
  public void testExceptionReport_Stacktrace() throws Exception {
    ExceptionReport report =
        new ExceptionReport(new Exception("testExceptionReport_Stacktrace"), true)
        .withWorkflow("myWorkflow").withExceptionLocation("ThrowExceptionService");
    Map<String, Object> map = report.asMap();
    assertTrue(map.containsKey(ExceptionReport.EXCEPTION_LOCATION));
    assertTrue(map.containsKey(ExceptionReport.WORKFLOW));
    assertTrue(map.containsKey(ExceptionReport.EXCEPTION_MESSAGE));
    assertTrue(map.containsKey(ExceptionReport.STACKTRACE));
    assertEquals("myWorkflow", map.get(ExceptionReport.WORKFLOW));
  }

}
