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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class MetadataDataInputParameterTest {

  private static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testMetadataKey() throws Exception {
    MetadataDataInputParameter p = new MetadataDataInputParameter();
    assertEquals(MetadataDataInputParameter.DEFAULT_METADATA_KEY, p.getMetadataKey());
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
    MetadataDataInputParameter p = new MetadataDataInputParameter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader(MetadataDataInputParameter.DEFAULT_METADATA_KEY, TEXT);
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, p.extract(msg));
  }

}
