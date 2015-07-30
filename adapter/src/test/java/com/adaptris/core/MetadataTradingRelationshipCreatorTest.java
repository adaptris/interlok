/*
 * $RCSfile: MetadataTradingRelationshipCreatorTest.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/08/13 13:28:43 $
 * $Author: lchan $
 */
package com.adaptris.core;

public class MetadataTradingRelationshipCreatorTest extends BaseCase {
  private static final String TYPE = "type";
  private static final String DEST = "dest";
  private static final String SRC = "src";

  public MetadataTradingRelationshipCreatorTest(String arg0) {
    super(arg0);
  }

  public void testSetDestinationKey() throws Exception {
    MetadataTradingRelationshipCreator creator = new MetadataTradingRelationshipCreator();
    try {
      creator.setDestinationKey("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setDestinationKey(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setDestinationKey(DEST);
    try {
      creator.create(new DefaultMessageFactory().newMessage(""));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testSetSourceKey() throws Exception {
    MetadataTradingRelationshipCreator creator = new MetadataTradingRelationshipCreator();
    try {
      creator.setSourceKey("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setSourceKey(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setSourceKey(SRC);
    try {
      creator.create(new DefaultMessageFactory().newMessage(""));
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testSetTypeKey() throws Exception {
    MetadataTradingRelationshipCreator creator = new MetadataTradingRelationshipCreator();
    try {
      creator.setTypeKey("");
      fail("'' allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      creator.setTypeKey(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
    creator.setTypeKey(TYPE);
    try {
      creator.create(new DefaultMessageFactory().newMessage());
      fail();
    }
    catch (CoreException expected) {
    }
  }

  public void testCreateFailsDueToConfig() throws Exception {
    MetadataTradingRelationshipCreator creator = new MetadataTradingRelationshipCreator();
    try {
      creator.create(new DefaultMessageFactory().newMessage());
      fail();
    }
    catch (CoreException expected) {
      // expected behaviour
    }
  }

  public void testMissingOrEmptySourceMetadata() throws Exception {
    MetadataTradingRelationshipCreator creator = new MetadataTradingRelationshipCreator(SRC, DEST, TYPE);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(DEST, DEST);
    msg.addMetadata(TYPE, TYPE);
    try {
      creator.create(msg);
      fail();
    }
    catch (CoreException e) {
    }
    msg.addMetadata(SRC, "");
    try {
      creator.create(msg);
      fail();
    }
    catch (CoreException e) {
    }
  }

  public void testMissingOrEmptyDestinationMetadata() throws Exception {
    MetadataTradingRelationshipCreator creator = new MetadataTradingRelationshipCreator(SRC, DEST, TYPE);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(SRC, SRC);
    msg.addMetadata(TYPE, TYPE);
    try {
      creator.create(msg);
      fail();
    }
    catch (CoreException e) {
    }
    msg.addMetadata(DEST, "");
    try {
      creator.create(msg);
      fail();
    }
    catch (CoreException e) {
    }
  }

  public void testMissingOrEmptyTypeMetadata() throws Exception {
    MetadataTradingRelationshipCreator creator = new MetadataTradingRelationshipCreator(SRC, DEST, TYPE);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(SRC, SRC);
    msg.addMetadata(DEST, DEST);
    try {
      creator.create(msg);
      fail();
    }
    catch (CoreException e) {
    }
    msg.addMetadata(TYPE, "");
    try {
      creator.create(msg);
      fail();
    }
    catch (CoreException e) {
    }
  }

  public void testNormal() throws Exception {
    MetadataTradingRelationshipCreator creator = new MetadataTradingRelationshipCreator(SRC, DEST, TYPE);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata(SRC, SRC);
    msg.addMetadata(DEST, DEST);
    msg.addMetadata(TYPE, TYPE);
    assertNotNull(creator.create(msg));
    TradingRelationship rel = creator.create(msg);
    assertEquals(SRC, rel.getSource());
    assertEquals(DEST, rel.getDestination());
    assertEquals(TYPE, rel.getType());
    assertEquals(new TradingRelationship(SRC, DEST, TYPE).toString(), new TradingRelationship(SRC, DEST, TYPE), rel);
  }


  public void testXmlRoundtrip() throws Exception {
    MetadataTradingRelationshipCreator input = new MetadataTradingRelationshipCreator(SRC, DEST, TYPE);

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    MetadataTradingRelationshipCreator output = (MetadataTradingRelationshipCreator) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }
}
