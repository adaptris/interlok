package com.adaptris.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Set;

public class FormattedMetadataDestinationTest extends ExampleProduceDestinationCase {

  public FormattedMetadataDestinationTest(java.lang.String testName) {
    super(testName);
  }

  public void testAddMetadataKey() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.addMetadataKey("key2");
    dest.addMetadataKey("key3");
    assertTrue(dest.getMetadataKeys().contains("key3"));
  }

  public void testGetMetadataKeys() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addMetadataKey("key1");
    dest.addMetadataKey("key2");
    List<String> keys = new ArrayList<>();
    keys.add("key1");
    keys.add("key2");

    assertTrue(dest.getMetadataKeys().equals(keys));
  }

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

  public void testAddObjectMetadataKey() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addObjectMetadataKey("key1");
    dest.addObjectMetadataKey("key2");
    dest.addObjectMetadataKey("key3");
    assertTrue(dest.getObjectMetadataKeys().contains("key3"));
  }

  public void testGetObjectMetadataKeys() {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addObjectMetadataKey("key1");
    dest.addObjectMetadataKey("key2");
    List<String> keys = new ArrayList<>();
    keys.add("key1");
    keys.add("key2");

    assertTrue(dest.getObjectMetadataKeys().equals(keys));
  }

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

  public void testGetDestination_NoConfig() throws CoreException {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload");
    assertNull(dest.getDestination(msg));
  }

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

  public void testGetDestination_ObjectMetadata_Null() throws CoreException {
    FormattedMetadataDestination dest = new FormattedMetadataDestination();
    dest.addObjectMetadataKey("timestamp");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload");
    assertNull(dest.getDestination(msg));
  }

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
