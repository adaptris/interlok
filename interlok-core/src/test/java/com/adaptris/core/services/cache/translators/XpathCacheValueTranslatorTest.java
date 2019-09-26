package com.adaptris.core.services.cache.translators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class XpathCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {


  @Test
  public void testGetKeyFromMessage() throws Exception {
    XpathCacheValueTranslator translator = createTranslator();
    AdaptrisMessage message = createMessage();
    assertEquals("abc", translator.getKeyFromMessage(message));
  }

  @Test
  public void testGetValueFromMessage() throws Exception {
    XpathCacheValueTranslator translator = createTranslator();
    AdaptrisMessage message = createMessage();
    assertEquals("abc", translator.getValueFromMessage(message));
  }

  @Test
  public void testGetValueFromMessage_DocumentBuilderFactory() throws Exception {
    XpathCacheValueTranslator translator = createTranslator();
    translator.setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder.newInstance());
    AdaptrisMessage message = createMessage();
    assertEquals("abc", translator.getValueFromMessage(message));
  }

  @Test
  public void testGetValueFromMessage_NotXml() throws Exception {
    XpathCacheValueTranslator translator = createTranslator();
    AdaptrisMessage message = createMessage();
    message.setContent("This is not XML", null);
    try {
      translator.getValueFromMessage(message);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testAddValue() throws Exception {
    XpathCacheValueTranslator translator = createTranslator();
    AdaptrisMessage message = createMessage();
    try {
      translator.addValueToMessage(message, "some value");
      fail();
    }
    catch (UnsupportedOperationException expected) {

    }
  }

  private XpathCacheValueTranslator createTranslator() {
    XpathCacheValueTranslator translator = new XpathCacheValueTranslator();
    KeyValuePairSet set = new KeyValuePairSet();
    set.add(new KeyValuePair("test", "uri:test"));
    translator.setNamespaceContext(set);
    translator.setXpath("/test:root/test:element[@id='one']");
    return translator;
  }
}
