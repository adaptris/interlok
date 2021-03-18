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

import static com.adaptris.core.http.jetty.EmbeddedJettyHelper.URL_TO_POST_TO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;
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

@SuppressWarnings("deprecation")
public class FileInputStreamDataInputParameterTest {

  public static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();


  @Test
  public void testDestination() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileInputStreamDataInputParameter p = new FileInputStreamDataInputParameter();
    try {
      p.url(m);
      fail();
    } catch (IllegalArgumentException e) {
      // ok
    }
    p.setEndpoint("file:////tmp/abc");
    assertEquals("file:////tmp/abc", p.url(m));
    try {
      p.setEndpoint(null);
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals("file:////tmp/abc", p.url(m));
  }

  @Test(expected=RuntimeException.class)
  public void testInterlokMessage() throws Exception {
    InterlokMessage msg = Mockito.mock(InterlokMessage.class);
    FileInputStreamDataInputParameter p = new FileInputStreamDataInputParameter();
    p.setEndpoint("file:////tmp/doesnotexist");
    p.url(msg);
  }

  @Test(expected = CoreException.class)
  public void testNonExistingFile() throws Exception {
    AdaptrisMessage m = new DefaultMessageFactory().newMessage();
    FileInputStreamDataInputParameter p = new FileInputStreamDataInputParameter();
    try {
      p.url(m);
      fail();
    } catch (IllegalArgumentException e) {
      // ok
    }
    p.setEndpoint("file:////tmp/doesnotexist");
    assertEquals("file:////tmp/doesnotexist", p.url(m));
    InputStream result = p.extract(m);
  }


  @Test
  public void testExtract() throws Exception {
    FileInputStreamDataInputParameter p = new FileInputStreamDataInputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setEndpoint("file:///" + f.getCanonicalPath());
    FileUtils.write(f, TEXT, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNotSame(TEXT, msg.getContent());
    InputStream stream = p.extract(msg);
    byte[] result = new byte[TEXT.length()];
    stream.read(result);
    assertEquals(TEXT, new String(result));
  }

  @Test
  public void testExtractDestination() throws Exception {
    FileInputStreamDataInputParameter p = new FileInputStreamDataInputParameter();
    File f = TempFileUtils.createTrackedFile(testName.getMethodName(), "", p);
    p.setEndpoint("file:///" + f.getCanonicalPath());
    FileUtils.write(f, TEXT, false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNotSame(TEXT, msg.getContent());
    InputStream stream = p.extract(msg);
    byte[] result = new byte[TEXT.length()];
    stream.read(result);
    assertEquals(TEXT, new String(result));
  }

  @Test
  public void testExtractFromClasspath() throws Exception {
    FileInputStreamDataInputParameter p = new FileInputStreamDataInputParameter();
    p.setEndpoint("xstream-standalone.xml");
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
      FileInputStreamDataInputParameter p = new FileInputStreamDataInputParameter();
      p.setEndpoint(helper.createProduceDestination());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      assertNotNull(p.extract(msg));
    } finally {
      LifecycleHelper.stopAndClose(channel);
      helper.stopServer();
    }
  }
}
