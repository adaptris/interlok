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

package com.adaptris.core.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataCollection;

public class ExcludeJmsHeadersTest {


  @Test
  public void testFilter() {
    ExcludeJmsHeaders filter = new ExcludeJmsHeaders();
    AdaptrisMessage msg = addSomeMetadata(DefaultMessageFactory.getDefaultInstance().newMessage());
    int origSize = msg.getMetadata().size();
    MetadataCollection resultingCollection = filter.filter(msg);
    assertEquals(origSize - 2, resultingCollection.size());
  }

  private AdaptrisMessage addSomeMetadata(AdaptrisMessage message) {
    message.addMetadata("JMS_isMultiPart", "true");
    message.addMetadata("JMSMessageID", "1234");
    message.addMetadata("key1", "value2");
    message.addMetadata("key2", "value4");
    message.addMetadata("key3", "value5");
    return message;
  }
}
