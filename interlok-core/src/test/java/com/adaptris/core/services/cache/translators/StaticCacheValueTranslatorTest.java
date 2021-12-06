package com.adaptris.core.services.cache.translators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;

public class StaticCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {

  @Test
  public void testGetKeyFromMessage() throws Exception {
    StaticCacheValueTranslator translator = new StaticCacheValueTranslator().withValue("Hello");
    AdaptrisMessage message = createMessage();
    assertEquals("Hello", translator.getKeyFromMessage(message));
  }


  @Test
  public void testGetValueFromMessage() throws Exception {
    StaticCacheValueTranslator translator = new StaticCacheValueTranslator().withValue("Hello");
    AdaptrisMessage message = createMessage();
    assertEquals("Hello", translator.getValueFromMessage(message));
  }

  @Test
  public void testAddValueToMessage() throws Exception {
    StaticCacheValueTranslator translator = new StaticCacheValueTranslator().withValue("Hello");
    AdaptrisMessage message = createMessage();
    try {
      translator.addValueToMessage(message, "some value");
      fail();
    }
    catch (UnsupportedOperationException expected) {

    }
  }
}
