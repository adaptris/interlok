package com.adaptris.interlok.config;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;

public interface DataDestination {
  
  public Object getData(InterlokMessage message) throws InterlokException;
  
  public void setData(InterlokMessage message, Object data) throws InterlokException;

}
