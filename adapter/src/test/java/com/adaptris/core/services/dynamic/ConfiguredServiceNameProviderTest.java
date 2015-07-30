/*
 * $RCSfile: ConfiguredServiceNameProviderTest.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/07/14 17:05:42 $
 * $Author: lchan $
 */
package com.adaptris.core.services.dynamic;

import java.util.Arrays;
import java.util.HashSet;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.BaseCase;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.TradingRelationship;

public class ConfiguredServiceNameProviderTest extends BaseCase {

  private ConfiguredServiceNameProvider provider;

  public ConfiguredServiceNameProviderTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    provider = new ConfiguredServiceNameProvider();
    this.fillStore();
  }

  public void testCastor() throws Exception {
    AdaptrisMarshaller c = DefaultMarshaller.getDefaultMarshaller();
    String s = c.marshal(provider);
    Object p = c.unmarshal(s);
    assertRoundtripEquality(provider, p);
  }

  public void testStoreSameAndEqualItems() {
    ServiceNameMapper mapper = new ServiceNameMapper("src", "dst", "diff", "1");
    assertTrue(provider.addServiceNameMapper(mapper));
    // add same item
    assertTrue(!provider.addServiceNameMapper(mapper));
    ServiceNameMapper mapper2 = new ServiceNameMapper("src", "dst", "diff", "1");
    // add equal item
    assertTrue(!provider.addServiceNameMapper(mapper2));
  }

  public void testNameExists() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dst", "typ");
    assertEquals(provider.obtain(t), "1");
  }

  public void testNameDoesNotExist() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dst", "blah");
    assertEquals(provider.obtain(t), null);
  }

  public void testNameExistsArray() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dst", "typ");
    TradingRelationship[] ts = new TradingRelationship[1];
    ts[0] = t;

    assertEquals(provider.obtain(ts), "1");
  }

  public void testNameDoesNotExistArray() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dst", "blah");
    TradingRelationship[] ts = new TradingRelationship[1];
    ts[0] = t;

    assertEquals(provider.obtain(ts), null);
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

  public void testSetServiceNameMappers() throws Exception {
    ServiceNameMapper mapper1 = new ServiceNameMapper("src", "dst", "typ", "2");
    provider.setServiceNameMappers(new HashSet(Arrays.asList(new ServiceNameMapper[]
    {
      mapper1
    })));
    assertEquals(1, provider.getServiceNameMappers().size());
  }
  private void fillStore() {
    ServiceNameMapper mapper1 = new ServiceNameMapper("src", "dst", "typ", "1");
    assertTrue(provider.addServiceNameMapper(mapper1));
  }
}
