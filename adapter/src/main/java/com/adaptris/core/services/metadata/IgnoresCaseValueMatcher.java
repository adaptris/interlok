/*
 * $RCSfile: $
 * $Revision: $
 * $Date: $
 * $Author: $
 */
package com.adaptris.core.services.metadata;

import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Ignores case match implementation of MetadataValueMatcher for {@link MetadataValueBranchingService}.
 * 
 * @config ignores-case-value-matcher
 * 
 * @author lchan
 */
@XStreamAlias("ignores-case-value-matcher")
public class IgnoresCaseValueMatcher implements MetadataValueMatcher {

  public IgnoresCaseValueMatcher() {
  }

  /**
   * @see MetadataValueMatcher#getNextServiceId(java.lang.String,
   *      KeyValuePairSet)
   */
  public String getNextServiceId(String serviceKey, KeyValuePairSet mappings) {
    return mappings.getValueIgnoringKeyCase(serviceKey);
  }

}
