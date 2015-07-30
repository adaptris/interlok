package com.adaptris.core.services.metadata.compare;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-contains
 * @author lchan
 * 
 */
@XStreamAlias("metadata-contains")
public class Contains extends ComparatorImpl {

  public Contains() {
    super();
  }

  public Contains(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(StringUtils.contains(firstItem.getValue(),
        secondItem.getValue())));
  }
}
