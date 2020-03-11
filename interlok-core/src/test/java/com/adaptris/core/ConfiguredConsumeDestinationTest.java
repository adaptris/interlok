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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class ConfiguredConsumeDestinationTest {
  private static final String EMPTY_STRING = "";
  private static final String THREAD_NAME = "threadName";
  private static final String DEST_NAME = "destination";
  private static final String FILTER = "filter";

  public ConfiguredConsumeDestinationTest() {
  }

  @Test
  public void testEquals() {
    ConfiguredConsumeDestination d1 = new ConfiguredConsumeDestination(DEST_NAME, FILTER);
    ConfiguredConsumeDestination d2 = new ConfiguredConsumeDestination(DEST_NAME, FILTER);
    ConfiguredConsumeDestination d3 = new ConfiguredConsumeDestination(EMPTY_STRING, FILTER);
    ConfiguredConsumeDestination d4 = new ConfiguredConsumeDestination(null, FILTER);
    ConfiguredConsumeDestination d5 = new ConfiguredConsumeDestination(DEST_NAME, EMPTY_STRING);
    ConfiguredConsumeDestination d6 = new ConfiguredConsumeDestination(DEST_NAME, null);
    ConfiguredConsumeDestination d7 = new ConfiguredConsumeDestination(null, null);

    equalityAssertion(new ConfiguredConsumeDestination(), new ConfiguredConsumeDestination(), true);
    equalityAssertion(d1, d1, true);
    equalityAssertion(d1, d2, true);
    equalityAssertion(d2, d1, true);
    equalityAssertion(d1, d3, false);
    equalityAssertion(d3, d1, false);
    equalityAssertion(d1, d4, false);
    equalityAssertion(d4, d1, false);
    equalityAssertion(d1, d5, false);
    equalityAssertion(d5, d1, false);
    equalityAssertion(d1, d6, false);
    equalityAssertion(d6, d1, false);
    equalityAssertion(d1, d7, false);
    equalityAssertion(d7, d1, false);
    assertFalse(d1.equals(null));
    assertFalse(d1.equals(new Object()));
  }

  private void equalityAssertion(Object a, Object b, boolean equal) {
    if (equal) {
      if (a != null) {
        assertTrue(a.toString(), a.equals(b));
        assertEquals(a, b);
      }
      if (a != null && b != null) {
        assertEquals(a.toString(), a.hashCode(), b.hashCode());
      }
    }
    else {
      if (a != null) {
        assertFalse(a.toString(), a.equals(b));
        assertNotSame(a, b);
      }
      if (a != null && b != null) {
        assertNotSame(a.toString(), a.hashCode(), b.hashCode());
      }
    }
  }

  @Test
  public void testSetDestination() {
    ConfiguredConsumeDestination destination = new ConfiguredConsumeDestination(DEST_NAME, FILTER);
    destination.setDestination("newDestination");
    assertTrue("newDestination".equals(destination.getDestination()));
  }

  @Test
  public void testSetFilterExpression() {
    ConfiguredConsumeDestination destination = new ConfiguredConsumeDestination(DEST_NAME, FILTER);
    destination.setFilterExpression("new");
    assertTrue("new".equals(destination.getFilterExpression()));
  }

  @Test
  public void testSetConfiguredThreadName() {
    ConfiguredConsumeDestination destination = new ConfiguredConsumeDestination(DEST_NAME, FILTER, THREAD_NAME);
    assertEquals(destination.toString(), THREAD_NAME, destination.getConfiguredThreadName());
    assertEquals(destination.toString(), THREAD_NAME, destination.getDeliveryThreadName());
    try {
      destination.setConfiguredThreadName(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    destination.setConfiguredThreadName(EMPTY_STRING);
    assertEquals(destination.toString(), EMPTY_STRING, destination.getConfiguredThreadName());
    assertNotSame(destination.toString(), EMPTY_STRING, destination.getDeliveryThreadName());
  }

  @Test
  public void testXmlRoundTrip() throws Exception {
    ConfiguredConsumeDestination input = new ConfiguredConsumeDestination(DEST_NAME, FILTER, THREAD_NAME);
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    ConfiguredConsumeDestination output = (ConfiguredConsumeDestination) m.unmarshal(xml);
    BaseCase.assertRoundtripEquality(input, output);
  }
}
