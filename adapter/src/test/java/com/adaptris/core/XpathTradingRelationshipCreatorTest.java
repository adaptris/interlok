package com.adaptris.core;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class XpathTradingRelationshipCreatorTest extends BaseCase {

  private static final String TYPE = "type";
  private static final String DEST = "dest";
  private static final String SRC = "src";
  private static final String SRC_XPATH = "/document/source";
  private static final String DEST_XPATH = "/document/destination";
  private static final String TYPE_XPATH = "/document/type";
  private static final String BAD_XPATH = "/document/Fred";
  private static final String EMPTY_XPATH = "/document/empty";

  private static final String XML_PAYLOAD = "<?xml version=\"1.0\"?>" + "<document>" + "<source>" + SRC + "</source>"
      + "<destination>" + DEST + "</destination>" + "<type>" + TYPE + "</type>" + "<empty></empty></document>";

  public XpathTradingRelationshipCreatorTest(String arg0) {
    super(arg0);
  }

  public void testSetDestinationXpath() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator();
    try {
      creator.setDestinationXpath("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setDestinationXpath(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setDestinationXpath(DEST_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testSetSourceXpath() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator();
    try {
      creator.setSourceXpath("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setSourceXpath(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setSourceXpath(SRC_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testSetTypeXpath() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator();
    try {
      creator.setTypeXpath("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setTypeXpath(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setTypeXpath(TYPE_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testCreateDefault() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator();
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testCreate() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator(SRC_XPATH, DEST_XPATH, TYPE_XPATH);
    assertNotNull(creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD)));
    TradingRelationship rel = creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
    assertEquals(SRC, rel.getSource());
    assertEquals(DEST, rel.getDestination());
    assertEquals(TYPE, rel.getType());
    assertEquals(new TradingRelationship(SRC, DEST, TYPE).toString(), new TradingRelationship(SRC, DEST, TYPE), rel);
  }

  public void testInvalidSourceXpath() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator(SRC_XPATH, DEST_XPATH, TYPE_XPATH);
    creator.setSourceXpath(BAD_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
    creator.setSourceXpath(EMPTY_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testInvalidDestinationXpath() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator(SRC_XPATH, DEST_XPATH, TYPE_XPATH);
    creator.setDestinationXpath(BAD_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
    creator.setDestinationXpath(EMPTY_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testInvalidTypeXpath() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator(SRC_XPATH, DEST_XPATH, TYPE_XPATH);
    creator.setTypeXpath(BAD_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
    creator.setTypeXpath(EMPTY_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage(XML_PAYLOAD));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testNonXmlDocument() throws Exception {
    XpathTradingRelationshipCreator creator = new XpathTradingRelationshipCreator(SRC_XPATH, DEST_XPATH, TYPE_XPATH);
    try {
      creator.create(new DefaultMessageFactory().newMessage("ABCDEFG"));
      fail();
    }
    catch (CoreException expected) {
    }
  }


  public void testXmlRoundTrip() throws Exception {
    XpathTradingRelationshipCreator input = new XpathTradingRelationshipCreator(SRC_XPATH, DEST_XPATH, TYPE_XPATH);
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    XpathTradingRelationshipCreator output = (XpathTradingRelationshipCreator) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }

  public void testSetNamespaceContext() {
    XpathTradingRelationshipCreator obj = new XpathTradingRelationshipCreator();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

}
