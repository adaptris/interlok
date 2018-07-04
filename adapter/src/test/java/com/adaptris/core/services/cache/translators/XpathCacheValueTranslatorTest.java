package com.adaptris.core.services.cache.translators;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

@SuppressWarnings("deprecation")
public class XpathCacheValueTranslatorTest extends CacheValueTranslatorBaseCase {


  public XpathCacheValueTranslatorTest(String s) {
    super(s);
  }

  public void testGetValueFromMessage() throws Exception {
    XpathCacheValueTranslator translator = createTranslator();
    AdaptrisMessage message = createMessage();
    assertEquals("abc", translator.getValueFromMessage(message));
  }

  public void testGetValueFromMessage_DocumentBuilderFactory() throws Exception {
    XpathCacheValueTranslator translator = createTranslator();
    translator.setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder.newInstance());
    AdaptrisMessage message = createMessage();
    assertEquals("abc", translator.getValueFromMessage(message));
  }

  public void testGetValueFromMessage_NotXml() throws Exception {
    XpathCacheValueTranslator translator = createTranslator();
    AdaptrisMessage message = createMessage();
    message.setStringPayload("This is not XML");
    try {
      translator.getValueFromMessage(message);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  // No longer a valid test, change to use SAXON means that this will never resolve
  // public void testGetValueFromMessage_NoNamespace() throws Exception {
  // XpathCacheValueTranslator translator = new XpathCacheValueTranslator();
  // translator.setXpath("/root/element[@id='one']");
  // AdaptrisMessage message = createMessage();
  // assertEquals("abc", translator.getValueFromMessage(message));
  // }

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
