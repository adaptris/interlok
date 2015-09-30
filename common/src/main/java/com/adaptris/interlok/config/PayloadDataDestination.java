package com.adaptris.interlok.config;

import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
