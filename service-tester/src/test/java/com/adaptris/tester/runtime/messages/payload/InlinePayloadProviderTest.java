package com.adaptris.tester.runtime.messages.payload;

import com.adaptris.tester.runtime.messages.MessagesCase;

public class InlinePayloadProviderTest extends MessagesCase {

  public InlinePayloadProviderTest(String name) {
    super(name);
  }

  public void testEmptyConstructor() throws Exception{
    InlinePayloadProvider m = new InlinePayloadProvider();
    assertEquals("", m.getPayload());
  }

  public void testConstructor() throws Exception{
    InlinePayloadProvider m = new InlinePayloadProvider(PAYLOAD);
    assertEquals(PAYLOAD, m.getPayload());
  }

  public void testGetPayload() throws Exception {
    InlinePayloadProvider m = new InlinePayloadProvider(PAYLOAD);
    assertEquals(PAYLOAD, m.getPayload());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new InlinePayloadProvider(PAYLOAD);
  }
}