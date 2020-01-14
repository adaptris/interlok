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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BaseCase;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataCollection;

public class MappedKeyMetadataFilterTest extends BaseCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testFilter() {
    MappedKeyMetadataFilter filterer = new MappedKeyMetadataFilter("^key(.*)$", "actual$1");
    AdaptrisMessage message = newMessage();
    MetadataCollection resultingCollection = filterer.filter(message);
    assertEquals(message.getMetadata().size(), resultingCollection.size());
    assertTrue(message.getMessageHeaders().containsKey("key1"));
    assertFalse(resultingCollection.containsKey("key1"));
    assertFalse(message.getMessageHeaders().containsKey("actual1"));
    assertTrue(resultingCollection.containsKey("actual1"));
  }

  @Test
  public void testFilterNoReplacement() {
    MappedKeyMetadataFilter filterer = new MappedKeyMetadataFilter("key", "");
    AdaptrisMessage message = newMessage();
    MetadataCollection resultingCollection = filterer.filter(message);
    assertEquals(message.getMetadata().size(), resultingCollection.size());
    assertTrue(message.getMessageHeaders().containsKey("key1"));
    assertFalse(resultingCollection.containsKey("key1"));
    assertFalse(message.getMessageHeaders().containsKey("1"));
    assertTrue(resultingCollection.containsKey("1"));
  }

  @Test
  public void testRoundTrip() throws Exception {
    MappedKeyMetadataFilter f1 = new MappedKeyMetadataFilter("^key(.*)$", "actual$1");
    AdaptrisMarshaller cm = DefaultMarshaller.getDefaultMarshaller();
    MappedKeyMetadataFilter f2 = (MappedKeyMetadataFilter) cm.unmarshal(cm.marshal(f1));
    assertRoundtripEquality(f1, f2);
  }

  private AdaptrisMessage newMessage() {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("key1", "value1");
    message.addMetadata("key2", "value2");
    message.addMetadata("key3", "value3");
    message.addMetadata("key4", "value4");
    message.addMetadata("key5", "value5");
    return message;
  }
}
