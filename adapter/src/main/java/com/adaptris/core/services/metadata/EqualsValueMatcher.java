package com.adaptris.core.services.metadata;

import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Exact value match implementation of MetadataValueMatcher for {@link MetadataValueBranchingService}.
 * 
 * @config equals-value-matcher
 * 
 * @see MetadataValueBranchingService
 * @author lchan
 */
@XStreamAlias("equals-value-matcher")
public class EqualsValueMatcher implements MetadataValueMatcher {

  public EqualsValueMatcher() {
  }

  /**
   * @see MetadataValueMatcher#getNextServiceId(java.lang.String,
   *      KeyValuePairSet)
   */
  public String getNextServiceId(String serviceKey, KeyValuePairSet mappings) {
    return mappings.getValue(serviceKey);
  }

}
