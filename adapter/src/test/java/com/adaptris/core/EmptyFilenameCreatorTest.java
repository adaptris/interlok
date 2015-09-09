package com.adaptris.core;


public class EmptyFilenameCreatorTest extends BaseCase {

  public EmptyFilenameCreatorTest(java.lang.String testName) {
    super(testName);
  }

  public void testCreateName() {
    EmptyFileNameCreator creator = new EmptyFileNameCreator();
    assertEquals("", creator.createName(new DefaultMessageFactory().newMessage("")));
    assertEquals("", creator.createName(null));
  }

  public void testXmlRoundTrip() throws Exception {
    EmptyFileNameCreator input = new EmptyFileNameCreator();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    EmptyFileNameCreator output = (EmptyFileNameCreator) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }
}
