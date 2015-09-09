package com.adaptris.core.services.dynamic;

import junit.framework.TestCase;

import com.adaptris.core.TradingRelationship;

public class DefaultServiceNameProviderTest extends TestCase {

  private DefaultServiceNameProvider provider;

  public DefaultServiceNameProviderTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    provider = new DefaultServiceNameProvider();
  }

  public void testSetSeparator() {
    try {
      provider.setSeparator(null);
      fail("null setSeparator");
    }
    catch (IllegalArgumentException expected) {

    }
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

    assertEquals(result, "src:dest:type");
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
}
