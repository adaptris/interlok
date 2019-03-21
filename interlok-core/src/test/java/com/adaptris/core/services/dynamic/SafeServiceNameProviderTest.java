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

import com.adaptris.core.TradingRelationship;
import junit.framework.TestCase;

@SuppressWarnings("deprecation")
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
