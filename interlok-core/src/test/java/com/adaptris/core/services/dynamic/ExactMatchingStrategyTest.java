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
