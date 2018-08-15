/*
 * Copyright 2017 Adaptris Ltd.
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

import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF16BE_BOM;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF16LE_BOM;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF32BE_BOM;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF32LE_BOM;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF8_BOM;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF_16_BE;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF_16_LE;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF_32_BE;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF_32_LE;
import static com.adaptris.util.stream.UnicodeDetectingInputStream.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class UnicodeDetectingInputStreamTest {
  
  private static final String ISO_8859_1 = "ISO-8859-1";

  @Test
  public void testDefaultEncoding() throws Exception {
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(roundTrip("hello", ISO_8859_1), ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      assertEquals(ISO_8859_1, stream.getEncoding());
      assertEquals(ISO_8859_1, stream.getEncoding());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testBrokenInput() throws Exception {
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(new StreamUtilTest.ErroringInputStream(),
        ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      stream.getEncoding();
    }
  }

  @Test
  public void testReadPastBom() throws Exception {
    // Write a UTF-8 BOM, but read the first byte, which means we're inited
    // So, consequently we can't figure out what BOM was written.
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(roundTrip("hello", UTF_8), ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      stream.read();
      assertNull(stream.getEncoding());
    }
  }

  @Test
  public void testRead_Methods() throws Exception {
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(roundTrip("hello", UTF_8), ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      stream.read();
      stream.read(new byte[1]);
      stream.read(new byte[10], 0, 10);
      assertNull(stream.getEncoding());
    }
  }

  @Test
  public void testUTF8() throws Exception {
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(roundTrip("hello", UTF_8), ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      assertEquals(UTF_8, stream.getEncoding());
    }
  }

  @Test
  public void testUTF16_LittleEnd() throws Exception {
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(roundTrip("hello", UTF_16_LE), ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      assertEquals(UTF_16_LE, stream.getEncoding());
    }
  }

  @Test
  public void testUTF16_BigEnd() throws Exception {
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(roundTrip("hello", UTF_16_BE), ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      assertEquals(UTF_16_BE, stream.getEncoding());
    }
  }

  @Test
  public void testUTF32_BigEnd() throws Exception {
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(roundTrip("hello", UTF_32_BE), ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      assertEquals(UTF_32_BE, stream.getEncoding());
    }
  }

  @Test
  public void testUTF32_LittleEnd() throws Exception {
    try (UnicodeDetectingInputStream stream = new UnicodeDetectingInputStream(roundTrip("hello", UTF_32_LE), ISO_8859_1)) {
      assertEquals(ISO_8859_1, stream.getDefaultEncoding());
      assertEquals(UTF_32_LE, stream.getEncoding());
    }
  }

  private OutputStreamWriter wrap(OutputStream out, String encoding) throws UnsupportedEncodingException, IOException {

    // According to some people UTF-16 + OutputStreamWriter will write a BOM...
    if (UTF_8.equals(encoding)) {
        out.write(UTF8_BOM, 0, UTF8_BOM.length);
    }
    else if (UTF_16_LE.equals(encoding)) {
      out.write(UTF16LE_BOM, 0, UTF16LE_BOM.length);
    }
    else if ("UTF-16".equals(encoding) || UTF_16_BE.equals(encoding)) {
      out.write(UTF16BE_BOM, 0, UTF16BE_BOM.length);
    }
    else if (UTF_32_LE.equals(encoding)) {
      out.write(UTF32LE_BOM, 0, UTF32LE_BOM.length);
    }
    else if ("UTF-32".equals(encoding) || UTF_32_BE.equals(encoding)) {
      out.write(UTF32BE_BOM, 0, UTF32BE_BOM.length);
    }
    return new OutputStreamWriter(out, encoding);
  }

  private InputStream roundTrip(String text, String encoding) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (OutputStreamWriter writer = wrap(out, encoding)) {
      writer.write(text);
    }
    return new ByteArrayInputStream(out.toByteArray());
  }

}
