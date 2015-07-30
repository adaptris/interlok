/*
 * $Id: TestURLString.java,v 1.5 2008/06/05 17:25:33 lchan Exp $
 */

package com.adaptris.util;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author lchan
 */
public class TestURLString extends TestCase {

  private static Log logR = LogFactory.getLog(TestURLString.class);

  private static final String testUrl = "http://myuser:mypassword@localhost:8888//url";
  private static String username = "myuser";
  private static String password = "mypassword";
  private static String host = "localhost";
  private static int port = 8888;
  private static String file = "/url";
  private static String protocol = "http";

  public TestURLString(java.lang.String testName) {
    super(testName);
  }

  public void testUrlByString() throws Exception {
    URLString url = new URLString(testUrl);
    assertEquals(protocol, url.getProtocol());
    assertEquals(username, url.getUsername());
    assertEquals(password, url.getPassword());
    assertEquals(host, url.getHost());
    assertEquals(port, url.getPort());
    assertEquals(file, url.getFile());
    assertEquals(testUrl, url.toString());
  }

  public void testUrlByURL() throws Exception {
    URLString url = new URLString(new URL(testUrl));
    assertEquals(protocol, url.getProtocol());
    assertEquals(username, url.getUsername());
    assertEquals(password, url.getPassword());
    assertEquals(host, url.getHost());
    assertEquals(port, url.getPort());
    assertEquals(file, url.getFile());
    assertEquals(testUrl, url.toString());
  }

  public void testUrlByComponents() throws Exception {
    URLString url = new URLString(protocol, host, port, file, username, password);
    assertEquals(protocol, url.getProtocol());
    assertEquals(username, url.getUsername());
    assertEquals(password, url.getPassword());
    assertEquals(host, url.getHost());
    assertEquals(port, url.getPort());
    assertEquals(file, url.getFile());
    assertEquals(testUrl, url.toString());
  }

  public void testUrlNonUrl() throws Exception {
    URLString url = new URLString("config.xml");
    logR.trace(url.getFile());
    assertEquals("config.xml", url.getFile());

  }

  public void testBug898() throws Exception {
    String buggyURL = "smtp://user%40btinternet.com:password@mail.btinternet.com/";
    String username = "user@btinternet.com";
    String password = "password";
    String host = "mail.btinternet.com";
    String protocol = "smtp";

    URLString url = new URLString(buggyURL);
    assertEquals(protocol, url.getProtocol());
    assertEquals(username, url.getUsername());
    assertEquals(password, url.getPassword());
    assertEquals(host, url.getHost());
    assertEquals(buggyURL, url.toString());
  }
}
