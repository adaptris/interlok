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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

public class MetadataDataParameterTest
{
  private static final String TEXT = "Hello World";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testMetadataKey()
  {
    MetadataDataParameter p = new MetadataDataParameter();
    assertEquals(MetadataDataParameter.DEFAULT_METADATA_KEY, p.getMetadataKey());
    p.setMetadataKey("myKey");
    assertEquals("myKey", p.getMetadataKey());
    try
    {
      p.setMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException e)
    {
      /* do nothing; expected */
    }
    assertEquals("myKey", p.getMetadataKey());
  }

  @Test
  public void testExtractInsert()
  {
    MetadataDataParameter p = new MetadataDataParameter();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader(MetadataDataParameter.DEFAULT_METADATA_KEY, TEXT);
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, p.extract(msg));

    p.insert(TEXT, msg);
    assertNotSame(TEXT, msg.getContent());
    assertEquals(TEXT, msg.getMetadataValue(MetadataDataParameter.DEFAULT_METADATA_KEY));
  }
}
