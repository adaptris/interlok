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
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.TradingRelationship;

@SuppressWarnings("deprecation")
public class SafeServiceNameProviderTest {

  private DefaultServiceNameProvider provider;

  @Before
  public void setUp() throws Exception {
    provider = new SafeServiceNameProvider();
  }

  @Test
  public void testStandard() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dest", "type");
    String result = provider.obtain(t);
    
    assertEquals(result, "src-dest-type");
  }

  @Test
  public void testDifferentSeparator() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dest", "type");
    provider.setSeparator(":");
    String result = provider.obtain(t);
    
    assertEquals(result, "srcdesttype");
  }

  @Test
  public void testNoSeparator() throws Exception {
    TradingRelationship t = new TradingRelationship("src", "dest", "type");
    provider.setSeparator("");
    String result = provider.obtain(t);
    
    assertEquals(result, "srcdesttype");
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
  public void testBadChars() throws Exception {
    TradingRelationship t = new TradingRelationship("/\\?*:| &\"<>\'src", "dest", "type");
    String result = provider.obtain(t);
    
    assertEquals(result, "src-dest-type");
  }
}
