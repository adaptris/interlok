/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.lms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.stubs.TempFileUtils;

public abstract class StreamWrapperCase {

  private static final StreamWrapper.Callback NO_OP = () -> {
  };
  private static final String TEXT = "HELLO WORLD";
  private static final byte[] BYTES_TEXT = TEXT.getBytes();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testInputStream_Available() throws Exception {
    StreamWrapper wrapper = createWrapper(false);
    File file = writeFile(TempFileUtils.createTrackedFile(wrapper));
    InputStream in = wrapper.openInputStream(file, NO_OP);
    try (InputStream closeable = in) {
      assertTrue(closeable.available() > 0);
    }
  }

  @Test
  public void testInputStream_Mark() throws Exception {
    StreamWrapper wrapper = createWrapper(false);
    File file = writeFile(TempFileUtils.createTrackedFile(wrapper));
    InputStream in = wrapper.openInputStream(file, NO_OP);
    try (InputStream closeable = in) {
      tryQuietly(() -> {
        in.markSupported();
      });
      tryQuietly(() -> {
        in.mark(10);
      });
      tryQuietly(() -> {
        in.reset();
      });
    }
  }

  @Test
  public void testInputStream_Skip() throws Exception {
    StreamWrapper wrapper = createWrapper(false);
    File file = writeFile(TempFileUtils.createTrackedFile(wrapper));
    InputStream in = wrapper.openInputStream(file, NO_OP);
    try (InputStream closeable = in) {
      final long toSkip = BYTES_TEXT.length - 1;
      tryQuietly(() -> {
        long skipped = in.skip(toSkip);
        assertEquals(toSkip, skipped);
      });
    }
  }

  @Test
  public void testInputStream_Read() throws Exception {
    StreamWrapper wrapper = createWrapper(false);
    AtomicBoolean callback = new AtomicBoolean(false);
    File file = writeFile(TempFileUtils.createTrackedFile(wrapper));
    InputStream in = wrapper.openInputStream(file, () -> {
      callback.set(true);
    });
    try (InputStream closeable = in) {
      int read = 0;
      while(read != -1) {
        read = closeable.read();
      }
    }
    tryQuietly(() -> {
      in.close();
    });
    assertTrue(callback.get());
  }

  @Test
  public void testInputStream_Read_Byte() throws Exception {
    StreamWrapper wrapper = createWrapper(true);
    File file = writeFile(TempFileUtils.createTrackedFile(wrapper));
    byte[] bytes = new byte[16];
    try (InputStream in = wrapper.openInputStream(file, NO_OP)) {
      assertEquals(BYTES_TEXT.length, in.read(bytes));
    }
  }

  @Test
  public void testInputStream_Read_Byte_Len() throws Exception {
    StreamWrapper wrapper = createWrapper(true);
    File file = writeFile(TempFileUtils.createTrackedFile(wrapper));
    byte[] bytes = new byte[16];
    try (InputStream in = wrapper.openInputStream(file, NO_OP)) {
      int read = 0;
      assertEquals(BYTES_TEXT.length, in.read(bytes, 0, bytes.length));
    }
  }

  @Test
  public void testOutputStream_Write() throws Exception {
    StreamWrapper wrapper = createWrapper(false);
    File file = TempFileUtils.createTrackedFile(wrapper);
    AtomicBoolean callback = new AtomicBoolean(false);
    OutputStream out = wrapper.openOutputStream(file, () -> {
      callback.set(true);
    });
    try (OutputStream closeable = out) {
      out.write(BYTES_TEXT[0]);
    }
    tryQuietly(() -> {
      out.close();
    });
    assertTrue(callback.get());
  }

  @Test
  public void testOutputStream_Write_Bytes() throws Exception {
    StreamWrapper wrapper = createWrapper(true);
    File file = TempFileUtils.createTrackedFile(wrapper);
    try (OutputStream out = wrapper.openOutputStream(file, NO_OP)) {
      out.write(BYTES_TEXT);
    }
  }

  @Test
  public void testOutputStream_Write_Bytes_Len() throws Exception {
    StreamWrapper wrapper = createWrapper(true);
    File file = TempFileUtils.createTrackedFile(wrapper);
    try (OutputStream out = wrapper.openOutputStream(file, NO_OP)) {
      out.write(BYTES_TEXT, 0, BYTES_TEXT.length);
    }
  }

  @Test
  public void testOutputStream_Flush() throws Exception {
    StreamWrapper wrapper = createWrapper(true);
    File file = TempFileUtils.createTrackedFile(wrapper);
    try (OutputStream out = wrapper.openOutputStream(file, NO_OP)) {
      out.write(BYTES_TEXT, 0, BYTES_TEXT.length);
      out.flush();
    }
  }

  private File writeFile(File f) throws IOException {
    FileUtils.writeByteArrayToFile(f, BYTES_TEXT);
    return f;
  }

  protected abstract StreamWrapper createWrapper(boolean logging);

  protected void tryQuietly(Operation o) {
    try {
      o.apply();
    } catch (Exception ignored) {

    }
  }

  @FunctionalInterface
  protected interface Operation {
    void apply() throws Exception;
  }
}
