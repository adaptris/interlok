package com.adaptris.core.services.metadata.compare;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-equals
 * @author lchan
 * 
 */
@XStreamAlias("metadata-equals")
public class Equals extends ComparatorImpl {

  public Equals() {
    super();
  }

  public Equals(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(StringUtils.equals(firstItem.getValue(), secondItem.getValue())));
  }
}
