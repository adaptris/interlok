package com.adaptris.core.services.cache.translators;

import com.adaptris.core.AdaptrisMessage;

@SuppressWarnings("deprecation")
public class StringPayloadCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {

  public StringPayloadCacheValueTranslatorTest(String s) {
    super(s);
  }

  public void testGetValueFromMessage() throws Exception {
    StringPayloadCacheTranslator translator = new StringPayloadCacheTranslator();
    AdaptrisMessage message = createMessage();

    assertEquals(PAYLOAD.length(), translator.getValueFromMessage(message).length());
  }

  public void testAddValueToMessage() throws Exception {
    StringPayloadCacheTranslator translator = new StringPayloadCacheTranslator();
    AdaptrisMessage message = createMessage();
    translator.addValueToMessage(message, "Hello World");
    assertEquals("Hello World", message.getStringPayload());
  }

  public void testAddValueToMessage_WithEncoding() throws Exception {
    StringPayloadCacheTranslator translator = new StringPayloadCacheTranslator("UTF-8");
    AdaptrisMessage message = createMessage();
    translator.addValueToMessage(message, "Hello World");
    assertEquals("Hello World", message.getStringPayload());
    assertEquals("UTF-8", message.getCharEncoding());
  }
}
