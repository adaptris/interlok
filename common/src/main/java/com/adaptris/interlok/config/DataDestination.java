package com.adaptris.interlok.config;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

public interface DataDestination {
  
  public Object getData(AdaptrisMessage message) throws CoreException;
  
  public void setData(AdaptrisMessage message, Object data) throws CoreException;

}
