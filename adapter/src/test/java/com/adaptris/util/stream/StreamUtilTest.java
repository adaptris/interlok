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

package com.adaptris.util.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StreamUtilTest extends StreamUtil {

  private static final String TEXT = "The Quick Brown fox jumps over the lazy dog.";

  private transient Log logR;

  @Before
  public void setUp() throws Exception {
    logR = LogFactory.getLog(this.getClass());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateFile() throws Exception {
    int bytes = TEXT.length();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    File f = StreamUtil.createFile(in, bytes);
    assertNotNull(f);
    assertEquals(TEXT, new String(read(f)));
    FileUtils.deleteQuietly(f);
  }

  @Test
  public void testCreateFileInDirectory() throws Exception {
    int bytes = TEXT.length();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    File f = StreamUtil.createFile(in, bytes, System.getProperty("java.io.tmpdir"));
    assertNotNull(f);
    assertEquals(TEXT, new String(read(f)));
    FileUtils.deleteQuietly(f);
  }

  @Test
  public void testCreateFileInDirectory2() throws Exception {
    int bytes = TEXT.length();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    File f = StreamUtil.createFile(in, bytes, "");
    assertNotNull(f);
    assertEquals(TEXT, new String(read(f)));
    FileUtils.deleteQuietly(f);
  }

  @Test
  public void testCopyStreamNullInputs() throws Exception {
    StreamUtil.copyStream(null, null);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(null, out);
    assertEquals(0, out.toByteArray().length);
    StreamUtil.copyStream(new ByteArrayInputStream(TEXT.getBytes()), null);
    StreamUtil.copyStream(null, null, -1);
    StreamUtil.copyStream(null, null, 10);
    StreamUtil.copyStream(null, new ByteArrayOutputStream(), 10);
    StreamUtil.copyStream(new ByteArrayInputStream(TEXT.getBytes()), null, 10);
    StreamUtil.copyStream(new ByteArrayInputStream(TEXT.getBytes()), new ByteArrayOutputStream(), -1);
  }

  @Test
  public void testCopyStreamWithLength() throws Exception {
    int bytes = TEXT.length();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(in, out, bytes);
    assertEquals(TEXT, out.toString());
  }

  @Test
  public void testCopyStreamNoLength() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(in, out);
    assertEquals(TEXT, out.toString());
  }

  @Test
  public void testMakeCopy() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    InputStream in2 = StreamUtil.makeCopy(in);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtils.copy(in2, out);
    assertEquals(TEXT, out.toString());
  }

  @Test(expected = IOException.class)
  public void testCopyAndCloseStreams() throws Exception {
    int bytes = TEXT.length();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyAndClose(in, out);
    assertEquals(TEXT, out.toString());
    StreamUtil.copyAndClose(new ErroringInputStream(), new ByteArrayOutputStream());
  }

  @Test(expected = IOException.class)
  public void testCopyAndCloseWriter() throws Exception {
    int bytes = TEXT.length();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(out);
    StreamUtil.copyAndClose(in, writer);
    assertEquals(TEXT, out.toString());
    StreamUtil.copyAndClose(new ErroringInputStream(), new OutputStreamWriter(new ByteArrayOutputStream()));

  }

  private byte[] read(File f) throws IOException {
    FileInputStream in = null;
    byte[] results;
    try {
      in = new FileInputStream(f);
      results = new byte[in.available()];
      in.read(results);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return results;
  }

  public static class ErroringInputStream extends InputStream {

    public ErroringInputStream() {
    }

    @Override
    public int read() throws IOException {
      throw new IOException("Failed to read");
    }

    @Override
    public int read(byte[] b) throws IOException {
      throw new IOException("Failed to read");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      throw new IOException("Failed to read");
    }

  }
}
