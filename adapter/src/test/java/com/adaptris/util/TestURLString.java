/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.util;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

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

  public void testGetURL() throws Exception {
    String httpURL = "http://config.f4f.com/v3config/adapter.xml";
    URLString url = new URLString(httpURL);
    assertEquals(httpURL, url.getURL().toString());
    String fileURL = "file:///./config/adapter.xml";
    // This should skip the first "/"
    url = new URLString(fileURL);
    assertEquals("./config/adapter.xml", url.getURL().getFile());

    String httpURL_2 = "http://config.f4f.com//v3config/adapter.xml";
    url = new URLString(httpURL_2);
    // The first / should get dropped but we're still OK.
    assertEquals(httpURL, url.getURL().toString());
  }
}
