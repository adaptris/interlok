package com.adaptris.core.services.metadata.compare;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-contains-ignore-case
 * @author lchan
 * 
 */
@XStreamAlias("metadata-contains-ignore-case")
public class ContainsIgnoreCase extends ComparatorImpl {

  public ContainsIgnoreCase() {
    super();
  }

  public ContainsIgnoreCase(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(StringUtils.containsIgnoreCase(firstItem.getValue(),
        secondItem.getValue())));
  }
}
