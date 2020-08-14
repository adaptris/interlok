package com.adaptris.core.management.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClasspathDupConfigurationCheckerTest {

  private ClasspathDupConfigurationChecker checker;

  @Before
  public void setUp() throws Exception {
    checker = new ClasspathDupConfigurationChecker();

    checker.setDebug(true);
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testDeDup() throws Exception {
    // this one may actually fail occasionally.
    ConfigurationCheckReport report = checker.performConfigCheck(null);
    if(!report.isCheckPassed()) {
      assertTrue(report.getWarnings().size() > 0);
    }
    assertNotNull(report.toString());
  }

  @Test
  public void testDeDupWithNoLogging() throws Exception {
    // this one may actually fail occasionally.
    checker.setDebug(false);
    ConfigurationCheckReport report = checker.performConfigCheck(null);
    if(!report.isCheckPassed()) {
      assertTrue(report.getWarnings().size() > 0);
    }
    assertNotNull(report.toString());
  }

}
