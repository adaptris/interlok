package com.adaptris.core.services.jmx;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of {@link ValueTranslator} will pull an object metadata value from the
 * {@link AdaptrisMessage} to be used as a Jmx operation parameter. Conversely we can also take a
 * string result from a Jmx operation call and create a new/overwrite an existing object metadata
 * item with the new value.
 * </p>
 * <p>
 * Note that no data conversion will take place. The object class being pulled from object metadata
 * will be used as is to source the jmx operation parameter.
 * </p>
 * 
 * @author amcgrath
 * @config jmx-object-metadata-value-translator
 * @since 3.0.3
 */
@XStreamAlias("jmx-object-metadata-value-translator")
public class ObjectMetadataValueTranslator implements ValueTranslator {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
  @NotBlank
  @AutoPopulated
  private String metadataKey;

  @NotBlank
  private String type;
  
  public ObjectMetadataValueTranslator() {
  }
  
  public Object getValue(AdaptrisMessage message) {
    if(!StringUtils.isEmpty(metadataKey))
      return message.getObjectMetadata().get(this.getMetadataKey());
    else
      log.warn("No metadata key set for metadata-value-translator, no translation occuring.");
    
    return null;
  }

  public void setValue(AdaptrisMessage message, Object value) {
    if(!StringUtils.isEmpty(metadataKey))
      message.addObjectMetadata(this.getMetadataKey(), value);
    else
      log.warn("No metadata key set for metadata-value-translator, no translation occuring.");
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
