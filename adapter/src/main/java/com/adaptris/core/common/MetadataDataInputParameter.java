package com.adaptris.core.common;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@code DataInputParameter} is used when you want to source data from the {@link com.adaptris.core.AdaptrisMessage} metadata.
 * </p>
 * <p>
 * An example might be specifying that the XPath expression required for the {@link
 * com.adaptris.core.services.xml.XPathService} can be found in
 * a particular metadata item of an {@link com.adaptris.core.AdaptrisMessage}.
 * </p>
 * 
 * @author amcgrath
 * @config metadata-data-input-parameter
 * @license BASIC
 */
@XStreamAlias("metadata-data-input-parameter")
public class MetadataDataInputParameter implements DataInputParameter<String> {
  
  private static final String DEFAULT_METADATA_KEY = "destinationKey";

  @NotBlank
  @AutoPopulated
  private String metadataKey;
  
  public MetadataDataInputParameter() {
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  public MetadataDataInputParameter(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public String extract(InterlokMessage message) {
    return message.getMessageHeaders().get(this.getMetadataKey());
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String key) {
    this.metadataKey = Args.notNull(key, "metadata key");
  }

}
