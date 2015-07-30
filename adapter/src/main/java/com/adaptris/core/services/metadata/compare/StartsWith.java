package com.adaptris.core.services.metadata.compare;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-starts-with
 * @author lchan
 * 
 */
@XStreamAlias("metadata-starts-with")
public class StartsWith extends ComparatorImpl {

  public StartsWith() {
    super();
  }

  public StartsWith(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(StringUtils.startsWith(firstItem.getValue(),
        secondItem.getValue())));
  }
}
