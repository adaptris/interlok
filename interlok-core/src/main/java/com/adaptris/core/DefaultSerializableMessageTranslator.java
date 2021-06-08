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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class DefaultSerializableMessageTranslator implements SerializableMessageTranslator {

  private AdaptrisMessageFactory messageFactory;
  // Set a 1Mb boundary for FileBackedMessages.
  private static final long DEFAULT_LMS_BOUNDARY = 1024 * 1024;

  public DefaultSerializableMessageTranslator() {
    messageFactory = new DefaultMessageFactory();
  }

  @Override
  public AdaptrisMessageFactory currentMessageFactory() {
    return messageFactory;
  }

  @Override
  public void registerMessageFactory(AdaptrisMessageFactory messageFactory) {
    this.messageFactory = messageFactory;
  }

  @Override
  public SerializableMessage translate(AdaptrisMessage message) throws CoreException {
    SerializableAdaptrisMessage serializedMsg = new SerializableAdaptrisMessage();
    // It's a file message; arbitrarily too large?
    if (message instanceof FileBackedMessage && message.getSize() > DEFAULT_LMS_BOUNDARY) {
      serializedMsg.setContent(buildFileDetails(((FileBackedMessage) message).currentSource()));
    }
    else {
      serializedMsg.setContent(message.getContent());
    }
    serializedMsg.setUniqueId(message.getUniqueId());
    serializedMsg.setContentEncoding(message.getContentEncoding());
    serializedMsg.setMetadata(message.getMetadata());
    serializedMsg.setNextServiceId(message.getNextServiceId());
    
    // do we have a failed/error'd message?
    if(message.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION))
      serializedMsg.addMetadata(CoreConstants.OBJ_METADATA_EXCEPTION, ((Throwable) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION)).getMessage());
      
    return serializedMsg;
  }

  @Override
  public AdaptrisMessage translate(SerializableMessage message) throws CoreException {
    try {
      AdaptrisMessage adaptrisMessage = null;
      if (StringUtils.isEmpty(message.getContentEncoding())) {
        adaptrisMessage = messageFactory.newMessage(message.getContent(), convertMap(message.getMessageHeaders()));
      }
      else {
        adaptrisMessage = messageFactory.newMessage(message.getContent(), message.getContentEncoding(),
            convertMap(message.getMessageHeaders()));
      }

      if (message.getMessageHeaders().containsKey("_interlokMessageSerialization")) {
        if (message.getMessageHeaders().get("_interlokMessageSerialization").equals("BASE64")) {
          adaptrisMessage.setPayload(Base64.getDecoder().decode(message.getContent()));
        }
      }

      if(StringUtils.isEmpty(message.getUniqueId()))
        message.setUniqueId(new GuidGenerator().create(this));
        
      adaptrisMessage.setUniqueId(message.getUniqueId());
      adaptrisMessage.setNextServiceId(message.getNextServiceId());

      return adaptrisMessage;

    }
    catch (UnsupportedEncodingException ex) {
      throw new CoreException(ex);
    }
  }

  private Set<MetadataElement> convertKeyValuePairs(KeyValuePairSet set) {
    Set<MetadataElement> result = new HashSet<MetadataElement>();
    for (KeyValuePair kvp : set) {
      result.add(new MetadataElement(kvp));
    }
    return result;
  }

  private Set<MetadataElement> convertMap(Map<String, String> set) {
    return convertKeyValuePairs(new KeyValuePairSet(set));
  }

  private String buildFileDetails(File f) throws CoreException {
    String result = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
      result = new ToStringBuilder(f, ToStringStyle.MULTI_LINE_STYLE).append("Path", f.getCanonicalPath())
          .append("Size", FileUtils.byteCountToDisplaySize(f.length()))
          .append("LastModified", sdf.format(new Date(f.lastModified()))).toString();
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }
}
