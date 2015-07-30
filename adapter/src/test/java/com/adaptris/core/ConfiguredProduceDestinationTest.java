/*
 * $RCSfile: ConfiguredProduceDestinationTest.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/07/31 12:42:13 $
 * $Author: lchan $
 */
package com.adaptris.core;

public class ConfiguredProduceDestinationTest extends ExampleProduceDestinationCase {

  public ConfiguredProduceDestinationTest(java.lang.String testName) {
    super(testName);
  }

  public void testEquals() {
    ConfiguredProduceDestination dest1 = new ConfiguredProduceDestination("1");
    ConfiguredProduceDestination dest2 = new ConfiguredProduceDestination("1");
    ConfiguredProduceDestination dest3 = new ConfiguredProduceDestination("2");

    assertEquals(dest1, dest2);
    assertNotSame(dest1, dest3);
    assertFalse(dest1.equals(null));
    assertFalse(dest1.equals(new Object()));
  }

  public void testHashCode() {
    ConfiguredProduceDestination dest1 = new ConfiguredProduceDestination("1");
    ConfiguredProduceDestination dest2 = new ConfiguredProduceDestination("1");
    ConfiguredProduceDestination dest3 = new ConfiguredProduceDestination("2");
    assertEquals(dest1.hashCode(), dest2.hashCode());
    assertNotSame(dest1.hashCode(), dest3.hashCode());
  }

  public void testGetDestination() {
    ConfiguredProduceDestination dest1 = new ConfiguredProduceDestination("1");
    assertEquals("1", dest1.getDestination());
  }

  public void testSetDestination() {
    ConfiguredProduceDestination dest1 = new ConfiguredProduceDestination();
    dest1.setDestination("3");
    assertEquals("3", dest1.getDestination());
    try {
      dest1.setDestination(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
  }

  @Override
  public void testXmlRoundTrip() throws Exception {
    ConfiguredProduceDestination input = new ConfiguredProduceDestination("1");
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    ConfiguredProduceDestination output = (ConfiguredProduceDestination) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }

  @Override
  protected ProduceDestination createDestinationForExamples() {
    return new ConfiguredProduceDestination("The_Destination_String");
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object) + "<!--\n\nThis is the simplest ProduceDestination implementation"
        + "\nSimply configure a string that has some meaning for the producer in question and it will be used." + "\n\n-->\n";
  }

}