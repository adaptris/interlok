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

package com.adaptris.core;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class TradingRelationshipTest {

  @Test
  public void testEqual() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();

    assertEquals(tr1, tr2);
    assertFalse(tr1.equals(new Object()));
  }

  @Test
  public void testSetDestination() {
    TradingRelationship tr1 = new TradingRelationship();
    try {
      tr1.setDestination(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    tr1.setDestination("FRED");
    assertEquals("FRED", tr1.getDestination());
  }

  @Test
  public void testSetSource() {
    TradingRelationship tr1 = new TradingRelationship();
    try {
      tr1.setSource(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    tr1.setSource("FRED");
    assertEquals("FRED", tr1.getSource());
  }

  @Test
  public void testSetType() {
    TradingRelationship tr1 = new TradingRelationship();
    try {
      tr1.setType(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    tr1.setType("FRED");
    assertEquals("FRED", tr1.getType());
  }

  @Test
  public void testEqualModified() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();

    tr1.setSource("zzz");
    tr2.setSource("zzz");

    assertEquals(tr1, tr2);
  }

  @Test
  public void testNotEqualSource() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();
    tr2.setSource("zzz");

    assertTrue(!tr1.equals(tr2));
  }

  @Test
  public void testNotEqualDestination() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();
    tr2.setDestination("zzz");

    assertTrue(!tr1.equals(tr2));
  }

  @Test
  public void testNotEqualType() {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = new TradingRelationship();
    tr2.setType("zzz");

    assertTrue(!tr1.equals(tr2));
  }

  @Test
  public void testClone() throws Exception {
    TradingRelationship tr1 = new TradingRelationship();
    TradingRelationship tr2 = (TradingRelationship) tr1.clone();

    assertEquals(tr1, tr2); // semantic
    assertTrue(tr1 != tr2); // memory
  }

  @Test
  public void testHasWildcardsWithWildcards() {
    TradingRelationship tr1 = new TradingRelationship("s", "d", "t");
    assertTrue(tr1.hasWildCards() == false);

    TradingRelationship tr2 = new TradingRelationship("*", "d", "t");
    assertTrue(tr2.hasWildCards() == true);

    TradingRelationship tr3 = new TradingRelationship("s", "*", "t");
    assertTrue(tr3.hasWildCards() == true);

    TradingRelationship tr4 = new TradingRelationship("s", "d", "*");
    assertTrue(tr4.hasWildCards() == true);

    TradingRelationship tr5 = new TradingRelationship("*", "*", "t");
    assertTrue(tr5.hasWildCards() == true);

    TradingRelationship tr6 = new TradingRelationship("*", "d", "*");
    assertTrue(tr6.hasWildCards() == true);

    TradingRelationship tr7 = new TradingRelationship("s", "*", "*");
    assertTrue(tr7.hasWildCards() == true);

    TradingRelationship tr8 = new TradingRelationship("*", "*", "*");
    assertTrue(tr8.hasWildCards() == true);
  }
}
