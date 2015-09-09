package com.adaptris.core;

public class ConfiguredTradingRelationshipCreatorTest extends BaseCase {

  private static final String TYPE = "type";
  private static final String DEST = "dest";
  private static final String SRC = "src";


  public ConfiguredTradingRelationshipCreatorTest(String arg0) {
    super(arg0);
  }


  public void testSetDestination() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator();
    try {
      creator.setDestination("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setDestination(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setDestination(DEST);
    try {
      creator.create(new DefaultMessageFactory().newMessage());
      fail();
    }
    catch (IllegalArgumentException expected) {
      // expected from TradingRElationship
    }
  }

  public void testSetSource() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator();
    try {
      creator.setSource("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setSource(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setSource(SRC);
    try {
      creator.create(new DefaultMessageFactory().newMessage());
      fail();
    }
    catch (IllegalArgumentException expected) {
      // expected from TradingRElationship
    }
  }

  public void testSetType() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator();
    try {
      creator.setType("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setType(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setType(TYPE);
    try {
      creator.create(new DefaultMessageFactory().newMessage());
      fail();
    }
    catch (IllegalArgumentException expected) {
      // expected from TradingRElationship
    }
  }

  public void testCreateDefault() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator();
    try {
      creator.create(new DefaultMessageFactory().newMessage());
      fail();
    }
    catch (IllegalArgumentException expected) {
      // expected from TradingRElationship
    }
  }

  public void testCreate() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator(SRC, DEST, TYPE);
    assertNotNull(creator.create(new DefaultMessageFactory().newMessage()));
    TradingRelationship rel = creator.create(new DefaultMessageFactory().newMessage());
    assertEquals(SRC, rel.getSource());
    assertEquals(DEST, rel.getDestination());
    assertEquals(TYPE, rel.getType());
    assertEquals(new TradingRelationship(SRC, DEST, TYPE).toString(), new TradingRelationship(SRC, DEST, TYPE), rel);
  }

  public void testXmlRoundTrip() throws Exception {
    ConfiguredTradingRelationshipCreator input = new ConfiguredTradingRelationshipCreator(SRC, DEST, TYPE);
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    ConfiguredTradingRelationshipCreator output = (ConfiguredTradingRelationshipCreator) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }
}
