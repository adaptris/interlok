/*
 * $Id: TestGetServiceByName.java,v 1.3 2003/10/16 07:29:33 lchan Exp $
 */

package com.adaptris.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author lchan
 */
public class TestGetServiceByName extends TestCase
{

  public TestGetServiceByName(java.lang.String testName)
  {
    super(testName);
  }

  public static void main(java.lang.String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite(TestGetServiceByName.class);
    return suite;
  }

  // Add test methods here, they have to start with 'test' name.
  // for example:
  // public void testHello() {}
  public void testGetHttpsServicePort()
  {
    assertEquals("Get Https port",
                GetServiceByName.getPort("https", "tcp"),
                443);
  }

  public void testGetSmtpServicePort()
  {
    assertEquals("Get SMTP port",
                GetServiceByName.getPort("smtp", "tcp"),
                25);
  }

  public void testGetHttpServicePort()
  {
    assertEquals("Get Http port",
                GetServiceByName.getPort("http", "tcp"),
                80);
  }

  public void testGetNonExistentPort() {
    assertEquals("Non-Existent Port",
                GetServiceByName.getPort("asdfasdfadsf", "tcp"),
                -1);
  }
  @Override
  protected void finalize() throws Throwable
  {
    super.finalize();
  }
}


