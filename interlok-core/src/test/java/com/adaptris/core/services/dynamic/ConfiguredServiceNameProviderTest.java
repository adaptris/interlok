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

package com.adaptris.core.services.dynamic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.BaseCase;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.TradingRelationship;

@SuppressWarnings("deprecation")
public class ConfiguredServiceNameProviderTest extends BaseCase {

  private ConfiguredServiceNameProvider provider;

  public ConfiguredServiceNameProviderTest() {
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void beforeMyTests() throws Exception {
    provider = new ConfiguredServiceNameProvider();
    this.fillStore();
  }

  @Test
  public void testCastor() throws Exception {
    AdaptrisMarshaller c = DefaultMarshaller.getDefaultMarshaller();
    String s = c.marshal(provider);
    Object p = c.unmarshal(s);
    assertRoundtripEquality(provider, p);
  }

  @Test
  public void testStoreSameAndEqualItems() {
    ServiceNameMapper mapper = new ServiceNameMapper("src", "dst", "diff", "1");
    assertTrue(provider.addServiceNameMapper(mapper));
    // add same item
    assertTrue(!provider.addServiceNameMapper(mapper));
    ServiceNameMapper mapper2 = new ServiceNameMapper("src", "dst", "diff", "1");
    // add equal item
    assertTrue(!provider.addServiceNameMapper(mapper2));
  }

  @Test
  public void testNameExists() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dst", "typ");
    assertEquals(provider.obtain(t), "1");
  }

  @Test
  public void testNameDoesNotExist() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dst", "blah");
    assertEquals(provider.obtain(t), null);
  }

  @Test
  public void testNameExistsArray() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dst", "typ");
    TradingRelationship[] ts = new TradingRelationship[1];
    ts[0] = t;

    assertEquals(provider.obtain(ts), "1");
  }

  @Test
  public void testNameDoesNotExistArray() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dst", "blah");
    TradingRelationship[] ts = new TradingRelationship[1];
    ts[0] = t;

    assertEquals(provider.obtain(ts), null);
  }

  @Test
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

  @Test
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
