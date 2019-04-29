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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataCollection;

public class DiscardValuesTooLongFilterTest {


  @Test
  public void testFilterMessage() throws Exception {
    DiscardValuesTooLongFilter filter = new DiscardValuesTooLongFilter(36);
    MetadataCollection c = filter.filter(createMessage());
    assertEquals(4, c.size());
  }

  @Test
  public void testFilterSet() throws Exception {
    DiscardValuesTooLongFilter filter = new DiscardValuesTooLongFilter(36);
    MetadataCollection c = filter.filter(createMessage().getMetadata());
    assertEquals(4, c.size());
  }

  @Test
  public void testFilterCollection() throws Exception {
    DiscardValuesTooLongFilter filter = new DiscardValuesTooLongFilter(36);
    MetadataCollection c = filter.filter(new MetadataCollection(createMessage().getMetadata()));
    assertEquals(4, c.size());
  }

  @Test
  public void testFilterMessage_EmptyMetadata() throws Exception {
    DiscardValuesTooLongFilter filter = new DiscardValuesTooLongFilter(36);
    MetadataCollection c = filter.filter(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertEquals(0, c.size());
  }


  private AdaptrisMessage createMessage() {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("LENGTH_33", "Jived fox nymph grabs quick waltz");
    message.addMetadata("LENGTH_34", "Glib jocks quiz nymph to vex dwarf");
    message.addMetadata("LEGNTH_35", "How vexingly quick daft zebras jump");
    message.addMetadata("LENGTH_36", "Sphinx of black quartz, judge my vow");
    message.addMetadata("LENGTH_37", "Jackdaws love my big sphinx of quartz");
    message.addMetadata("LENGTH_39", "Pack my box with five dozen liquor jugs");
    message.addMetadata("LENGTH_43", "The quick brown fox jumps over the lazy dog");
    return message;
  }
}
