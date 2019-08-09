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

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class XpathProduceDestinationTest extends ExampleProduceDestinationCase {

  private static final String DEFAULT_DEST = "default";
  private static final String DEST_XPATH = "/root/document";
  private static final String DEST_XPATH_WITH_FUNCTION = "local-name(/*)";
  private static final String LINE_SEP = System.lineSeparator();
  private static final String XML_DOC = "<root>" + LINE_SEP
      + "<document>value</document>" + LINE_SEP + "</root>" + LINE_SEP;

  public XpathProduceDestinationTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() {
  }

  public void testSetNamespaceContext() {
    XpathProduceDestination obj = new XpathProduceDestination();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  public void testEquals() {
    assertEquals(new XpathProduceDestination(), new XpathProduceDestination());
    XpathProduceDestination d1 = new XpathProduceDestination(DEST_XPATH);
    XpathProduceDestination d2 = new XpathProduceDestination(DEST_XPATH);
    assertFalse(d1.equals(null));
    assertNotSame(new XpathProduceDestination(), d1);
    assertEquals(d1, d2);
    assertEquals(d2, d1);
    XpathProduceDestination d3 = new XpathProduceDestination(DEST_XPATH, DEFAULT_DEST);
    XpathProduceDestination d4 = new XpathProduceDestination(DEST_XPATH);
    assertFalse(d3.equals(d4));
    assertFalse(d4.equals(d3));

    XpathProduceDestination d5 = new XpathProduceDestination(DEST_XPATH_WITH_FUNCTION, DEFAULT_DEST);
    XpathProduceDestination d6 = new XpathProduceDestination(DEST_XPATH, DEFAULT_DEST);
    assertFalse(d5.equals(d6));
    assertFalse(d6.equals(d5));

    XpathProduceDestination d7 = new XpathProduceDestination(DEST_XPATH_WITH_FUNCTION, "");
    XpathProduceDestination d8 = new XpathProduceDestination(DEST_XPATH, DEFAULT_DEST);
    assertFalse(d7.equals(d8));
    assertFalse(d8.equals(d7));

    assertFalse(new XpathProduceDestination().equals(new Object()));
  }

  public void testHashCode() {
    XpathProduceDestination dest = new XpathProduceDestination(DEST_XPATH, DEFAULT_DEST);
    XpathProduceDestination d2 = new XpathProduceDestination(DEST_XPATH, DEFAULT_DEST);
    assertEquals(new XpathProduceDestination().hashCode(), new XpathProduceDestination().hashCode());
    assertEquals(dest.hashCode(), d2.hashCode());
  }

  public void testSetDefaultDestination() {
    XpathProduceDestination dest = new XpathProduceDestination();
    try {
      dest.setDefaultDestination(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      ;
    }
    dest.setDefaultDestination("");
    assertEquals("", dest.getDefaultDestination());
    dest.setDefaultDestination(DEFAULT_DEST);
    assertEquals(DEFAULT_DEST, dest.getDefaultDestination());
  }

  public void testSetXpath() {
    XpathProduceDestination dest = new XpathProduceDestination();
    try {
      dest.setXpath(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      ;
    }
    dest.setXpath("");
    assertEquals("", dest.getXpath());
    dest.setXpath(DEST_XPATH);
    assertEquals(DEST_XPATH, dest.getXpath());
  }

  public void testValidXpathDestination() {
    XpathProduceDestination dest1 = new XpathProduceDestination(DEST_XPATH, DEFAULT_DEST);
    String s = dest1.getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOC));
    assertEquals("Value from Xpath", "value", s);
  }

  public void testValidXpathFunctionDestination() {
    XpathProduceDestination dest1 = new XpathProduceDestination(DEST_XPATH_WITH_FUNCTION, DEFAULT_DEST);
    String s = dest1.getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOC));
    assertEquals("Value from Xpath", "root", s);
  }

  public void testInvalidXpathDestination() {
    XpathProduceDestination dest1 = new XpathProduceDestination("ABCDE", DEFAULT_DEST);
    String s = dest1.getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOC));
    assertEquals("Value from Xpath", DEFAULT_DEST, s);
  }

  public void testInvalidXML() {
    XpathProduceDestination dest1 = new XpathProduceDestination(DEST_XPATH, DEFAULT_DEST);
    String s = dest1.getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEFG"));
    assertEquals(DEFAULT_DEST, s);
  }

  public void testXmlrRoundTrip() throws Exception {
    XpathProduceDestination dest1 = new XpathProduceDestination(DEST_XPATH, DEFAULT_DEST);
    Object input = dest1;
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    Object output = m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }

  @Override
  protected ProduceDestination createDestinationForExamples() {
    XpathProduceDestination dest = new XpathProduceDestination();
    dest.setXpath(DEST_XPATH_WITH_FUNCTION);
    dest.setDefaultDestination("The Default Destination If the XPath does not resolve");
    return dest;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\nThis ProduceDestination implementation derives its destination from an XML document"
        + "\nConfigure an XPath to retrieve some data from the document, and this will be used" + "\nas the destination."
        + "\nIf the XPath cannot be evaluated then the default destination will be used" + "\n\n-->\n";
  }

}
