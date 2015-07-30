package com.adaptris.core;

import java.util.Date;

public class FormattedFilenameCreatorTest extends BaseCase {

  public FormattedFilenameCreatorTest(java.lang.String testName) {
    super(testName);
  }

  public void testSetFormat() {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    try {
      creator.setFilenameFormat(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(creator.getFilenameFormat(), "%1$s");
    try {
      creator.setFilenameFormat("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(creator.getFilenameFormat(), "%1$s");
    creator.setFilenameFormat("message");
    assertEquals("message", creator.getFilenameFormat());
  }

  public void testPlain() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = msg.getUniqueId();
    String fileName = creator.createName(msg);
    assertEquals(fileName, expectedName);
  }

  public void testWithTimestamp() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    creator.setFilenameFormat("%1$s-%2$tF");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = String.format("%1$s-%2$tF", msg.getUniqueId(), new Date());
    String fname = creator.createName(msg);
    assertEquals(expectedName, fname);
  }

  public void testWithConstants() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    creator.setFilenameFormat("message-%1$s-%2$tF");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = String.format("message-%1$s-%2$tF", msg.getUniqueId(), new Date());
    String fname = creator.createName(msg);
    assertEquals(expectedName, fname);
  }

  public void testWithoutUniqueid() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    creator.setFilenameFormat("message-%2$tF");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = String.format("message-%2$tF", msg.getUniqueId(), new Date());
    String fname = creator.createName(msg);
    assertEquals(expectedName, fname);
  }

  public void testReversedOrder() throws Exception {
    FormattedFilenameCreator creator = new FormattedFilenameCreator();
    creator.setFilenameFormat("%2$tF-%1$s");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = String.format("%2$tF-%1$s", msg.getUniqueId(), new Date());
    String fname = creator.createName(msg);
    assertEquals(expectedName, fname);
  }

  public void testXmlRoundTrip() throws Exception {
    FormattedFilenameCreator input = new FormattedFilenameCreator();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    FormattedFilenameCreator output = (FormattedFilenameCreator) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }

}
