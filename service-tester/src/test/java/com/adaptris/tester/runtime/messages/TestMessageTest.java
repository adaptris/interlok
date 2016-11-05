package com.adaptris.tester.runtime.messages;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class TestMessageTest extends TestCase {

  private static final String METADATA_KEY = "key";
  private static final String METADATA_VALUE = "value";
  private static final String PAYLOAD = "payload";


  public void testEmptyConstructor() throws Exception{
    TestMessage m = new TestMessage();
    assertEquals("", m.getPayload());
    assertEquals(0, m.getMessageHeaders().size());
  }

  public void testConstructor() throws Exception{
    Map<String, String> metadata = new HashMap<>();
    metadata.put(METADATA_KEY, METADATA_VALUE);
    TestMessage m = new TestMessage(metadata, PAYLOAD);
    assertEquals(PAYLOAD, m.getPayload());
    assertEquals(1, m.getMessageHeaders().size());
    assertTrue(m.getMessageHeaders().containsKey(METADATA_KEY));
    assertEquals(METADATA_VALUE, m.getMessageHeaders().get(METADATA_KEY));
  }

  public void testSetMessageHeaders() throws Exception {
    TestMessage m = new TestMessage();
    Map<String, String> metadata = new HashMap<>();
    metadata.put(METADATA_KEY, METADATA_VALUE);
    m.setMessageHeaders(metadata);
    assertEquals(1, m.getMessageHeaders().size());
    assertTrue(m.getMessageHeaders().containsKey(METADATA_KEY));
    assertEquals(METADATA_VALUE, m.getMessageHeaders().get(METADATA_KEY));
  }

  public void testAddMessageHeader() throws Exception {
    TestMessage m = new TestMessage();
    m.addMessageHeader(METADATA_KEY, METADATA_VALUE);
    assertEquals(1, m.getMessageHeaders().size());
    assertTrue(m.getMessageHeaders().containsKey(METADATA_KEY));
    assertEquals(METADATA_VALUE, m.getMessageHeaders().get(METADATA_KEY));
  }

  public void testSetPayload() throws Exception {
    TestMessage m = new TestMessage();
    m.setPayload(PAYLOAD);
    assertEquals(PAYLOAD, m.getPayload());
  }

  public void testToString() throws Exception {
    TestMessage m = new TestMessage();
    m.addMessageHeader(METADATA_KEY, METADATA_VALUE);
    m.setPayload(PAYLOAD);
    assertEquals("Metadata: {key=value}\n" +
        "Payload: payload", m.toString());
  }

}