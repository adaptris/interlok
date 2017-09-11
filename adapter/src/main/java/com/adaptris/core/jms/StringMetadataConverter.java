package com.adaptris.core.jms;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.metadata.MetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <code>MetadataElement</code> key and value set as property of <code>javax.jms.Message</code>
 * using <code>setStringProperty(String key, String value)</code>.
 *
 * @config jms-string-metadata-converter
 * @author mwarman
 */
@XStreamAlias("jms-string-metadata-converter")
@DisplayOrder(order = {"metadataFilter"})
public class StringMetadataConverter extends MetadataConverter {


  /** @see MetadataConverter#MetadataConverter() */
  public StringMetadataConverter() {
    super();
  }

  public StringMetadataConverter(MetadataFilter metadataFilter) {
    super(metadataFilter);
  }

}
