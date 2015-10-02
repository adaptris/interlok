package com.adaptris.core.common;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.config.DataDestination;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link DataDestination} is used when you want to source/target data to/from the {@link AdaptrisMessage}'s metadata.
 * </p>
 * <p>
 * An example might be specifying that the XPath expression required for the {@link XPathService} can be found in
 * a particular metadata item of an {@link AdaptrisMessage}.
 * </p>
 * 
 * @author amcgrath
 * @config metadata-data-destination
 * @license BASIC
 */
@XStreamAlias("metadata-data-destination")
public class MetadataDataDestination implements DataDestination<String> {
  
  private static final String DEFAULT_METADATA_KEY = "destinationKey";

  @NotBlank
  @AutoPopulated
  private String metadataKey;
  
  public MetadataDataDestination() {
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  @Override
  public String getData(InterlokMessage message) {
    return message.getMessageHeaders().get(this.getMetadataKey());
  }

  @Override
  public void setData(InterlokMessage message, String data) {
    message.addMessageHeader(this.getMetadataKey(), data);
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String key) {
    this.metadataKey = Args.notNull(key, "metadata key");
  }

}
