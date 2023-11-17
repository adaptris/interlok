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

import static com.adaptris.core.common.MetadataDataOutputParameter.DEFAULT_METADATA_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class MetadataStreamDataOutputParameterTest {
  private static final String UTF_8 = "UTF-8";

  private static final String TEXT = "Hello World";

  @Test
  public void testMetadataKey() throws Exception {
    MetadataStreamOutputParameter p = new MetadataStreamOutputParameter();
    assertEquals(DEFAULT_METADATA_KEY, p.getMetadataKey());
    p.setMetadataKey("myKey");
    assertEquals("myKey", p.getMetadataKey());
    try {
      p.setMetadataKey("");
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals("myKey", p.getMetadataKey());
  }

  @Test
  public void testContentEncoding() {
    MetadataStreamOutputParameter p = new MetadataStreamOutputParameter();
    assertNull(p.getContentEncoding());
    p.setContentEncoding(UTF_8);
    assertEquals(UTF_8, p.getContentEncoding());
    p.setContentEncoding(null);
    assertNull(p.getContentEncoding());
  }

  @Test
  public void testInsert_NoEncoding() throws Exception {
    MetadataStreamOutputParameter p = new MetadataStreamOutputParameter(DEFAULT_METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes());
    p.insert(new InputStreamWithEncoding(in, null), msg);
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testInsert_NullStream() throws Exception {
    // INTERLOK-3527
    MetadataStreamOutputParameter p = new MetadataStreamOutputParameter(DEFAULT_METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    p.insert(new InputStreamWithEncoding(null, null), msg);
    assertEquals("", msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testInsert_Encoding() throws Exception {
    MetadataStreamOutputParameter p = new MetadataStreamOutputParameter();
    p.setContentEncoding(UTF_8);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes(UTF_8));
    p.insert(new InputStreamWithEncoding(in, null), msg);
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testInsert_Broken() throws Exception {
    Assertions.assertThrows(CoreException.class, () -> {
      MetadataStreamOutputParameter p = new MetadataStreamOutputParameter();
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
      p.insert(new InputStreamWithEncoding(in, null), msg);
    });
  }

}
