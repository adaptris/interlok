/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
