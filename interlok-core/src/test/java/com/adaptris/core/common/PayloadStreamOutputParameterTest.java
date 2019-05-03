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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class PayloadStreamOutputParameterTest {

  private static final String UTF_8 = "UTF-8";
  private static final String TEXT = "Hello World";
  private static final byte[] BYTES = {-128, -127, -126, -125, -124, -123, -122, -121, -120, -119, -118, -117, -116, -115, -114, -113, -112, -111, -110, -109, -108, 
    -107, -106, -105, -104, -103, -102, -101, -100, -99, -98, -97, -96, -95, -94, -93, -92, -91, -90, -89, -88, -87, -86, -85, -84, -83, -82, -81, -80, -79, -78, -77, 
    -76, -75, -74, -73, -72, -71, -70, -69, -68, -67, -66, -65, -64, -63, -62, -61, -60, -59, -58, -57, -56, -55, -54, -53, -52, -51, -50, -49, -48, -47, -46, -45, 
    -44, -43, -42, -41, -40, -39, -38, -37, -36, -35, -34, -33, -32, -31, -30, -29, -28, -27, -26, -25, -24, -23, -22, -21, -20, -19, -18, -17, -16, -15, -14, -13, 
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
    30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 
    71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 
    110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127};
    
  @Test
  public void testContentEncoding() {
    PayloadStreamOutputParameter p = new PayloadStreamOutputParameter();
    assertNull(p.getContentEncoding());
    p.setContentEncoding(UTF_8);
    assertEquals(UTF_8, p.getContentEncoding());
    p.setContentEncoding(null);
    assertNull(p.getContentEncoding());
  }
  
  @Test
  public void testInsert_NoEncoding() throws Exception {
    PayloadStreamOutputParameter p = new PayloadStreamOutputParameter();
    assertNull(p.getContentEncoding());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    p.insert(new InputStreamWithEncoding(in, null), msg);
    assertEquals(TEXT, msg.getContent());
  }
  
  @Test
  public void testInsert_Binary() throws Exception {
    PayloadStreamOutputParameter p = new PayloadStreamOutputParameter();
    assertNull(p.getContentEncoding());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ByteArrayInputStream in = new ByteArrayInputStream(BYTES);
    p.insert(new InputStreamWithEncoding(in, null), msg);
    
    byte[] buffer = new byte[(int)msg.getSize()];
    IOUtils.readFully(msg.getInputStream(), buffer);
    assertEquals(BYTES.length, buffer.length);
    for(int i=0; i<buffer.length; i++) {
      assertEquals(BYTES[i], buffer[i]);
    }
    assertNull(msg.getContentEncoding());
  }

  @Test
  public void testInsert_WithEncoding() throws Exception {
    PayloadStreamOutputParameter p = new PayloadStreamOutputParameter();
    p.setContentEncoding(UTF_8);
    assertEquals(UTF_8, p.getContentEncoding());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes(UTF_8));
    p.insert(new InputStreamWithEncoding(in, null), msg);
    assertEquals(TEXT, msg.getContent());
    assertEquals(UTF_8, msg.getContentEncoding());
  }

  @Test(expected = CoreException.class)
  public void testInsert_BrokenInput() throws Exception {
    PayloadStreamOutputParameter p = new PayloadStreamOutputParameter();
    p.setContentEncoding(UTF_8);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    FilterInputStream in = new FilterInputStream(new ByteArrayInputStream(new byte[0])) {
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
    };
    p.insert(new InputStreamWithEncoding(in, UTF_8), msg);
  }

  @Test(expected = CoreException.class)
  public void testInsert_Broken_BinaryInput() throws Exception {
    PayloadStreamOutputParameter p = new PayloadStreamOutputParameter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    FilterInputStream in = new FilterInputStream(new ByteArrayInputStream(new byte[0])) {
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
    };
    p.insert(new InputStreamWithEncoding(in, null), msg);
  }

  @Test
  public void testWrap() throws Exception {
    PayloadStreamOutputParameter p = new PayloadStreamOutputParameter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try (OutputStream out = p.wrap(msg)) {

    }
  }
}
