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

package com.adaptris.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetadataDestinationTest extends ExampleProduceDestinationCase {

  public MetadataDestinationTest(java.lang.String testName) {
    super(testName);
  }

  public void testAddKey() {
    MetadataDestination dest = new MetadataDestination();
    dest.addKey("key1");
    dest.addKey("key2");
    dest.addKey("key3");
    assertTrue(dest.getKeys().contains("key3"));
  }

  public void testGetKeys() {
    MetadataDestination dest = new MetadataDestination();
    dest.addKey("key1");
    dest.addKey("key2");
    List keys = new ArrayList();
    keys.add("key1");
    keys.add("key2");

    assertTrue(dest.getKeys().equals(keys));
  }

  public void testSetKeys() {
    MetadataDestination dest = new MetadataDestination();
    List keys = new ArrayList();
    keys.add("key1");
    keys.add("key2");
    dest.setKeys(keys);
    assertTrue(dest.getKeys().equals(keys));
    try {
      dest.setKeys(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testGetDestination() throws CoreException {
    MetadataDestination dest = new MetadataDestination();
    dest.addKey("key1");
    dest.addKey("key2");
    Set metadata = new HashSet();
    metadata.add(new MetadataElement("key1", "val1"));
    metadata.add(new MetadataElement("key2", "val2"));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload", metadata);

    assertTrue("val1val2".equals(dest.getDestination(msg)));
  }


  @Override
  protected ProduceDestination createDestinationForExamples() {
    MetadataDestination input = new MetadataDestination();
    input.addKey("metadata_key1");
    input.addKey("metadata_key2");
    input.addKey("another_metadata_key");
    input.addKey("yamk");
    return input;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\nThis ProduceDestination implementation derives its destination from metadata"
        + "\nFor each key that is configured, the value associated with each key will be"
        + "\nconcatenated together to create the destination."
        + "\nThis allows you to dynamically build up destinations using one or more metadata keys" + "\n\n-->\n";
  }

}
