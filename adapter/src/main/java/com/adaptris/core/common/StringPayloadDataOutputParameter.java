package com.adaptris.core.common;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code DataOutputParameter} is used when you want to source data from the {@link com.adaptris.core.AdaptrisMessage} payload.
 * <p>
 * An example might be specifying that the XML content required for the {@link com.adaptris.core.services.xml.XPathService} can be
 * found in the payload of an {@link com.adaptris.core.AdaptrisMessage}.
 * </p>
 * 
 * @author amcgrath
 * @config string-payload-data-output-parameter
 * @license BASIC
 */
@XStreamAlias("string-payload-data-output-parameter")
public class StringPayloadDataOutputParameter implements DataOutputParameter<String> {

  private String contentEncoding;
  public StringPayloadDataOutputParameter() {
    
  }

  @Override
  public void insert(String data, InterlokMessage msg) throws InterlokException {
    String encoding = defaultIfEmpty(getContentEncoding(), msg.getContentEncoding());
    msg.setContent(data, encoding);
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

}
