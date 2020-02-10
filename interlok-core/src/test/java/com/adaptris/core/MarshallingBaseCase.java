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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterReader;
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
import org.junit.Before;
import org.junit.Test;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.URLString;
import com.adaptris.util.system.Os;

public abstract class MarshallingBaseCase extends BaseCase {

  private static final String TEST_DIR = "MarshallerTest.dir";

  protected File testOutputDir;

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void beforeMyTests() throws Exception {
    testOutputDir = new File(PROPERTIES.getProperty(TEST_DIR));
    testOutputDir.mkdirs();
  }


  protected abstract AdaptrisMarshaller createMarshaller() throws Exception;

  protected abstract String getClasspathXmlFilename();

  protected Adapter createMarshallingObject() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId("AdapterUniqueId");
    return a;
  }

  @Test
  public void testInlineRoundtrip() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String xml = marshaller.marshal(adapter);
    assertRoundtripEquality(adapter, marshaller.unmarshal(xml));
  }

  @Test
  public void testRoundTripToFilename() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    marshaller.marshal(adapter, f.getCanonicalPath());
    assertRoundtripEquality(adapter, marshaller.unmarshal(f));
  }

  @Test
  public void testRoundTripToFile() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    marshaller.marshal(adapter, f);
    assertRoundtripEquality(adapter, marshaller.unmarshal(f));
  }

  @Test
  public void testUnmarshalFromInputStream() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    try (ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes())) {
      assertRoundtripEquality(adapter, marshaller.unmarshal(in));
    }
  }

  @Test
  public void testUnmarshalFromInputStream_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    InputStream fail = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("testUnmarshalFromInputStream_WithException");
      }
    };
    try (InputStream in = fail) {
      Adapter adapter2 = (Adapter) marshaller.unmarshal(in);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      assertRootCause("testUnmarshalFromInputStream_WithException", e);
    }
  }

  @Test
  public void testUncheckedUnmarshalFromInputStream() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    assertRoundtripEquality(adapter, AdaptrisMarshaller.uncheckedUnmarshal(marshaller, adapter, () -> {
      return new ByteArrayInputStream(s.getBytes());
    }));
  }

  @Test
  public void testUncheckedUnmarshalFromInputStream_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    try {
      Object o = AdaptrisMarshaller.uncheckedUnmarshal(marshaller, adapter, () -> {
        return new InputStream() {
          @Override
          public int read() throws IOException {
            throw new IOException("testUncheckedUnmarshalFromInputStream_WithException");
          }

          @Override
          public int read(byte[] cbuf, int off, int len) throws IOException {
            throw new IOException("testUncheckedUnmarshalFromInputStream_WithException");
          }
        };

      });
      fail();
    } catch (RuntimeException expected) {

    }
  }

  @Test
  public void testUnmarshalFromReader() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    try (StringReader in = new StringReader(s)) {
      assertRoundtripEquality(adapter, marshaller.unmarshal(in));
    }
  }

  @Test
  public void testUnmarshalFromReader_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    String s = marshaller.marshal(adapter);
    Reader fail = new FilterReader(new StringReader(s)) {
      @Override
      public int read() throws IOException {
        throw new IOException("testUnmarshalFromReader_WithException");
      }
      @Override
      public int read(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("testUnmarshalFromReader_WithException");
      }
    };
    try (Reader in = fail) {
      Adapter adapter2 = (Adapter) marshaller.unmarshal(in);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      // assertEquals(IOException.class, e.getCause().getClass());
      assertRootCause("testUnmarshalFromReader_WithException", e);
    }
  }

  @Test
  public void testRoundTripToURL() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    marshaller.marshal(adapter, f.toURI().toURL());
    assertRoundtripEquality(adapter, marshaller.unmarshal(f.toURI().toURL()));
  }

  @Test
  public void testUnmarshalFromURLString() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    File f = new File(testOutputDir, new GuidGenerator().getUUID());
    marshaller.marshal(adapter, f);
    assertRoundtripEquality(adapter, marshaller.unmarshal(new URLString(f.toURI().toURL())));
  }

  @Test
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

  @Test
  public void testMarshalToOutputStream() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      marshaller.marshal(adapter, out);
    }
  }

  @Test
  public void testMarshalToOutputStream_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    OutputStream fail = new OutputStream() {
      @Override
      public void write(int c) throws IOException {
        throw new IOException("testMarshalToOutputStream_WithException");
      }
    };
    try (OutputStream out = fail) {
      marshaller.marshal(adapter, out);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      // assertEquals(IOException.class, e.getCause().getClass());
      assertRootCause("testMarshalToOutputStream_WithException", e);
    }
  }

  @Test
  public void testUncheckedMarshal_ToOutputStream() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    AdaptrisMarshaller.uncheckedMarshal(marshaller, adapter, () -> {
      return new ByteArrayOutputStream();
    });
  }

  @Test
  public void testUncheckedMarshal_ToOutputStreamWithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    try {
      AdaptrisMarshaller.uncheckedMarshal(marshaller, adapter, () -> {
        return new OutputStream() {
          @Override
          public void write(int c) throws IOException {
            throw new IOException("testUncheckedMarshal_ToOutputStreamWithException");
          }
        };
      });
      fail();
    } catch (RuntimeException expected) {

    }
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testMarshalToWriter() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    StringWriter out = new StringWriter();
    marshaller.marshal(adapter, out);
  }

  @Test
  public void testMarshalToWriter_WithException() throws Exception {
    AdaptrisMarshaller marshaller = createMarshaller();
    Adapter adapter = createMarshallingObject();
    Writer fail = new Writer() {

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("testMarshalToWriter_WithException");
      }

      @Override
      public void flush() throws IOException {
        throw new IOException("testMarshalToWriter_WithException");
      }

      @Override
      public void close() throws IOException {
      }
    };
    try (Writer out = fail) {
      marshaller.marshal(adapter, out);
      fail();
    }
    catch (CoreException e) {
      assertNotNull(e.getCause());
      // assertEquals(IOException.class, e.getCause().getClass());
      assertRootCause("testMarshalToWriter_WithException", e);
    }
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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
      assertTrue(IOException.class.isAssignableFrom(e.getCause().getClass()));
    }
  }

  @Test
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
