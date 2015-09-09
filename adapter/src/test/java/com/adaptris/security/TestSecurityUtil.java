package com.adaptris.security;

import java.security.SecureRandom;

import junit.framework.TestCase;

import com.adaptris.security.util.SecurityUtil;

/**
 * @author lchan
 *
 */
public class TestSecurityUtil extends TestCase {

  /**
   * @param arg0
   */
  public TestSecurityUtil(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() {
    SecurityUtil.addProvider();
  }

  @Override
  protected void tearDown() {
  }

  public void testSecureRandomInstance() throws Exception {
      SecureRandom sr = SecurityUtil.getSecureRandom();
      assertTrue(sr == SecurityUtil.getSecureRandom());
  }
}
