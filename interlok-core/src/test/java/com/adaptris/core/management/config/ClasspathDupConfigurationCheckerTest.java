package com.adaptris.core.management.config;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClasspathDupConfigurationCheckerTest {

  private ClasspathDupConfigurationChecker checker;
  
  @Before
  public void setUp() throws Exception {
    checker = new ClasspathDupConfigurationChecker();
    
    System.setProperty("interlok.bootstrap.debug", "true");
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testDeDup() throws Exception {
    // this one may actually fail occassionally.
    ConfigurationCheckReport report = checker.performConfigCheck(null, null);
    if(!report.isCheckPassed()) {
      assertNotNull(report.getFailureException());
    }
    assertNotNull(report.toString());
  }
  
}
