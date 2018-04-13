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

package com.adaptris.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdapterXStreamMarshallerFactory.OutputMode;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.URLString;
import com.adaptris.util.system.Os;

public abstract class MarshallingBaseCase extends BaseCase {

  private static final String TEST_DIR = "MarshallerTest.dir";

  protected File testOutputDir;

  public MarshallingBaseCase(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    testOutputDir = new File(PROPERTIES.getProperty(TEST_DIR));
    testOutputDir.mkdirs();
  }

  @Override
  protected void tearDown() throws Exception {
    // To avoid the situation where suddenly all the stuff is beautified...
    AdapterXStreamMarshallerFactory.getInstance().setMode(OutputMode.STANDARD);
  }

  protected abstract AdaptrisMarshaller createMarshaller() throws Exception;

  protected abstract String getClasspathXmlFilename();

  protected Adapter createMarshallingObject() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId("AdapterUniqueId");
    return a;
  }

  public void testInlineRoundtrip() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String xml = marshaller.marshal(adapter);
    assertRoundtripEquality(adapter, marshaller.unmarshal(xml));
  }

  public void testRoundTripToFilename() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    marshaller.marshal(adapter, f.getCanonicalPath());
    assertRoundtripEquality(adapter, marshaller.unmarshal(f));
  }

  public void testRoundTripToFile() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    marshaller.marshal(adapter, f);
    assertRoundtripEquality(adapter, marshaller.unmarshal(f));
  }

  public void testUnmarshalFromInputStream() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
    assertRoundtripEquality(adapter, marshaller.unmarshal(in));
    in.close();
  }

  public void testUnmarshalFromInputStream_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    InputStream in = new FilterInputStream(new ByteArrayInputStream(s.getBytes())) {
      @Override
      public int read() throws IOException {
        throw new IOException("testUnmarshalFromInputStream_WithException");
      }

      @Override
      public int read(byte[] cbuf, int off, int len) throws IOException {
        throw new IOException("testUnmarshalFromInputStream_WithException");
      }
    };
    try {
      Adapter adapter2 = (Adapter) marshaller.unmarshal(in);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      assertRootCause("testUnmarshalFromInputStream_WithException", e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }

  public void testUnmarshalFromReader() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    StringReader in = new StringReader(s);
    assertRoundtripEquality(adapter, marshaller.unmarshal(in));
    in.close();
  }

  public void testUnmarshalFromReader_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    Reader in = new FilterReader(new StringReader(s)) {
      @Override
      public int read() throws IOException {
        throw new IOException("testUnmarshalFromReader_WithException");
      }
      @Override
      public int read(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("testUnmarshalFromReader_WithException");
      }
    };
    try {
      Adapter adapter2 = (Adapter) marshaller.unmarshal(in);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      // assertEquals(IOException.class, e.getCause().getClass());
      assertRootCause("testUnmarshalFromReader_WithException", e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }

  public void testRoundTripToURL() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    marshaller.marshal(adapter, f.toURI().toURL());
    assertRoundtripEquality(adapter, marshaller.unmarshal(f.toURI().toURL()));
  }

  public void testUnmarshalFromURLString() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    marshaller.marshal(adapter, f);
    assertRoundtripEquality(adapter, marshaller.unmarshal(new URLString(f.toURI().toURL())));
  }

  public void testUnmarshalWithTransient() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Channel channel = new Channel();
    // availability starts off as "true"
    channel.toggleAvailability(false);
    assertEquals(false, channel.isAvailable());
    String xml = marshaller.marshal(channel);
    Channel c2 = (Channel) marshaller.unmarshal(xml);
    // availability should still as per constructor if it's marked as transient
    assertNotSame(false, c2.isAvailable());
  }

  public void testMarshalToOutputStream() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(adapter, out);
  }

  public void testMarshalToOutputStream_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    OutputStream out = new FilterOutputStream(new ByteArrayOutputStream()) {

      @Override
      public void write(byte[] cbuf, int off, int len) throws IOException {
        throw new IOException("testMarshalToOutputStream_WithException");
      }

      @Override
      public void write(int c) throws IOException {
        throw new IOException("testMarshalToOutputStream_WithException");
      }

      @Override
      public void flush() throws IOException {
        throw new IOException("testMarshalToOutputStream_WithException");
      }
    };
    try {
      marshaller.marshal(adapter, out);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      // assertEquals(IOException.class, e.getCause().getClass());
      assertRootCause("testMarshalToOutputStream_WithException", e);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }

  public void testMarshalToFile_NonExistent() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File dir = File.createTempFile("pfx", ".sfx");
    File nonExistent = new File(dir, new GuidGenerator().getUUID());
    try {
      marshaller.marshal(adapter, nonExistent);
      fail();
    }
    catch (CoreException expected) {

    }
    finally {
      FileUtils.deleteQuietly(dir);
    }
  }

  public void testUnmarshalString_InvalidXML() throws Exception {
    String xml = "This isn't XML";
    AdaptrisMarshaller marshaller = createMarshaller();
    try {
      marshaller.unmarshal(xml);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testUnmarshal_NonExistent() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    File dir = File.createTempFile("pfx", ".sfx");
    File nonExistent = new File(dir, new GuidGenerator().getUUID());
    try {
      marshaller.unmarshal(nonExistent);
      fail();
    }
    catch (CoreException expected) {

    }
    finally {
      FileUtils.deleteQuietly(dir);
    }
  }

  public void testMarshalToWriter() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    StringWriter out = new StringWriter();
    marshaller.marshal(adapter, out);
  }

  public void testMarshalToWriter_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    Writer out = new FilterWriter(new StringWriter()) {

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("testMarshalToWriter_WithException");
      }

      @Override
      public void write(int c) throws IOException {
        throw new IOException("testMarshalToWriter_WithException");
      }

      @Override
      public void write(String str, int off, int len) throws IOException {
        throw new IOException("testMarshalToWriter_WithException");
      }

      @Override
      public void flush() throws IOException {
        throw new IOException("testMarshalToWriter_WithException");
      }
    };
    try {
      marshaller.marshal(adapter, out);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      // assertEquals(IOException.class, e.getCause().getClass());
      assertRootCause("testMarshalToWriter_WithException", e);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }

  public void testMarshalToURL() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    URL url = f.toURI().toURL();
    try {
      marshaller.marshal(null, (URL) null);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      marshaller.marshal(adapter, (URL) null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    try {
      marshaller.marshal(null, url);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    try {
      marshaller.marshal(adapter, new URL("http://development.adaptris.com/bugs"));
      fail();
    }
    catch (CoreException e) {
      assertEquals("URL protocol must be file:", e.getMessage());
    }
    marshaller.marshal(adapter, url);
  }

  public void testUnmarshalFromUrl() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();

    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    URL url = f.toURI().toURL();
    marshaller.marshal(adapter, url);
    try {
      marshaller.unmarshal((URL) null);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    Adapter adapter2 = (Adapter) marshaller.unmarshal(url);
    assertRoundtripEquality(adapter, adapter2);
  }

  public void testUnmarshalFromUrlWithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    // Ha, anonymous URLStreamHandler to the rescue.
    URL failingUrl = new URL("http", "development.adaptris.com", 80, "index.html", new URLStreamHandler() {
      @Override
      protected URLConnection openConnection(URL u) throws IOException {
        throw new IOException("testUnmarshalFromUrl");
      }
    });
    try {
      marshaller.unmarshal(failingUrl);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      assertEquals(IOException.class, e.getCause().getClass());
      assertEquals("testUnmarshalFromUrl", e.getCause().getMessage());
    }
  }

  public void testUnmarshalFromUrlStringClasspath() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();

    StandaloneProducer o1 = (StandaloneProducer) marshaller.unmarshal(new URLString(getClasspathXmlFilename()));
    assertEquals("unique-id", o1.getUniqueId());
    String unknownLoc = "zzzz-does-not-exist.xml";
    try {
      o1 = (StandaloneProducer) marshaller.unmarshal(new URLString(unknownLoc));
      fail("Success unmarshal of something that doesn't exist");
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      assertEquals(IOException.class, e.getCause().getClass());
    }
  }

  public void testUnmarshalFromUrlStringLocalFile() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();

    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    String fname = f.getCanonicalPath();
    if (Os.isFamily(Os.WINDOWS_FAMILY)) {
      // THis is juist to remove c: and leave us with \blah.
      fname = f.getCanonicalPath().substring(2).replaceAll("\\\\", "/");
    }
    marshaller.marshal(adapter, f);
    Adapter o2 = (Adapter) marshaller.unmarshal(new URLString(fname));
    assertRoundtripEquality(adapter, o2);
  }

  private void assertRootCause(String errMsg, Exception e) {
    Throwable rootCause = e;
    while (rootCause.getCause() != null) {
      rootCause = rootCause.getCause();
    }
    assertEquals(errMsg, rootCause.getMessage());
  }
}
