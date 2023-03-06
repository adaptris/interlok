package com.adaptris.core.services.cache.translators;

import javax.validation.constraints.NotBlank;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.cache.CacheKeyTranslator;
import com.adaptris.core.services.cache.CacheValueTranslator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CacheValueTranslator} that maps values to / from metadata on the message.
 * 
 * @config metadata-cache-value-translator
 * 
 * @author stuellidge
 * 
 */
@JacksonXmlRootElement(localName = "metadata-cache-value-translator")
@XStreamAlias("metadata-cache-value-translator")
public class MetadataCacheValueTranslator implements CacheValueTranslator<String>, CacheKeyTranslator {

  @NotBlank
  private String metadataKey;

  public MetadataCacheValueTranslator() {

  }

  public MetadataCacheValueTranslator(String s) {
    setMetadataKey(s);
  }

  /**
   * Retrieves the piece of metadata associated with the configured metadataKey
   */
  @Override
  public String getValueFromMessage(AdaptrisMessage msg) throws CoreException {
    return msg.getMetadataValue(getMetadataKey());
  }

  /**
   * Sets the result of calling toString() on the supplied {@link Object} as
   * the value of the metadata item associated with the configured metadataKey
   */
  @Override
  public void addValueToMessage(AdaptrisMessage msg, String value) throws CoreException {
    msg.addMetadata(getMetadataKey(), value);
  }

  /**
   * The metadata key to access
   * @param metadataKey
   */
  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  @Override
  public String getKeyFromMessage(AdaptrisMessage msg) throws CoreException {
    return getValueFromMessage(msg);
  }

}
