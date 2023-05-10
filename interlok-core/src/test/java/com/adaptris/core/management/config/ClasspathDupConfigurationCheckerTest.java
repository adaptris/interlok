package com.adaptris.core.management.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClasspathDupConfigurationCheckerTest {

  private ClasspathDupConfigurationChecker checker;

  @BeforeEach
  public void setUp() throws Exception {
    checker = new ClasspathDupConfigurationChecker();

    checker.setDebug(true);
  }

  @AfterEach
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
