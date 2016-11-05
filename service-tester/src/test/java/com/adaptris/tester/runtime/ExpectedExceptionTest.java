package com.adaptris.tester.runtime;

import com.adaptris.core.ServiceException;
import com.adaptris.tester.report.junit.JUnitReportTestIssue;
import com.adaptris.tester.report.junit.JUnitReportTestIssueTyped;

import static org.junit.Assert.*;

public class ExpectedExceptionTest {

  @SuppressWarnings("ThrowableInstanceNeverThrown")
  @org.junit.Test
  public void checkExpected() throws Exception {
    Exception exception = new ServiceException("required-metadata-not-present");
    ExpectedException expectedException = new ExpectedException("com.adaptris.core.ServiceException", "required-metadata-not-present");
    JUnitReportTestIssue result = expectedException.check(exception);
    assertNull(result);
  }

  @SuppressWarnings("ThrowableInstanceNeverThrown")
  @org.junit.Test
  public void checkNotExpected() throws Exception {
    Exception exception = new Exception("required-metadata-not-present");
    ExpectedException expectedException = new ExpectedException("com.adaptris.core.ServiceException", "required-metadata-not-present");
    JUnitReportTestIssueTyped result = expectedException.check(exception);
    assertNotNull(result);
    assertEquals("failure", result.getType());
  }

  @SuppressWarnings("ThrowableInstanceNeverThrown")
  @org.junit.Test
  public void checkMessageNotExpected() throws Exception {
    Exception exception = new ServiceException("does not match");
    ExpectedException expectedException = new ExpectedException("com.adaptris.core.ServiceException", "required-metadata-not-present");
    JUnitReportTestIssueTyped result = expectedException.check(exception);
    assertNotNull(result);
    assertEquals("failure", result.getType());
  }


}