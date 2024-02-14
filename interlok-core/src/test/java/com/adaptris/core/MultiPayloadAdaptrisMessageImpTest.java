package com.adaptris.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class MultiPayloadAdaptrisMessageImpTest extends BaseCase {

  private static String PAYLOAD = "Some payload";
  private static String EXAMPLE_PAYLOAD_FILE = "example.payload.file";

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
  
  //Unit test added to test bug INTERLOK-4325 
  @Test
  public void testUpdatingOriginalPayloadFromInputStream() throws IOException {
    MultiPayloadAdaptrisMessage multiPayloadAdaptrisMessage = MessageHelper.createMultiPayloadMessage("default-payload", PROPERTIES.getProperty(EXAMPLE_PAYLOAD_FILE));
    multiPayloadAdaptrisMessage.setContent("new payload", multiPayloadAdaptrisMessage.getContentEncoding());
    assertEquals("new payload", multiPayloadAdaptrisMessage.getContent());
    
    multiPayloadAdaptrisMessage.addContent("default-payload", "another payload", multiPayloadAdaptrisMessage.getContentEncoding());
    assertEquals("another payload", multiPayloadAdaptrisMessage.getContent());
  }
}
