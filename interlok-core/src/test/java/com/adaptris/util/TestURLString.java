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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.core.stubs.TempFileUtils;

/**
 *
 * @author lchan
 */
public class TestURLString extends BaseCase {

  private static Log logR = LogFactory.getLog(TestURLString.class);

  private static final String TEST_DIR = "urlstring.dir";
  private static final String TEST_SERIALIZED_FILE = "urlstring.serialized";

  protected File testOutputDir;

  @Before
  public void setUp() throws Exception {
    testOutputDir = new File(PROPERTIES.getProperty(TEST_DIR));
    testOutputDir.mkdirs();
  }

  private static final String testUrl = "http://myuser:mypassword@localhost:8888//url";
  private static String username = "myuser";
  private static String password = "mypassword";
  private static String host = "localhost";
  private static int port = 8888;
  private static String file = "/url";
  private static String protocol = "http";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
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

  @Test
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

  @Test
  public void testUrlByFile() throws Exception {
    Object marker = new Object();
    File trackedFile = TempFileUtils.createTrackedFile(marker);
    URLString url = new URLString(trackedFile);
    assertEquals("file", url.getProtocol());
    assertEquals(trackedFile.getCanonicalPath(), new File(url.getFile()).getCanonicalPath());
  }

  @Test
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

  @Test
  public void testUrlNonUrl() throws Exception {
    URLString url = new URLString("config.xml");
    logR.trace(url.getFile());
    assertEquals("config.xml", url.getFile());
    assertEquals("config.xml", url.toString());

  }

  @Test
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

  @Test
  public void testEquals() throws Exception {
    URLString url1 = new URLString("http://config.f4f.com/v3config/adapter.xml");
    URLString url_pw = new URLString("http://user%40btinternet.com:password@mail.btinternet.com/");
    assertFalse(url1.equals(new Object()));
    assertFalse(url1.equals(null));

    assertTrue(newInstance().equals(newInstance()));
    assertEquals(url1, new URLString("http://config.f4f.com/v3config/adapter.xml"));
    assertEquals(url1.hashCode(), new URLString("http://config.f4f.com/v3config/adapter.xml").hashCode());

    assertNotSame(url1, new URLString("http://config.f4f.xxx/v3config/adapter.xml"));
    assertNotSame(url1.hashCode(), new URLString("http://config.f4f.xxx/v3config/adapter.xml").hashCode());

    assertNotSame(url1, new URLString("http://config.f4f.com/xxxx/adapter.xml"));
    assertNotSame(url1.hashCode(), new URLString("http://config.f4f.com/xxxx/adapter.xml").hashCode());

    assertNotSame(url1, new URLString("http://config.f4f.com/v3config/xxx.xml"));
    assertNotSame(url1.hashCode(), new URLString("http://config.f4f.com/v3config/xxx.xml").hashCode());

    assertNotSame(url1, new URLString("https://config.f4f.com/v3config/adapter.xml"));
    assertNotSame(url1.hashCode(), new URLString("https://config.f4f.com/v3config/adapter.xml").hashCode());

    assertNotSame(url1, url_pw);
    assertNotSame(url1.hashCode(), url_pw.hashCode());

    assertEquals(url_pw, new URLString("http://user%40btinternet.com:password@mail.btinternet.com/"));
    assertEquals(url_pw.hashCode(), new URLString("http://user%40btinternet.com:password@mail.btinternet.com/").hashCode());

    assertNotSame(url_pw, new URLString("http://user%40btinternet.com:password1@mail.btinternet.com/"));
    assertNotSame(url_pw.hashCode(), new URLString("http://user%40btinternet.com:password1@mail.btinternet.com/").hashCode());

    assertNotSame(url_pw, new URLString("http://user:password@mail.btinternet.com/"));
    assertNotSame(url_pw.hashCode(), new URLString("http://user:password@mail.btinternet.com/").hashCode());
  }

  @Test
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

  @Test
  public void testSerialize() throws Exception {
    URLString url = new URLString("http://config.f4f.com/v3config/adapter.xml");
    URLString roundtrip = roundTrip(url);
    assertEquals(url, roundtrip);
    assertEquals("http://config.f4f.com/v3config/adapter.xml", roundtrip.toString());

    url = new URLString("http://user:password@mail.btinternet.com/");
    roundtrip = roundTrip(url);
    assertEquals(url, roundtrip);
    assertEquals("http://user:password@mail.btinternet.com/", roundtrip.toString());

    url = new URLString("http://user@mail.btinternet.com/");
    roundtrip = roundTrip(url);
    assertEquals(url, roundtrip);
    assertEquals("http://user@mail.btinternet.com/", roundtrip.toString());

  }

  @Test
  public void testUnserialize() throws Exception {
    String httpURL = "http://config.f4f.com/v3config/adapter.xml";
    URLString urlString = new URLString(httpURL);
    File f = new File(PROPERTIES.getProperty(TEST_SERIALIZED_FILE));
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
      URLString url = (URLString) in.readObject();
      assertEquals(httpURL, url.toString());
      assertEquals(urlString, url);
      assertEquals(urlString.hashCode(), url.hashCode());
    }
  }

  private URLString roundTrip(URLString url) throws Exception {
    URLString roundtrip = null;
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(f))) {
      output.writeObject(url);
    }
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
      roundtrip = (URLString) in.readObject();
    }
    return roundtrip;
  }

  private URLString newInstance() throws Exception {
    Constructor[] ctors = URLString.class.getDeclaredConstructors();
    Constructor ctor = noArg(ctors);
    ctor.setAccessible(true);
    return (URLString) ctor.newInstance();
  }

  Constructor noArg(Constructor[] ctors) {
    Constructor result = null;
    for (Constructor ctor : ctors) {
      if (ctor.getGenericParameterTypes().length == 0) {
        result = ctor;
        break;
      }
    }
    return result;
  }
}
