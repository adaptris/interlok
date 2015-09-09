package com.adaptris.core.services.dynamic;

import com.adaptris.core.TradingRelationship;

import junit.framework.TestCase;

public class ExactMatchingStrategyTest extends TestCase {

  private ExactMatchingStrategy exactMatchingStrategy;
  
  public ExactMatchingStrategyTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    exactMatchingStrategy = new ExactMatchingStrategy();
  }

  public void testValidParam() throws Exception {
    TradingRelationship t = new TradingRelationship();
    t.setSource("src");
    t.setDestination("type");
    t.setType("type");
    
    TradingRelationship[] ts = exactMatchingStrategy.create(t);
    
    assertTrue(ts.length == 1);
    assertTrue(ts[0] == t);
  }
  
  public void testNullParam() throws Exception {
    TradingRelationship[] ts = exactMatchingStrategy.create(null);
    assertTrue(ts.length == 0);
  }
}
