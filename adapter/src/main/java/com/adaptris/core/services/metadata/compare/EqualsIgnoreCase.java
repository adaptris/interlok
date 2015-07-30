package com.adaptris.core.services.metadata.compare;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-equals-ignore-case
 * @author lchan
 * 
 */
@XStreamAlias("metadata-equals-ignore-case")
public class EqualsIgnoreCase extends ComparatorImpl {

  public EqualsIgnoreCase() {
    super();
  }

  public EqualsIgnoreCase(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(StringUtils.equalsIgnoreCase(firstItem.getValue(),
        secondItem.getValue())));
  }
}
