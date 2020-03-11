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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class ConfiguredTradingRelationshipCreatorTest {

  private static final String TYPE = "type";
  private static final String DEST = "dest";
  private static final String SRC = "src";


  public ConfiguredTradingRelationshipCreatorTest() {
  }

  @Test
  public void testSetDestination() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator();
    try {
      creator.setDestination(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testSetSource() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator();
    try {
      creator.setSource(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testSetType() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator();
    try {
      creator.setType(null);
      fail("null allowed");
    }
    catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testCreate() throws Exception {
    ConfiguredTradingRelationshipCreator creator = new ConfiguredTradingRelationshipCreator(SRC, DEST, TYPE);
    assertNotNull(creator.create(new DefaultMessageFactory().newMessage()));
    TradingRelationship rel = creator.create(new DefaultMessageFactory().newMessage());
    assertEquals(SRC, rel.getSource());
    assertEquals(DEST, rel.getDestination());
    assertEquals(TYPE, rel.getType());
    assertEquals(new TradingRelationship(SRC, DEST, TYPE).toString(), new TradingRelationship(SRC, DEST, TYPE), rel);
  }

  @Test
  public void testXmlRoundTrip() throws Exception {
    ConfiguredTradingRelationshipCreator input = new ConfiguredTradingRelationshipCreator(SRC, DEST, TYPE);
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    ConfiguredTradingRelationshipCreator output = (ConfiguredTradingRelationshipCreator) m.unmarshal(xml);
    BaseCase.assertRoundtripEquality(input, output);
  }
}
