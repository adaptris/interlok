package com.adaptris.core.services.jmx;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of {@link ValueTranslator} will pull the string payload value from the
 * {@link AdaptrisMessage} to be used as a Jmx operation parameter. Conversely we can also take a
 * string result from a Jmx operation call and overwrite the payload with the new value.
 * </p>
 * 
 * @author amcgrath
 * @config jmx-payload-value-translator
 * @since 3.0.3
 */
@XStreamAlias("jmx-payload-value-translator")
public class PayloadValueTranslator implements ValueTranslator {
  
  
  @NotBlank
  @AutoPopulated
  private String type;

  public PayloadValueTranslator() {
    this.setType(DEFAULT_PARAMETER_TYPE);
  }
  
  @Override
  public Object getValue(AdaptrisMessage message) {
    return message.getContent();
  }

  @Override
  public void setValue(AdaptrisMessage message, Object object) {
    message.setContent((String) object, message.getContentEncoding());
  }

  public String getType() {
    return this.type == null ? DEFAULT_PARAMETER_TYPE : this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
