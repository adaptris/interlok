package com.adaptris.core.services.dynamic;

import com.adaptris.core.TradingRelationship;

import junit.framework.TestCase;

public class SafeServiceNameProviderTest extends TestCase {

  private DefaultServiceNameProvider provider;
  
  public SafeServiceNameProviderTest(String arg0) {
    super(arg0);
  }
  
  protected void setUp() throws Exception {
    provider = new SafeServiceNameProvider();
  }
  
  public void testStandard() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dest", "type");
    String result = provider.obtain(t);
    
    assertEquals(result, "src-dest-type");
  }
  
  public void testDifferentSeparator() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dest", "type");
    provider.setSeparator(":");
    String result = provider.obtain(t);
    
    assertEquals(result, "srcdesttype");
  }
  
  public void testNoSeparator() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dest", "type");
    provider.setSeparator("");
    String result = provider.obtain(t);
    
    assertEquals(result, "srcdesttype");
  }
  
  public void testNullParams() throws Exception {
    TradingRelationship t = null;
    try {
      provider.obtain(t);
      fail();
    }
    catch (Exception e) {
      // expected
    }
    
    TradingRelationship[] ts = null;
    try {
      provider.obtain(ts);
      fail();
    }
    catch (Exception e) {
      // expected
    }
    
    TradingRelationship[] containsNulls = new TradingRelationship[2];
    assertTrue(containsNulls.length == 2);
    
    try {
      provider.obtain(containsNulls);
      fail();
    }
    catch (Exception e) {
      // expected
    }
  }

  public void testBadChars() throws Exception {
    TradingRelationship t = new TradingRelationship("/\\?*:| &\"<>\'src", "dest", "type");
    String result = provider.obtain(t);
    
    assertEquals(result, "src-dest-type");
  }
}
