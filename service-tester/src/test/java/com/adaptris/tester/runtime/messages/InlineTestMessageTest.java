package com.adaptris.tester.runtime.messages;

import com.adaptris.util.KeyValuePairSet;

@Deprecated
public class InlineTestMessageTest extends MessagesCase {

  public InlineTestMessageTest(String name) {
    super(name);
  }

  public void testEmptyConstructor() throws Exception{
    InlineTestMessage m = new InlineTestMessage();
    assertEquals("", m.getPayload());
    assertEquals(0, m.getMessageHeaders().size());
  }

  public void testSetMetadata() throws Exception {
    InlineTestMessage m = new InlineTestMessage();
    m.setMetadata(new KeyValuePairSet(metadata));
    assertEquals(1, m.getMessageHeaders().size());
    assertTrue(m.getMessageHeaders().containsKey(METADATA_KEY));
    assertEquals(METADATA_VALUE, m.getMessageHeaders().get(METADATA_KEY));
  }

  public void testSetPayload() throws Exception {
    InlineTestMessage m = new InlineTestMessage();
    m.setPayload(PAYLOAD);
    assertEquals(PAYLOAD, m.getPayload());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    InlineTestMessage m = new InlineTestMessage();
    m.setMetadata(new KeyValuePairSet(metadata));
    m.setPayload(PAYLOAD);
    return m;
  }
}