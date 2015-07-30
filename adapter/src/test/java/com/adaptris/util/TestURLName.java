/*
 * $Id: TestURLName.java,v 1.1 2008/06/05 17:28:16 lchan Exp $
 */

package com.adaptris.util;

import javax.mail.URLName;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author lchan
 */
public class TestURLName extends TestCase {

  private static Log logR = LogFactory.getLog(TestURLName.class);

  private static String testUrl = "smtp://user%40btinternet.com:password@mail.btinternet.com/";
  private static String username = "user@btinternet.com";
  private static String password = "password";
  private static String host = "mail.btinternet.com";
  private static String protocol = "smtp";

  public TestURLName(java.lang.String testName) {
    super(testName);
  }

  public void testUrl() throws Exception {
    URLName url = new URLName(testUrl);
    assertEquals(protocol, url.getProtocol());
    assertEquals(username, url.getUsername());
    assertEquals(password, url.getPassword());
    assertEquals(host, url.getHost());
    assertEquals(testUrl, url.toString());
  }

}
