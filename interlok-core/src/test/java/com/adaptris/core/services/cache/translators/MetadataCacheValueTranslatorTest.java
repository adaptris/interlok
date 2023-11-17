package com.adaptris.core.services.cache.translators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;

public class MetadataCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {

  @Test
  public void testGetValueFromMessage() throws Exception {
    MetadataCacheValueTranslator translator = new MetadataCacheValueTranslator(KEY_TWO);
    AdaptrisMessage message = createMessage();
    assertEquals(VALUE_TWO, translator.getValueFromMessage(message));
  }

  @Test
  public void testAddValueToMessage() throws Exception {
    MetadataCacheValueTranslator translator = new MetadataCacheValueTranslator(KEY_TWO);
    AdaptrisMessage message = createMessage();
    translator.addValueToMessage(message, "three");
    assertEquals("three", message.getMetadataValue(KEY_TWO));
  }
}
