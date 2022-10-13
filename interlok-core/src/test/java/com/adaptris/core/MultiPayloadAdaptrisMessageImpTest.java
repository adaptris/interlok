package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class MultiPayloadAdaptrisMessageImpTest {

  private static String PAYLOAD = "Some payload";

  @Test
  public void testAddPayloadMessageHeader() {
    MultiPayloadAdaptrisMessage multiPayloadAdaptrisMessage = (MultiPayloadAdaptrisMessage) new MultiPayloadMessageFactory().newMessage(PAYLOAD);

    multiPayloadAdaptrisMessage.addPayloadMessageHeader("key", "value");
    multiPayloadAdaptrisMessage.addPayloadMessageHeader("another-payload", "another-key", "another-value");

    assertEquals("value", multiPayloadAdaptrisMessage.getMetadataValue("PAYLOAD_default-payload_key"));
    assertEquals("another-value", multiPayloadAdaptrisMessage.getMetadataValue("PAYLOAD_another-payload_another-key"));
  }

  @Test
  public void testGetPayloadMessageHeaders() {
    MultiPayloadAdaptrisMessage multiPayloadAdaptrisMessage = (MultiPayloadAdaptrisMessage) new MultiPayloadMessageFactory().newMessage(PAYLOAD);

    multiPayloadAdaptrisMessage.addPayloadMessageHeader("key", "value");
    multiPayloadAdaptrisMessage.addPayloadMessageHeader("another-payload", "key", "value2");
    multiPayloadAdaptrisMessage.addPayloadMessageHeader("another-payload", "another-key", "another-value");

    Map<String, String> payloadMessageHeaders = multiPayloadAdaptrisMessage.getPayloadMessageHeaders();
    assertEquals(1, payloadMessageHeaders.size());
    assertEquals("value", payloadMessageHeaders.get("key"));
    assertEquals("value", multiPayloadAdaptrisMessage.getPayloadMessageHeaderValue("key"));
    assertEquals("value2", multiPayloadAdaptrisMessage.getPayloadMessageHeaderValue("another-payload", "key"));
  }

  @Test
  public void testPayloadHeadersContainsKey() {
    MultiPayloadAdaptrisMessage multiPayloadAdaptrisMessage = (MultiPayloadAdaptrisMessage) new MultiPayloadMessageFactory().newMessage(PAYLOAD);

    multiPayloadAdaptrisMessage.addPayloadMessageHeader("key", "value");
    multiPayloadAdaptrisMessage.addPayloadMessageHeader("another-payload", "key", "value2");

    assertTrue(multiPayloadAdaptrisMessage.payloadHeadersContainsKey("key"));
    assertTrue(multiPayloadAdaptrisMessage.payloadHeadersContainsKey("another-payload", "key"));
  }

  @Test
  public void testRemovePayloadMessageHeader() {
    MultiPayloadAdaptrisMessage multiPayloadAdaptrisMessage = (MultiPayloadAdaptrisMessage) new MultiPayloadMessageFactory().newMessage(PAYLOAD);

    multiPayloadAdaptrisMessage.addPayloadMessageHeader("key", "value");
    multiPayloadAdaptrisMessage.addPayloadMessageHeader("another-payload", "key", "value2");

    multiPayloadAdaptrisMessage.removePayloadMessageHeader("key");
    multiPayloadAdaptrisMessage.removePayloadMessageHeader("another-payload", "key");

    assertFalse(multiPayloadAdaptrisMessage.payloadHeadersContainsKey("key"));
    assertFalse(multiPayloadAdaptrisMessage.payloadHeadersContainsKey("another-payload", "key"));
  }

}
