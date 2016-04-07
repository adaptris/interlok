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

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class PayloadStreamOutputParameterTest {

  private static final String UTF_8 = "UTF-8";
  private static final String TEXT = "Hello World";

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
    p.insert(in, msg);
    assertEquals(TEXT, msg.getContent());
  }

  @Test
  public void testInsert_WithEncoding() throws Exception {
    PayloadStreamOutputParameter p = new PayloadStreamOutputParameter();
    p.setContentEncoding(UTF_8);
    assertEquals(UTF_8, p.getContentEncoding());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ByteArrayInputStream in = new ByteArrayInputStream(TEXT.getBytes(UTF_8));
    p.insert(in, msg);
    assertEquals(TEXT, msg.getContent());
    assertEquals(UTF_8, msg.getContentEncoding());
  }
}
