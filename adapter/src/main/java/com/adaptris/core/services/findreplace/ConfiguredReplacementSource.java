package com.adaptris.core.services.findreplace;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ReplacementSource} implementation which returns the passed in value.
 * <p>
 * Used with {@link FindAndReplaceService} to replace text in the message.
 * </p>
 * 
 * @config configured-replacement-source
 */
@XStreamAlias("configured-replacement-source")
public class ConfiguredReplacementSource extends AbstractReplacementSource {

  public ConfiguredReplacementSource() {
    super();
  }

  public ConfiguredReplacementSource(String val) {
    this();
    setValue(val);
  }

  public String obtainValue(AdaptrisMessage msg) {
    return this.getValue();
  }
}
