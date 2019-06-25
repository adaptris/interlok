/*
 * Copyright 2016 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.adaptris.core.common;

import static com.adaptris.core.common.MetadataDataInputParameter.DEFAULT_METADATA_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class MetadataStreamDataInputParameterTest {
  private static final String METADATA_KEY = "myMetadataKey";
  private static final String UTF_8 = "UTF-8";
  private static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testMetadataKey() throws Exception {
    MetadataStreamInputParameter p = new MetadataStreamInputParameter();
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
  public void testExtract() throws Exception {
    MetadataStreamInputParameter p = new MetadataStreamInputParameter(METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(METADATA_KEY, TEXT);
    try (InputStream in = p.extract(msg)) {
      List<String> strings = IOUtils.readLines(in, Charset.defaultCharset());
      assertEquals(1, strings.size());
      assertEquals(TEXT, strings.get(0));
    }
  }

  @Test
  public void testExtract_WithContentEncoding() throws Exception {
    MetadataStreamInputParameter p =
        new MetadataStreamInputParameter().withMetadataKey(METADATA_KEY).withContentEncoding(UTF_8);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(METADATA_KEY, TEXT);
    try (InputStream in = p.extract(msg)) {
      List<String> strings = IOUtils.readLines(in, Charset.defaultCharset());
      assertEquals(1, strings.size());
      assertEquals(TEXT, strings.get(0));
    }
  }

  @Test
  public void testWrap() throws Exception {
    MetadataInputStreamWrapper p = new MetadataInputStreamWrapper(METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(METADATA_KEY, TEXT);
    try (InputStream in = msg.wrap(p)) {
      List<String> strings = IOUtils.readLines(in, Charset.defaultCharset());
      assertEquals(1, strings.size());
      assertEquals(TEXT, strings.get(0));
    }
  }

  @Test
  public void testWrap_WithContentEncoding() throws Exception {
    MetadataInputStreamWrapper p = new MetadataInputStreamWrapper().withMetadataKey(METADATA_KEY).withContentEncoding(UTF_8);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(METADATA_KEY, TEXT);
    try (InputStream in = msg.wrap(p)) {
      List<String> strings = IOUtils.readLines(in, Charset.defaultCharset());
      assertEquals(1, strings.size());
      assertEquals(TEXT, strings.get(0));
    }
  }

}
