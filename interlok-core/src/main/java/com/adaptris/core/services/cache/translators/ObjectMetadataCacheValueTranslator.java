package com.adaptris.core.services.cache.translators;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.cache.CacheValueTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CacheValueTranslator} that retrieves and sets the arbitrary object metadata of a message.
 * 
 * <p>
 * This can only be used with caches that allow insertion of non-{@link Serializable} objects as the objects are not guaranteed to
 * be serializable. This is considered a generic replacement for {@link JmsReplyToCacheValueTranslator} which already uses object
 * metadata.
 * </p>
 * 
 * @config object-metadata-cache-value-translator
 */
@XStreamAlias("object-metadata-cache-value-translator")
public class ObjectMetadataCacheValueTranslator implements CacheValueTranslator<Object> {

  @NotBlank
  private String metadataKey;

  public ObjectMetadataCacheValueTranslator() {

  }

  public ObjectMetadataCacheValueTranslator(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public Object getValueFromMessage(AdaptrisMessage msg) throws CoreException {
    if (msg.getObjectHeaders().containsKey(getMetadataKey())) {
      return msg.getObjectHeaders().get(getMetadataKey());
    }
    return null;
  }

  @Override
  public void addValueToMessage(AdaptrisMessage msg, Object value) throws CoreException {
    msg.addObjectHeader(getMetadataKey(), value);
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * The key in object metadata.
   * 
   * @param s the key
   * @see AdaptrisMessage#getObjectMetadata()
   */
  public void setMetadataKey(String s) {
    this.metadataKey = s;
  }

}
