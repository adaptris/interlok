/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services.aggregator;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link ConsumeDestinationGenerator} that works with metadata.
 * 
 * @config consume-destination-from-metadata
 * 
 */
@XStreamAlias("consume-destination-from-metadata")
@DisplayOrder(order = {"destinationMetadataKey", "defaultDestination", "filterMetadataKey", "defaultFilterExpression"})
@Deprecated()
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
public class ConsumeDestinationFromMetadata implements ConsumeDestinationGenerator {
  private String defaultDestination;
  private String destinationMetadataKey;
  @AdvancedConfig
  private String defaultFilterExpression;
  @AdvancedConfig(rare = true)
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
    if (!isEmpty(key) && msg.headersContainsKey(key)) {
      result = msg.getMetadataValue(key);
    }
    return result;
  }

}
