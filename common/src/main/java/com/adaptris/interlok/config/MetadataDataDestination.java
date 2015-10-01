package com.adaptris.interlok.config;

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
public class MetadataDataDestination implements DataDestination {
  
  private static final String DEFAULT_METADATA_KEY = "destinationKey";

  private String metadataKey;
  
  public MetadataDataDestination() {
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  @Override
  public Object getData(InterlokMessage message) {
    return message.getMessageHeaders().get(this.getMetadataKey());
  }

  @Override
  public void setData(InterlokMessage message, Object data) {
    message.addMessageHeader(this.getMetadataKey(), (String) data);
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
