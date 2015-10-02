package com.adaptris.core;

import junit.framework.TestCase;

public class DefaultSerializableMessageTranslatorTest extends TestCase {
  
  public void setUp() throws Exception {
    
  }

  public void tearDown() throws Exception {
    
  }
  
  public void testMetadataTranslated() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("key1", "value1");
    message.addMetadata("key2", "value2");
    message.addMetadata("key3", "value3");
    
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableAdaptrisMessage translated = translator.translate(message);
    
    assertEquals("value1", translated.getMetadataValue("key1"));
    assertEquals("value2", translated.getMetadataValue("key2"));
    assertEquals("value3", translated.getMetadataValue("key3"));
  }
  
  public void testPayloadTranslated() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("SomePayload");
    
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableAdaptrisMessage translated = translator.translate(message);
    
    assertEquals("SomePayload", translated.getContent());
  }
  
  public void testUniqueId() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("SomePayload");
    
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableAdaptrisMessage translated = translator.translate(message);
    
    assertEquals(message.getUniqueId(), translated.getUniqueId());
  }
  
  public void testCharEncoding() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("SomePayload");
    message.setContentEncoding("UTF-8");
    
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableAdaptrisMessage translated = translator.translate(message);
    
    assertEquals(message.getContentEncoding(), translated.getContentEncoding());
  }
  
  public void testExceptionMessage() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("An Error Happened"));

    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableAdaptrisMessage translated = translator.translate(message);
    
    assertEquals("An Error Happened", translated.getMetadataValue(CoreConstants.OBJ_METADATA_EXCEPTION));
  }
  
}
