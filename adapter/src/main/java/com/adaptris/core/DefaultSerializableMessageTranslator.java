package com.adaptris.core;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.util.ExceptionHelper;
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
  public SerializableAdaptrisMessage translate(AdaptrisMessage message) throws CoreException {
    SerializableAdaptrisMessage serializedMsg = new SerializableAdaptrisMessage();
    // It's a file message; arbitrarily too large?
    if (message instanceof FileBackedMessage && message.getSize() > DEFAULT_LMS_BOUNDARY) {
      serializedMsg.setContent(buildFileDetails(((FileBackedMessage) message).currentSource()));
    }
    else {
      serializedMsg.setContent(message.getStringPayload());
    }
    serializedMsg.setUniqueId(message.getUniqueId());
    serializedMsg.setContentEncoding(message.getCharEncoding());
    serializedMsg.setMetadata(message.getMetadata());
    
    // do we have a failed/error'd message?
    if(message.getObjectMetadata().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION))
      serializedMsg.addMetadata(CoreConstants.OBJ_METADATA_EXCEPTION, ((Throwable) message.getObjectMetadata().get(CoreConstants.OBJ_METADATA_EXCEPTION)).getMessage());
      
    return serializedMsg;
  }

  @Override
  public AdaptrisMessage translate(SerializableAdaptrisMessage message) throws CoreException {
    try {
      AdaptrisMessage adaptrisMessage = null;
      if (StringUtils.isEmpty(message.getContentEncoding())) {
        adaptrisMessage = messageFactory.newMessage(message.getContent(), convertKeyValuePairs(message.getMetadata()));
      }
      else {
        adaptrisMessage = messageFactory.newMessage(message.getContent(), message.getContentEncoding(),
            convertKeyValuePairs(message.getMetadata()));
      }
      if(StringUtils.isEmpty(message.getUniqueId()))
        message.setUniqueId(new GuidGenerator().create(this));
        
      adaptrisMessage.setUniqueId(message.getUniqueId());
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
