/*
 * $RCSfile: TradingRelationshipTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/10/04 23:40:33 $
 * $Author: hfraser $
 */
package com.adaptris.core;

import junit.framework.TestCase;

public class TradingRelationshipTest extends TestCase {

  public TradingRelationshipTest(String name) {
    super(name);
  }

  public void testEqual() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();

    assertEquals(tr1, tr2);
    assertFalse(tr1.equals(new Object()));
  }

  public void testSetDestination() {
    TradingRelationship tr1 = new TradingRelationship();
    try {
      tr1.setDestination("");
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    try {
      tr1.setDestination(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    tr1.setDestination("FRED");
    assertEquals("FRED", tr1.getDestination());
  }

  public void testSetSource() {
    TradingRelationship tr1 = new TradingRelationship();
    try {
      tr1.setSource("");
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    try {
      tr1.setSource(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    tr1.setSource("FRED");
    assertEquals("FRED", tr1.getSource());
  }


  public void testSetType() {
    TradingRelationship tr1 = new TradingRelationship();
    try {
      tr1.setType("");
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    try {
      tr1.setType(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    tr1.setType("FRED");
    assertEquals("FRED", tr1.getType());
  }

  public void testEqualModified() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();

    tr1.setSource("zzz");
    tr2.setSource("zzz");

    assertEquals(tr1, tr2);
  }

  public void testNotEqualSource() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();
    tr2.setSource("zzz");

    assertTrue(!tr1.equals(tr2));
  }

  public void testNotEqualDestination() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();
    tr2.setDestination("zzz");

    assertTrue(!tr1.equals(tr2));
  }

  public void testNotEqualType() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();
    tr2.setType("zzz");

    assertTrue(!tr1.equals(tr2));
  }

  public void testClone() throws Exception {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = (TradingRelationship) tr1.clone();

    assertEquals(tr1, tr2); // semantic
    assertTrue(tr1 != tr2); // memory
  }

  public void testHasWildcardsWithWildcards() {
    TradingRelationship tr1 = new TradingRelationship("s", "d", "t");
    assertTrue(tr1.hasWildCards() == false);

    TradingRelationship tr2 = new TradingRelationship("*", "d", "t");
    assertTrue(tr2.hasWildCards() == true);

    TradingRelationship tr3 = new TradingRelationship("s", "*", "t");
    assertTrue(tr3.hasWildCards() == true);

    TradingRelationship tr4 = new TradingRelationship("s", "d", "*");
    assertTrue(tr4.hasWildCards() == true);

    TradingRelationship tr5 = new TradingRelationship("*", "*", "t");
    assertTrue(tr5.hasWildCards() == true);

    TradingRelationship tr6 = new TradingRelationship("*", "d", "*");
    assertTrue(tr6.hasWildCards() == true);

    TradingRelationship tr7 = new TradingRelationship("s", "*", "*");
    assertTrue(tr7.hasWildCards() == true);

    TradingRelationship tr8 = new TradingRelationship("*", "*", "*");
    assertTrue(tr8.hasWildCards() == true);
  }
}
