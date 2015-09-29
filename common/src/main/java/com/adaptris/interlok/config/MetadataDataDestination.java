package com.adaptris.interlok.config;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("metadata-data-destination")
public class MetadataDataDestination implements DataDestination {
  
  private static final String DEFAULT_METADATA_KEY = "destinationKey";

  private String metadataKey;
  
  public MetadataDataDestination() {
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  @Override
  public Object getData(AdaptrisMessage message) {
    return message.getMetadataValue(this.getMetadataKey());
  }

  @Override
  public void setData(AdaptrisMessage message, Object data) {
    message.addMetadata(this.getMetadataKey(), (String) data);
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
