package com.adaptris.core;

@SuppressWarnings("deprecation")
public class StandardFileNameCreatorTest extends BaseCase {

  public StandardFileNameCreatorTest(java.lang.String testName) {
    super(testName);
  }

  public void testSetPrefix() {
    StandardFileNameCreator creator = new StandardFileNameCreator();
    try {
      creator.setPrefix(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException e) {

    }
  }

  public void testSetSuffix() {
    StandardFileNameCreator creator = new StandardFileNameCreator();
    try {
      creator.setSuffix(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException e) {

    }
  }

  public void testPlain() throws Exception {
    StandardFileNameCreator creator = new StandardFileNameCreator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = msg.getUniqueId();
    String fileName = creator.createName(msg);
    assertEquals(fileName, expectedName);
  }

  public void testWithTimestamp() throws Exception {
    StandardFileNameCreator creator = new StandardFileNameCreator();
    creator.setAddTimeStamp(true);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String expectedName = msg.getUniqueId();
    String fileName = creator.createName(msg);
    assertTrue(fileName.length() > expectedName.length());
    assertTrue(fileName.startsWith(expectedName));
  }

  public void testWithPrefix() throws Exception {
    StandardFileNameCreator creator = new StandardFileNameCreator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String prefix = "prefix";
    String expectedName = prefix + msg.getUniqueId();
    creator.setPrefix(prefix);
    String fileName = creator.createName(msg);
    assertEquals(expectedName, fileName);
  }

  public void testWithSuffix() throws Exception {
    StandardFileNameCreator creator = new StandardFileNameCreator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String suffix = "suffix";
    String expectedName = msg.getUniqueId() + suffix;
    creator.setSuffix(suffix);
    String fileName = creator.createName(msg);
    assertEquals(expectedName, fileName);
  }

  public void testWithoutUniqueId() throws Exception {
    StandardFileNameCreator creator = new StandardFileNameCreator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    String prefix = "prefix";
    String suffix = "suffix";
    String expectedName = prefix + suffix;

    creator.setPrefix(prefix);
    creator.setSuffix(suffix);
    creator.setUseMessageUniqueId(false);
    String fileName = creator.createName(msg);
    assertEquals(expectedName, fileName);
  }

  public void testZeroLength() {
    StandardFileNameCreator creator = new StandardFileNameCreator();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    creator.setUseMessageUniqueId(false);
    try {
      creator.createName(msg);
      fail("no Excpetion for 0 length name");
    }
    catch (CoreException e) {
      // okay
    }
  }

  public void testXmlRoundTrip() throws Exception {
    StandardFileNameCreator input = new StandardFileNameCreator();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    StandardFileNameCreator output = (StandardFileNameCreator) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }

}
