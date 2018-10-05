/*
 * Copyright 2015 Adaptris Ltd.
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

package com.adaptris.core.services.findreplace;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class MetadataReplacementSourceTest {

  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
    msg.addMetadata("key", "val");
    return msg;
  }

  @Test
  public void testNullMessage() throws Exception {
    MetadataReplacementSource source = new MetadataReplacementSource();
    try {
      source.obtainValue(null);
      fail("null did not throw Exception handled correctly");
    }
    catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testMissingKey() throws Exception {
    MetadataReplacementSource source = new MetadataReplacementSource();
    source.setValue("not-there");
    
    String replaceWith = source.obtainValue(createMessage());
    assertTrue(null == replaceWith);
  }

  @Test
  public void testValidKey() throws Exception {
    MetadataReplacementSource source = new MetadataReplacementSource();
    source.setValue("key");
    String replaceWith = source.obtainValue(createMessage());
    assertTrue("val".equals(replaceWith));
  }
}
