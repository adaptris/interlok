package com.adaptris.core.services.cache.translators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.MessageDigest;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.cache.CacheValueTranslator;

public class BytePayloadCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {

  @Test
  public void testGetValueFromMessage() throws Exception {
    BytePayloadCacheValueTranslator translator = new BytePayloadCacheValueTranslator();
    AdaptrisMessage message = createMessage();

    assertEquals(PAYLOAD.getBytes().length, translator.getValueFromMessage(message).length);
  }

  @Test
  public void testAddValueToMessage() throws Exception {
    BytePayloadCacheValueTranslator translator = new BytePayloadCacheValueTranslator();
    AdaptrisMessage message = createMessage();
    translator.addValueToMessage(message, "Hello World".getBytes());
    assertTrue(MessageDigest.isEqual("Hello World".getBytes(), message.getPayload()));
  }

  @Test
  public void testAddInvalidValueToMessage() throws Exception {
    AdaptrisMessage message = createMessage();
    CacheValueTranslator translator = new BytePayloadCacheValueTranslator();
    try {
      translator.addValueToMessage(message, new Object());
      fail("Successfully added an object that isn't a byte[]");
    }
    catch (ClassCastException expected) {
      ;
    }

  }

}
