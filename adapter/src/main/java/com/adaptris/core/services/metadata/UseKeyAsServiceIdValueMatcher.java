package com.adaptris.core.services.metadata;

import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of MetadataValueMatcher for {@link MetadataValueBranchingService} which returns the serviceKey as identifier of
 * the next Service to apply.
 * <p>
 * This simply allows MetadataValueBranchingService to be used without maintaining a set of mappings between metadata keys and
 * service IDs where the relationship is 1 to 1.
 * </p>
 * 
 * @config use-key-as-service-id-value-matcher
 */
@XStreamAlias("use-key-as-service-id-value-matcher")
public class UseKeyAsServiceIdValueMatcher implements MetadataValueMatcher {

  public String getNextServiceId(String serviceKey, KeyValuePairSet mappings) {
    return serviceKey;
  }
}
