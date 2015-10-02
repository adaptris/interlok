package com.adaptris.core.common;

import com.adaptris.interlok.config.DataDestination;
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
 * @config payload-data-destination
 * @license BASIC
 */
@XStreamAlias("payload-data-destination")
public class PayloadDataDestination implements DataDestination<String> {

  public PayloadDataDestination() {
    
  }
  
  @Override
  public String getData(InterlokMessage message) {
    return message.getContent();
  }

  @Override
  public void setData(InterlokMessage message, String data) {
    message.setContent((String) data, message.getContentEncoding());
  }

}
