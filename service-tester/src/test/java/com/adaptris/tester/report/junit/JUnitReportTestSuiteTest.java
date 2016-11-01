package com.adaptris.tester.report.junit;

import com.adaptris.core.DefaultMarshaller;
import org.junit.Ignore;
import org.junit.Test;

public class JUnitReportTestSuiteTest {

  @Ignore
  @Test
  public void marshall() throws Exception {
    JUnitReportTestSuite suite = new JUnitReportTestSuite("blah.base_test_1");
    JUnitReportTestCase successCase = new JUnitReportTestCase("001-passed-test");
    successCase.setTime(40);
    suite.addTestCase(successCase);
    suite.addProperty(new JUnitReportProperty("assert-passed", "1"));
    JUnitReportTestCase failureCase = new JUnitReportTestCase("002-failed-test");
    failureCase.setTestIssue(new JUnitReportFailure("Assertion FAILED: some failed assert"));
    failureCase.setTime(10);
    suite.addTestCase(failureCase);
    JUnitReportTestCase errorCase = new JUnitReportTestCase("003-errord-test");
    errorCase.setTime(30);
    errorCase.setTestIssue(new JUnitReportError("Assertion ERROR: some error assert"));
    suite.addTestCase(errorCase);
    suite.setTime(70);
    String s = DefaultMarshaller.getDefaultMarshaller().marshal(suite);
    System.out.println(s);
  }

}