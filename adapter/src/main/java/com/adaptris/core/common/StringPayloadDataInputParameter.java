package com.adaptris.core.common;

import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@code DataInputParameter} is used when you want to source data from the {@link com.adaptris.core.AdaptrisMessage} payload.
 * </p>
 * <p>
 * An example might be specifying that the XML content required for the {@link com.adaptris.core.services.xml.XPathService} can be
 * found in
 * the payload of an {@link com.adaptris.core.AdaptrisMessage}.
 * </p>
 * 
 * @author amcgrath
 * @config string-payload-data-input-parameter
 * @license BASIC
 */
@XStreamAlias("string-payload-data-input-parameter")
public class StringPayloadDataInputParameter implements DataInputParameter<String> {

  public StringPayloadDataInputParameter() {
    
  }
  
  @Override
  public String extract(InterlokMessage message) {
    return message.getContent();
  }

}
