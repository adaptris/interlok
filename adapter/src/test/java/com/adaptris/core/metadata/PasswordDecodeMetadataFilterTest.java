/*
 * Copyright Adaptris Ltd.
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

package com.adaptris.core.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.security.password.Password;

public class PasswordDecodeMetadataFilterTest {


  @Test
  public void testFilter() throws Exception {
    PasswordDecodeMetadataFilter filter = new PasswordDecodeMetadataFilter().withPatterns("^.*password.*$");
    AdaptrisMessage msg = createMessage();
    MetadataCollection filtered = filter.filter(msg);
    assertEquals(3, filtered.size());
    // Changes not reflected in the underlying message.
    assertNotSame("password1", msg.getMetadataValue("passwordKey1"));
    assertNotSame("password2", msg.getMetadataValue("passwordKey2"));
    assertEquals("value1", msg.getMetadataValue("key1"));
    assertPasswords(filtered);
  }

  @Test
  public void testFilterWithException() throws Exception {
    PasswordDecodeMetadataFilter filter = new PasswordDecodeMetadataFilter().withPatterns("^.*password.*$");
    AdaptrisMessage msg = createMessage();
    msg.addMetadata("passwordKey3", "PW:Does_Not_Compute");
    try {
      MetadataCollection filtered = filter.filter(msg);
      fail();
    } catch (RuntimeException expected) {

    }
  }

  private void assertPasswords(MetadataCollection collection) {
    for (MetadataElement e : collection) {
      if ("passwordKey1".equalsIgnoreCase(e.getKey())) {
        assertEquals("password1", e.getValue());
      }
      if ("passwordKey2".equalsIgnoreCase(e.getKey())) {
        assertEquals("password2", e.getValue());
      }
    }
  }

  private AdaptrisMessage createMessage() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("passwordKey1", Password.encode("password1", Password.PORTABLE_PASSWORD));
    message.addMetadata("passwordKey2", Password.encode("password2", Password.PORTABLE_PASSWORD));
    message.addMetadata("key1", "value1");
    return message;
  }
}
