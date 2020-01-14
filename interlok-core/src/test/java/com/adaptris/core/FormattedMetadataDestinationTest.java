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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.adaptris.core.metadata.ElementFormatter;
import com.adaptris.core.metadata.ElementKeyAndValueFormatter;

public class FormattedMetadataDestinationTest extends ExampleProduceDestinationCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testAddMetadataKey() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.addMetadataKey("key2");
    dest.addMetadataKey("key3");
    assertTrue(dest.getMetadataKeys().contains("key3"));
  }

  @Test
  public void testGetMetadataKeys() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.addMetadataKey("key2");
    List<String> keys = new ArrayList<>();
    keys.add("key1");
    keys.add("key2");

    assertTrue(dest.getMetadataKeys().equals(keys));
  }

  @Test
  public void testSetMetadataKeys() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.addMetadataKey("key2");
    List<String> keys = new ArrayList<>();
    keys.add("key1");
    keys.add("key2");
    dest.setMetadataKeys(keys);
    assertTrue(dest.getMetadataKeys().equals(keys));
    try {
      dest.setMetadataKeys(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testAddObjectMetadataKey() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addObjectMetadataKey("key1");
    dest.addObjectMetadataKey("key2");
    dest.addObjectMetadataKey("key3");
    assertTrue(dest.getObjectMetadataKeys().contains("key3"));
  }

  @Test
  public void testGetObjectMetadataKeys() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addObjectMetadataKey("key1");
    dest.addObjectMetadataKey("key2");
    List<String> keys = new ArrayList<>();
    keys.add("key1");
    keys.add("key2");

    assertTrue(dest.getObjectMetadataKeys().equals(keys));
  }

  @Test
  public void testSetObjectMetadataKeys() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    List<String> keys = new ArrayList<>();
    keys.add("key1");
    keys.add("key2");
    dest.setObjectMetadataKeys(keys);
    assertTrue(dest.getObjectMetadataKeys().equals(keys));
    try {
      dest.setObjectMetadataKeys(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testSetDestinationTemplate() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    assertEquals("", dest.getDestinationTemplate());
    try {
      dest.setDestinationTemplate(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
      
    }
    dest.setDestinationTemplate("Hello World");
    assertEquals("Hello World", dest.getDestinationTemplate());
  }

  @Test
  public void testGetDestination_NoConfig() throws CoreException {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload");
    assertNull(dest.getDestination(msg));
  }

  @Test
  public void testGetDestination_Metadata() throws CoreException {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.addMetadataKey("key2");
    dest.setDestinationTemplate("/%1$s/%2$s");
    Set<MetadataElement> metadata = new HashSet<>();
    metadata.add(new MetadataElement("key1", "val1"));
    metadata.add(new MetadataElement("key2", "val2"));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload", metadata);

    assertTrue("/val1/val2".equals(dest.getDestination(msg)));
  }

  @Test
  public void testGetDestination_Metadata_Null() throws CoreException {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.addMetadataKey("key2");
    dest.setDestinationTemplate("/%1$s/%2$s");
    Set<MetadataElement> metadata = new HashSet<>();
    metadata.add(new MetadataElement("key1", "val1"));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload", metadata);

    assertTrue("/val1/".equals(dest.getDestination(msg)));
  }

  @Test
  public void testGetDestination_ObjectMetadata() throws CoreException {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.setDestinationTemplate("%1$tF");
    Date d = new Date();
    String expectedDate = String.format("%1$tF", d);
    dest.addObjectMetadataKey("timestamp");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload");
    msg.addObjectHeader("timestamp", d);
    assertEquals(expectedDate, dest.getDestination(msg));
  }

  @Test
  public void testGetDestination_ObjectMetadata_Null() throws CoreException {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addObjectMetadataKey("timestamp");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload");
    assertNull(dest.getDestination(msg));
  }

  @Test
  public void testGetDestination_MetadataAndObjectMetadata() throws CoreException {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    Date d = new Date();
    String expectedValue = String.format("/val1/%1$tF", d);
    dest.addMetadataKey("key1");
    dest.addObjectMetadataKey("timestamp");
    dest.setDestinationTemplate("/%1$s/%2$tF");
    Set<MetadataElement> metadata = new HashSet<>();
    metadata.add(new MetadataElement("key1", "val1"));
    metadata.add(new MetadataElement("key2", "val2"));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload", metadata);
    msg.addObjectHeader("timestamp", d);
    assertTrue(expectedValue.equals(dest.getDestination(msg)));

  }

  @Test
  public void testGetDestination_BadFormat() throws Exception {
    // Here we pass in just normal metadata, but we have an error, whereby we reference the $tF to format
    // a String.
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.addMetadataKey("key2");
    dest.setDestinationTemplate("/%1$s/%2$tF");
    Set<MetadataElement> metadata = new HashSet<>();
    metadata.add(new MetadataElement("key1", "val1"));
    metadata.add(new MetadataElement("key2", "val2"));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload", metadata);
    try {
      dest.getDestination(msg);
      fail();
    }
    catch (IllegalFormatConversionException expected) {

    }
  }

  @Test
  public void testElementFormatter() throws Exception {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.setDestinationTemplate("%1$s");
    Set<MetadataElement> metadata = new HashSet<>();
    metadata.add(new MetadataElement("key1", "val1"));
    ElementFormatter ef = new ElementKeyAndValueFormatter();
    dest.setElementFormatter(ef);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload", metadata);
    assertEquals("key1=val1", dest.getDestination(msg));
  }
  
  @Test
  public void testElementFormatterSeparator() throws Exception {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.setDestinationTemplate("%1$s");
    Set<MetadataElement> metadata = new HashSet<>();
    metadata.add(new MetadataElement("key1", "val1"));
    ElementKeyAndValueFormatter ef = new ElementKeyAndValueFormatter();
    ef.setSeparator(":");
    dest.setElementFormatter(ef);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload", metadata);
    assertEquals("key1:val1", dest.getDestination(msg));
  }

  @Override
  protected ProduceDestination createDestinationForExamples() {
    FormattedMetadataDestination input = new FormattedMetadataDestination();
    input.addMetadataKey("metadata_key1");
    input.addMetadataKey("metadata_key2");
    input.addMetadataKey("another metadata key");
    input.addObjectMetadataKey("Object Metadata Key Containing a Date");
    input.setDestinationTemplate("%1$s/%2$s/%4$tF/%3$s");
    return input;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\nThis ProduceDestination implementation derives its destination from metadata"
        + "\nFor each key that is configured, the values associated are formatted using the"
        + "\ndestination template via String.format(). Each of the values are passed in order; "
        + "\nwith object metadata keys appearing after standard metadata keys in the sequence."
        + "\nThis allows you to dynamically build up destinations using one or more metadata keys" + "\n\n-->\n";
  }

}
