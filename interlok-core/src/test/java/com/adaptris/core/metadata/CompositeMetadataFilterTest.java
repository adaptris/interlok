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
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataCollection;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class CompositeMetadataFilterTest {

  public CompositeMetadataFilterTest() {
  }

  @Test
  public void testFilter() {
    CompositeMetadataFilter filterer = new CompositeMetadataFilter(new NoOpMetadataFilter(), new RegexMetadataFilter());
    AdaptrisMessage message = newMessage();
    MetadataCollection resultingCollection = filterer.filter(message);
    assertEquals(message.getMetadata().size(), resultingCollection.size());
  }

  @Test
  public void testRoundTrip() throws Exception {
    CompositeMetadataFilter f1 = new CompositeMetadataFilter(new NoOpMetadataFilter(), new RegexMetadataFilter());
    AdaptrisMarshaller cm = DefaultMarshaller.getDefaultMarshaller();
    CompositeMetadataFilter f2 = (CompositeMetadataFilter) cm.unmarshal(cm.marshal(f1));
    BaseCase.assertRoundtripEquality(f1, f2);
  }

  private AdaptrisMessage newMessage() {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
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
