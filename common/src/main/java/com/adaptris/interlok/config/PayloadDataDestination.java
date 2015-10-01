package com.adaptris.interlok.config;

import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link DataDestination} is used when you want to source/target data to/from the {@link AdaptrisMessage}'s payload.
 * </p>
 * <p>
 * An example might be specifying that the XML content required for the {@link XPathService} can be found in
 * the payload of an {@link AdaptrisMessage}.
 * </p>
 * 
 * @author amcgrath
 * @config metadata-data-destination
 * @license BASIC
 */
@XStreamAlias("payload-data-destination")
public class PayloadDataDestination implements DataDestination {

  public PayloadDataDestination() {
    
  }
  
  @Override
  public Object getData(InterlokMessage message) {
    return message.getContent();
  }

  @Override
  public void setData(InterlokMessage message, Object data) {
    message.setContent((String) data, message.getCharEncoding());
  }

}
