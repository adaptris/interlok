package com.adaptris.interlok.config;

import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
