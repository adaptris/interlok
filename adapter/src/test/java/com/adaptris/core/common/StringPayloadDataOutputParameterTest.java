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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class StringPayloadDataOutputParameterTest {

  private static final String TEXT = "Hello World";
  private static final String UTF_8 = "UTF-8";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testContentEncoding() {
    StringPayloadDataOutputParameter p = new StringPayloadDataOutputParameter();
    assertNull(p.getContentEncoding());
    p.setContentEncoding(UTF_8);
    assertEquals(UTF_8, p.getContentEncoding());
    p.setContentEncoding(null);
    assertNull(p.getContentEncoding());
  }

  @Test
  public void testInsert_NoEncoding() throws Exception {
    StringPayloadDataOutputParameter p = new StringPayloadDataOutputParameter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    p.insert(TEXT, msg);
    assertEquals(TEXT, msg.getContent());
    assertNull(msg.getContentEncoding());
  }


  @Test
  public void testInsert_Encoding() throws Exception {
    StringPayloadDataOutputParameter p = new StringPayloadDataOutputParameter();
    p.setContentEncoding(UTF_8);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    p.insert(TEXT, msg);
    assertEquals(TEXT, msg.getContent());
    assertEquals(UTF_8, msg.getContentEncoding());
  }

}
