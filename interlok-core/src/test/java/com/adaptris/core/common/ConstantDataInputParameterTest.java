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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class ConstantDataInputParameterTest {

  private static final String TEXT = "Hello World";

  @Test
  public void testValue() {
    ConstantDataInputParameter p = new ConstantDataInputParameter(TEXT);
    assertEquals(TEXT, p.getValue());
    p.setValue(null);
    assertNull(p.getValue());
  }
  
  @Test
  public void testExtract() throws Exception {
    ConstantDataInputParameter p = new ConstantDataInputParameter(TEXT);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, p.extract(msg));
  }
}
