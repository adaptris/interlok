package com.adaptris.core.services.metadata.compare;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-starts-with-ignore-case
 * @author lchan
 * 
 */
@XStreamAlias("metadata-starts-with-ignore-case")
public class StartsWithIgnoreCase extends ComparatorImpl {

  public StartsWithIgnoreCase() {
    super();
  }

  public StartsWithIgnoreCase(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(StringUtils.startsWithIgnoreCase(firstItem.getValue(),
        secondItem.getValue())));
  }
}
