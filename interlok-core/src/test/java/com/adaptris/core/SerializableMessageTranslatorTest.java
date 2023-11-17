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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.RandomAccessFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.interlok.types.SerializableMessage;

public class SerializableMessageTranslatorTest {
  
  
  @Test
  public void testMessageFactory() throws Exception {
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    assertEquals(DefaultMessageFactory.class, translator.currentMessageFactory().getClass());
    translator.registerMessageFactory(new FileBackedMessageFactory());
    assertEquals(FileBackedMessageFactory.class, translator.currentMessageFactory().getClass());
  }

  @Test
  public void testSerialize_FileBackedMessage_Small() throws Exception {
    FileBackedMessage adaptrisMessage = (FileBackedMessage) new FileBackedMessageFactory().newMessage("Some Payload");
    adaptrisMessage.addMetadata("MetaKey1", "MetaValue1");
    adaptrisMessage.addMetadata("MetaKey2", "MetaValue2");
    SerializableMessage serialisableAdaptrisMessage = new DefaultSerializableMessageTranslator().translate(adaptrisMessage);

    assertEquals("MetaValue1", serialisableAdaptrisMessage.getMessageHeaders().get("MetaKey1"));
    assertEquals("MetaValue2", serialisableAdaptrisMessage.getMessageHeaders().get("MetaKey2"));
    assertEquals(adaptrisMessage.getUniqueId(), serialisableAdaptrisMessage.getUniqueId());
    assertEquals("Some Payload", serialisableAdaptrisMessage.getContent());
  }

  @Test
  public void testSerialize_FileBackedMessage_TooLarge() throws Exception {
    FileBackedMessage adaptrisMessage = (FileBackedMessage) new FileBackedMessageFactory().newMessage();
    File sourceFile = TempFileUtils.createTrackedFile(this);
    RandomAccessFile rf = new RandomAccessFile(sourceFile, "rw");
    rf.setLength(1024L * 7 * 1024L); // 7Mb should be bigger than the default size.
    rf.close();
    adaptrisMessage.initialiseFrom(sourceFile);
    adaptrisMessage.addMetadata("MetaKey1", "MetaValue1");
    adaptrisMessage.addMetadata("MetaKey2", "MetaValue2");
    SerializableMessage serialisableAdaptrisMessage = new DefaultSerializableMessageTranslator().translate(adaptrisMessage);

    assertEquals("MetaValue1", serialisableAdaptrisMessage.getMessageHeaders().get("MetaKey1"));
    assertEquals("MetaValue2", serialisableAdaptrisMessage.getMessageHeaders().get("MetaKey2"));
    assertEquals(adaptrisMessage.getUniqueId(), serialisableAdaptrisMessage.getUniqueId());

    assertTrue(serialisableAdaptrisMessage.getContent().contains("Size=7 MB"));
  }

  @Test
  public void testSerialize() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage("Some Payload");
    adaptrisMessage.setUniqueId("uuid");
    adaptrisMessage.addMetadata("MetaKey1", "MetaValue1");
    adaptrisMessage.addMetadata("MetaKey2", "MetaValue2");
    
    SerializableMessage serialisableAdaptrisMessage = new DefaultSerializableMessageTranslator().translate(adaptrisMessage);
    
    assertEquals("MetaValue1", serialisableAdaptrisMessage.getMessageHeaders().get("MetaKey1"));
    assertEquals("MetaValue2", serialisableAdaptrisMessage.getMessageHeaders().get("MetaKey2"));
    assertEquals("Some Payload", serialisableAdaptrisMessage.getContent());
    assertEquals("uuid", serialisableAdaptrisMessage.getUniqueId());
  }

  @Test
  public void testUnserialize(TestInfo info) throws Exception {
    SerializableAdaptrisMessage serialisableAdaptrisMessage = new SerializableAdaptrisMessage();
    serialisableAdaptrisMessage.setContent("Some Payload");
    serialisableAdaptrisMessage.setUniqueId("uuid2");
    serialisableAdaptrisMessage.addMetadata("MetaKey3", "MetaValue3");
    serialisableAdaptrisMessage.addMetadata("MetaKey4", "MetaValue4");
    serialisableAdaptrisMessage.setNextServiceId(info.getDisplayName());
    AdaptrisMessage adaptrisMessage = new DefaultSerializableMessageTranslator().translate(serialisableAdaptrisMessage);
    
    assertEquals("MetaValue3", adaptrisMessage.getMetadataValue("MetaKey3"));
    assertEquals("MetaValue4", adaptrisMessage.getMetadataValue("MetaKey4"));
    assertEquals("Some Payload", adaptrisMessage.getContent());
    assertEquals("uuid2", adaptrisMessage.getUniqueId());
    assertEquals(info.getDisplayName(), adaptrisMessage.getNextServiceId());
  }

  @Test
  public void testUnserialize_WithEncoding() throws Exception {
    SerializableAdaptrisMessage serialisableAdaptrisMessage = new SerializableAdaptrisMessage();
    serialisableAdaptrisMessage.setContent("Some Payload");
    serialisableAdaptrisMessage.setUniqueId("uuid2");
    serialisableAdaptrisMessage.setContentEncoding("UTF-8");
    serialisableAdaptrisMessage.addMetadata("MetaKey3", "MetaValue3");
    serialisableAdaptrisMessage.addMetadata("MetaKey4", "MetaValue4");

    AdaptrisMessage adaptrisMessage = new DefaultSerializableMessageTranslator().translate(serialisableAdaptrisMessage);

    assertEquals("MetaValue3", adaptrisMessage.getMetadataValue("MetaKey3"));
    assertEquals("MetaValue4", adaptrisMessage.getMetadataValue("MetaKey4"));
    assertEquals("Some Payload", adaptrisMessage.getContent());
    assertEquals("uuid2", adaptrisMessage.getUniqueId());
    assertEquals("UTF-8", adaptrisMessage.getContentEncoding());
  }

}
