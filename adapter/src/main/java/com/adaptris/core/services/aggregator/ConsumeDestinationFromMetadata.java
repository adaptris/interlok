package com.adaptris.core.services.aggregator;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConsumeDestination;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link ConsumeDestinationGenerator} that works with metadata.
 * 
 * @config consume-destination-from-metadata
 * 
 */
@XStreamAlias("consume-destination-from-metadata")
public class ConsumeDestinationFromMetadata implements ConsumeDestinationGenerator {
  private String defaultDestination;
  private String destinationMetadataKey;
  private String defaultFilterExpression;
  private String filterMetadataKey;

  public String getDefaultDestination() {
    return defaultDestination;
  }

  /**
   * Set the default destination.
   * 
   * @param dest the destination.
   */
  public void setDefaultDestination(String dest) {
    this.defaultDestination = dest;
  }

  public String getDefaultFilterExpression() {
    return defaultFilterExpression;
  }

  /**
   * Set the default filter expression.
   * 
   * @param filter the default filter expression.
   */
  public void setDefaultFilterExpression(String filter) {
    this.defaultFilterExpression = filter;
  }

  public String getFilterMetadataKey() {
    return filterMetadataKey;
  }

  /**
   * Set the metadata key that will contain the filter expression.
   * 
   * @param key
   */
  public void setFilterMetadataKey(String key) {
    this.filterMetadataKey = key;
  }


  /**
   * @return the destinationMetadataKey
   */
  public String getDestinationMetadataKey() {
    return destinationMetadataKey;
  }

  /**
   * Set the metadata key that will contain the destination.
   * 
   * @param key the destinationMetadataKey to set
   */
  public void setDestinationMetadataKey(String key) {
    this.destinationMetadataKey = key;
  }

  @Override
  public ConsumeDestination generate(AdaptrisMessage msg) {
    ConfiguredConsumeDestination result = new ConfiguredConsumeDestination(getMetadataValue(msg, getDestinationMetadataKey(),
        getDefaultDestination()), getMetadataValue(msg, getFilterMetadataKey(), getDefaultFilterExpression()));
    return result;
  }

  private String getMetadataValue(AdaptrisMessage msg, String key, String defaultValue) {
    String result = defaultValue;
    if (!isEmpty(key) && msg.containsKey(key)) {
      result = msg.getMetadataValue(key);
    }
    return result;
  }

}
