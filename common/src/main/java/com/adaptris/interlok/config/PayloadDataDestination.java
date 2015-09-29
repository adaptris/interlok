package com.adaptris.interlok.config;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("payload-data-destination")
public class PayloadDataDestination implements DataDestination {

  public PayloadDataDestination() {
    
  }
  
  @Override
  public Object getData(AdaptrisMessage message) {
    return message.getStringPayload();
  }

  @Override
  public void setData(AdaptrisMessage message, Object data) {
    message.setStringPayload((String) data, message.getCharEncoding());
  }

}
