/*
 * Copyright 2020 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.adaptris.core.common;

import static com.adaptris.core.common.MetadataDataOutputParameter.DEFAULT_METADATA_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.util.text.Base64ByteTranslator;

public class MetadataStreamOutputTest {
  private static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testMetadataKey() throws Exception {
    MetadataStreamOutput p = new MetadataStreamOutput();
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
  public void testInsert_DefaultTranslator() throws Exception {
    MetadataStreamOutput p = new MetadataStreamOutput().withMetadataKey(DEFAULT_METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try (ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes()); OutputStream out = p.wrap(msg)) {
      IOUtils.copy(in, out);
    }
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testInsert_Encoding() throws Exception {
    String base64 = Base64.getEncoder().encodeToString(TEXT.getBytes());
    MetadataStreamOutput p = new MetadataStreamOutput().withTranslator(new Base64ByteTranslator());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try (ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes()); OutputStream out = p.wrap(msg)) {
      IOUtils.copy(in, out);
    }
    assertNotSame(base64, msg.getContent());
    assertEquals(base64, msg.getMetadataValue(DEFAULT_METADATA_KEY));
  }

}
