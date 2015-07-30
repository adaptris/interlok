package com.adaptris.core.services.metadata.compare;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-ends-with
 * @author lchan
 * 
 */
@XStreamAlias("metadata-ends-with")
public class EndsWith extends ComparatorImpl {

  public EndsWith() {
    super();
  }

  public EndsWith(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(StringUtils.endsWith(firstItem.getValue(),
        secondItem.getValue())));
  }
}
