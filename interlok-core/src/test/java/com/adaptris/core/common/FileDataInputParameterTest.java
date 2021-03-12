/*
 * Copyright 2016 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.common;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.http.jetty.EmbeddedConnection;
import com.adaptris.core.http.jetty.EmbeddedJettyHelper;
import com.adaptris.core.http.jetty.JettyHelper;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.types.InterlokMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static com.adaptris.core.http.jetty.EmbeddedJettyHelper.URL_TO_POST_TO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

@SuppressWarnings("deprecation")
public class FileDataInputParameterTest {

  public static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();


  @Test
  public void testDestination() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileDataInputParameter p = new FileDataInputParameter();
    try {
      p.url(m);
      fail();
    } catch (IllegalArgumentException e) {
      // ok
    }
    p.setEndPoint("file:////tmp/abc");
    assertEquals("file:////tmp/abc", p.url(m));
    try {
      p.setEndPoint(null);
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals("file:////tmp/abc", p.url(m));
  }
  
  @Test(expected=RuntimeException.class)
  public void testInterlokMessage() throws Exception {
    InterlokMessage msg = Mockito.mock(InterlokMessage.class);
    FileDataInputParameter p = new FileDataInputParameter();
    p.setEndPoint("file:////tmp/doesnotexist");
    p.url(msg);
  }
  
  @Test(expected = CoreException.class)
  public void testNonExistingFile() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileDataInputParameter p = new FileDataInputParameter();
    try {
      p.url(m);
      fail();
    } catch (IllegalArgumentException e) {
      // ok
    }
    p.setEndPoint("file:////tmp/doesnotexist");
    assertEquals("file:////tmp/doesnotexist", p.url(m));
    String result = p.extract(m);
  }


  @Test
  public void testExtract() throws Exception {
    FileDataInputParameter p = new FileDataInputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setEndPoint("file:///" + f.getCanonicalPath());
    FileUtils.write(f, TEXT, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, p.extract(msg));
  }

  @Test
  public void testExtractDestination() throws Exception {
    FileDataInputParameter p = new FileDataInputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setEndPoint("file:///" + f.getCanonicalPath());
    FileUtils.write(f, TEXT, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, p.extract(msg));
  }

  @Test
  public void testExtractFromClasspath() throws Exception {
    FileDataInputParameter p = new FileDataInputParameter();
    p.setEndPoint("xstream-standalone.xml");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNotNull(p.extract(msg));
  }

  @Test
  public void testExtractFromRemote() throws Exception {
    EmbeddedJettyHelper helper = new EmbeddedJettyHelper();
    helper.startServer();

    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel channel = JettyHelper.createChannel(new EmbeddedConnection(), JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      LifecycleHelper.initAndStart(channel);
      FileDataInputParameter p = new FileDataInputParameter();
      p.setEndPoint(helper.createProduceDestination());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      assertNotNull(p.extract(msg));
    } finally {
      LifecycleHelper.stopAndClose(channel);
      helper.stopServer();
    }
  }

  @Test
  public void testWrap() throws Exception {
    FileInputMessageWrapper p = new FileInputMessageWrapper();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setEndPoint("file:///" + f.getCanonicalPath());
    FileUtils.write(f, TEXT, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try (InputStream in = p.wrap(msg)) {
      assertNotNull(in);
      List<String> content = IOUtils.readLines(in);
      assertEquals(1, content.size());
      assertEquals(TEXT, content.get(0));
    }
  }
}
