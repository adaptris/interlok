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

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataCollection;

public class MetadataRegexKeyFilterTest extends TestCase {

  private RegexMetadataFilter filterer;
  private AdaptrisMessage message;

  @Override
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    addSomeMetadata(message);

    filterer = new RegexMetadataFilter();
  }

  @Override
  public void tearDown() throws Exception {
  }

  public void testNoIncludesOrExcludes() {
    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(message.getMetadata().size(), resultingCollection.size());
  }

  public void testIncludesOnly1() {
    filterer.addIncludePattern("key1");
    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(1, resultingCollection.size());
  }

  public void testIncludesOnly5Keys() {
    filterer.addIncludePattern("key");
    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(5, resultingCollection.size());
  }

  public void testIncludesOnly2Keys() {
    filterer.addIncludePattern("Jill");
    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(2, resultingCollection.size());
  }

  public void testExcludes5Keys() {
    filterer.addExcludePattern("key");
    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(3, resultingCollection.size());
    assertTrue(resultingCollection.containsKey("someRandomKey"));
    assertTrue(resultingCollection.containsKey("JackAndJill"));
    assertTrue(resultingCollection.containsKey("JillAndJack"));
  }

  public void testExcludes2Keys() {
    filterer.addExcludePattern("Jill");
    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(6, resultingCollection.size());
    assertTrue(resultingCollection.containsKey("key1"));
    assertTrue(resultingCollection.containsKey("key2"));
    assertTrue(resultingCollection.containsKey("key3"));
    assertTrue(resultingCollection.containsKey("key4"));
    assertTrue(resultingCollection.containsKey("key5"));
    assertTrue(resultingCollection.containsKey("someRandomKey"));
  }

  public void testIncludesKeyAndExcludes3() {
    filterer.addIncludePattern("key");
    filterer.addExcludePattern("3");

    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(4, resultingCollection.size());
    assertTrue(resultingCollection.containsKey("key1"));
    assertTrue(resultingCollection.containsKey("key2"));
    assertTrue(resultingCollection.containsKey("key4"));
    assertTrue(resultingCollection.containsKey("key5"));
  }

  public void testMultipleIncludes() {
    filterer.addIncludePattern("key");
    filterer.addIncludePattern("Jill");

    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(7, resultingCollection.size());
    assertFalse(resultingCollection.containsKey("someRandomKey"));
  }

  public void testMultipleExcludes() {
    filterer.addExcludePattern("key");
    filterer.addExcludePattern("Jill");

    MetadataCollection resultingCollection = filterer.filter(message);

    assertEquals(1, resultingCollection.size());
    assertTrue(resultingCollection.containsKey("someRandomKey"));
  }

  private void addSomeMetadata(AdaptrisMessage message) {
    message.addMetadata("key1", "value1");
    message.addMetadata("key2", "value2");
    message.addMetadata("key3", "value3");
    message.addMetadata("key4", "value4");
    message.addMetadata("key5", "value5");

    message.addMetadata("someRandomKey", "Some random value");
    message.addMetadata("JackAndJill", "Ran up some hill");
    message.addMetadata("JillAndJack", "Broke their backs");
  }
}
