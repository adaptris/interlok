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

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.TradingRelationship;

@SuppressWarnings("deprecation")
public class StandardMatchingStrategyTest {
  
  private StandardMatchingStrategy strategy;

  @Before
  public void setUp() throws Exception {
    strategy = new StandardMatchingStrategy();
  }

  @Test
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

  @Test
  public void testNullParam() throws Exception {
    TradingRelationship[] ts = strategy.create(null);
    assertTrue(ts.length == 0);
  }
}
