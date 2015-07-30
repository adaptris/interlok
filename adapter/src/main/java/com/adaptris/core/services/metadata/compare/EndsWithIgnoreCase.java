package com.adaptris.core.services.metadata.compare;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-ends-with-ignore-case
 * @author lchan
 * 
 */
@XStreamAlias("metadata-ends-with-ignore-case")
public class EndsWithIgnoreCase extends ComparatorImpl {

  public EndsWithIgnoreCase() {
    super();
  }

  public EndsWithIgnoreCase(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(StringUtils.endsWithIgnoreCase(firstItem.getValue(),
        secondItem.getValue())));
  }
}
