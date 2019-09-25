package com.adaptris.core.services.cache.translators;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class StringPayloadCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {

  @Test
  public void testGetKeyFromMessage() throws Exception {
    StringPayloadCacheTranslator translator = new StringPayloadCacheTranslator();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
    assertEquals("hello world", translator.getKeyFromMessage(message));
  }

  @Test
  public void testGetValueFromMessage() throws Exception {
    StringPayloadCacheTranslator translator = new StringPayloadCacheTranslator();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
    assertEquals("hello world", translator.getValueFromMessage(message));
  }

  @Test
  public void testAddValueToMessage() throws Exception {
    StringPayloadCacheTranslator translator = new StringPayloadCacheTranslator();
    AdaptrisMessage message = createMessage();
    translator.addValueToMessage(message, "Hello World");
    assertEquals("Hello World", message.getContent());
  }

  @Test
  public void testAddValueToMessage_WithEncoding() throws Exception {
    StringPayloadCacheTranslator translator = new StringPayloadCacheTranslator("UTF-8");
    AdaptrisMessage message = createMessage();
    translator.addValueToMessage(message, "Hello World");
    assertEquals("Hello World", message.getContent());
    assertEquals("UTF-8", message.getContentEncoding());
  }
}
