package com.adaptris.core.services.findreplace;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ReplacementSource} implementation which returns the metadata value when the replacement value is treated as a metadata
 * key.
 * 
 * <p>
 * Used with {@link FindAndReplaceService} to replace text in the message.
 * </p>
 * 
 * @config metadata-replacement-source
 */
@XStreamAlias("metadata-replacement-source")
public class MetadataReplacementSource extends AbstractReplacementSource {

  public MetadataReplacementSource() {
    super();
  }

  public MetadataReplacementSource(String value) {
    this();
    setValue(value);
  }

  public String obtainValue(AdaptrisMessage msg) {
    return msg.getMetadataValue(this.getValue());
  }
}
