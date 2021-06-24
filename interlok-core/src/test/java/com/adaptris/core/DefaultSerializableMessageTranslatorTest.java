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

import static org.junit.Assert.assertEquals;

import com.adaptris.interlok.types.DefaultSerializableMessage;
import org.junit.Test;
import com.adaptris.interlok.types.SerializableMessage;

public class DefaultSerializableMessageTranslatorTest {

  private static final String BASE64_ORIGINAL = "Mistakes are often the stepping stones to utter failure.";
  public static final String BASE64_ENCODED = "TWlzdGFrZXMgYXJlIG9mdGVuIHRoZSBzdGVwcGluZyBzdG9uZXMgdG8gdXR0ZXIgZmFpbHVyZS4=";

  @Test
  public void testMetadataTranslated() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata("key1", "value1");
    message.addMetadata("key2", "value2");
    message.addMetadata("key3", "value3");
    
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableMessage translated = translator.translate(message);
    
    assertEquals("value1", translated.getMessageHeaders().get("key1"));
    assertEquals("value2", translated.getMessageHeaders().get("key2"));
    assertEquals("value3", translated.getMessageHeaders().get("key3"));
  }

  @Test
  public void testPayloadTranslated() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("SomePayload");
    
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableMessage translated = translator.translate(message);
    
    assertEquals("SomePayload", translated.getContent());
  }

  @Test
  public void testUniqueId() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("SomePayload");
    
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableMessage translated = translator.translate(message);
    
    assertEquals(message.getUniqueId(), translated.getUniqueId());
  }

  @Test
  public void testCharEncoding() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("SomePayload");
    message.setContentEncoding("UTF-8");
    
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableMessage translated = translator.translate(message);
    
    assertEquals(message.getContentEncoding(), translated.getContentEncoding());
  }

  @Test
  public void testExceptionMessage() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("An Error Happened"));

    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    SerializableMessage translated = translator.translate(message);
    
    assertEquals("An Error Happened", translated.getMessageHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION));
  }

  @Test
  public void testBase64Decode() throws Exception {
    DefaultSerializableMessage serializableMessage = new DefaultSerializableMessage();
    serializableMessage.setContent(BASE64_ENCODED);
    serializableMessage.addMessageHeader("_interlokMessageSerialization", "BASE64");

    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    AdaptrisMessage adaptrisMessage = translator.translate(serializableMessage);
    assertEquals(BASE64_ORIGINAL, adaptrisMessage.getContent());
  }

  @Test
  public void testBase64WrongValue() throws Exception {
    DefaultSerializableMessage serializableMessage = new DefaultSerializableMessage();
    serializableMessage.setUniqueId(null);
    serializableMessage.setContent(BASE64_ENCODED);
    serializableMessage.addMessageHeader("_interlokMessageSerialization", "something-else");

    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    AdaptrisMessage adaptrisMessage = translator.translate(serializableMessage);
    assertEquals(BASE64_ENCODED, adaptrisMessage.getContent());
  }
}
