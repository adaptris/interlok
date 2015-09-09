package com.adaptris.core;

public class ConfiguredConsumeDestinationTest extends BaseCase {

  private static final String EMPTY_STRING = "";
  private static final String THREAD_NAME = "threadName";
  private static final String DEST_NAME = "destination";
  private static final String FILTER = "filter";

  public ConfiguredConsumeDestinationTest(String name) {
    super(name);
  }

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

  public void testSetDestination() {
    ConfiguredConsumeDestination destination = new ConfiguredConsumeDestination(DEST_NAME, FILTER);
    destination.setDestination("newDestination");
    assertTrue("newDestination".equals(destination.getDestination()));
  }

  public void testSetFilterExpression() {
    ConfiguredConsumeDestination destination = new ConfiguredConsumeDestination(DEST_NAME, FILTER);
    destination.setFilterExpression("new");
    assertTrue("new".equals(destination.getFilterExpression()));
  }

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

  public void testXmlRoundTrip() throws Exception {
    ConfiguredConsumeDestination input = new ConfiguredConsumeDestination(DEST_NAME, FILTER, THREAD_NAME);
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    ConfiguredConsumeDestination output = (ConfiguredConsumeDestination) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }
}