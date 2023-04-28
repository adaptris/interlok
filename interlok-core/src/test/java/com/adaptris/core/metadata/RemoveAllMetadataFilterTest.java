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
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataCollection;

public class RemoveAllMetadataFilterTest {


  @Test
  public void testFilterMessage() throws Exception {
    RemoveAllMetadataFilter filter = new RemoveAllMetadataFilter();
    MetadataCollection c = filter.filter(createMessage());
    assertEquals(0, c.size());
  }

  @Test
  public void testFilterSet() throws Exception {
    RemoveAllMetadataFilter filter = new RemoveAllMetadataFilter();
    MetadataCollection c = filter.filter(createMessage().getMetadata());
    assertEquals(0, c.size());
  }

  @Test
  public void testFilterCollection() throws Exception {
    RemoveAllMetadataFilter filter = new RemoveAllMetadataFilter();
    MetadataCollection c = filter.filter(new MetadataCollection(createMessage().getMetadata()));
    assertEquals(0, c.size());
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("key1", "value1");
    message.addMetadata("key2", "value2");
    message.addMetadata("key3", "value3");
    message.addMetadata("key4", "value4");
    message.addMetadata("key5", "value5");

    message.addMetadata("someRandomKey", "Some random value");
    message.addMetadata("JackAndJill", "Ran up some hill");
    message.addMetadata("JillAndJack", "Broke their backs");
    return message;
  }
}
