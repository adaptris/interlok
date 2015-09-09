package com.adaptris.core.services.dynamic;

import com.adaptris.core.TradingRelationship;

import junit.framework.TestCase;

public class StandardMatchingStrategyTest extends TestCase {
  
  private StandardMatchingStrategy strategy;

  public StandardMatchingStrategyTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    strategy = new StandardMatchingStrategy();
  }

  public void testValidParam() throws Exception {
    TradingRelationship t = new TradingRelationship();
    t.setSource("src");
    t.setDestination("des");
    t.setType("typ");
    
    TradingRelationship[] ts = strategy.create(t);
    
    assertTrue(ts.length == 8);
    assertTrue(ts[0].equals(t));
    
    TradingRelationship t1 = new TradingRelationship("*", "des", "typ");
    assertTrue(ts[1].equals(t1));
    
    TradingRelationship t2 = new TradingRelationship("src", "*", "typ");
    assertTrue(ts[2].equals(t2));
    
    TradingRelationship t3 = new TradingRelationship("src", "des", "*");
    assertTrue(ts[3].equals(t3));
    
    TradingRelationship t4 = new TradingRelationship("*", "*", "typ");
    assertTrue(ts[4].equals(t4));
    
    TradingRelationship t5 = new TradingRelationship("src", "*", "*");
    assertTrue(ts[5].equals(t5));
    
    TradingRelationship t6 = new TradingRelationship("*", "des", "*");
    assertTrue(ts[6].equals(t6));
    
    TradingRelationship t7 = new TradingRelationship("*", "*", "*");
    assertTrue(ts[7].equals(t7));
  }
  
  public void testNullParam() throws Exception {
    TradingRelationship[] ts = strategy.create(null);
    assertTrue(ts.length == 0);
  }
}
