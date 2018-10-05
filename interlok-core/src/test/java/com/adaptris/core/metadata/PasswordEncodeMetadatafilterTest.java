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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.security.password.Password;

public class PasswordEncodeMetadatafilterTest {


  @Test
  public void testFilter() throws Exception {
    PasswordEncodeMetadataFilter filter = new PasswordEncodeMetadataFilter().withStyle(Password.PORTABLE_PASSWORD)
        .withPatterns("^.*password.*$");
    AdaptrisMessage msg = createMessage();
    MetadataCollection filtered = filter.filter(msg);
    assertEquals(3, filtered.size());
    // Changes not reflected in the underlying message.
    assertEquals("password1", msg.getMetadataValue("passwordKey1"));
    assertEquals("password2", msg.getMetadataValue("passwordKey2"));
    assertEquals("value1", msg.getMetadataValue("key1"));
    assertPasswords(filtered);
  }

  private void assertPasswords(MetadataCollection collection) {
    for (MetadataElement e : collection) {
      if ("passwordKey1".equalsIgnoreCase(e.getKey())) {
        assertNotSame("password1", e.getValue());
        assertTrue(e.getValue().startsWith("PW:"));
      }
      if ("passwordKey2".equalsIgnoreCase(e.getKey())) {
        assertNotSame("password2", e.getValue());
        assertTrue(e.getValue().startsWith("PW:"));
      }
    }
  }

  private AdaptrisMessage createMessage() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("passwordKey1", "password1");
    message.addMetadata("passwordKey2", "password2");
    message.addMetadata("key1", "value1");
    return message;
  }
}
