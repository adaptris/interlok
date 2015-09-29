package com.adaptris.interlok.config;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;

public class ConstantDataDestination implements DataDestination {

  private String value;
  
  public ConstantDataDestination() {
  }
  
  @Override
  public Object getData(InterlokMessage message) throws InterlokException {
    return this.getValue();
  }

  @Override
  public void setData(InterlokMessage message, Object data) throws InterlokException {
    throw new InterlokException("setData not supported for " + this.getClass().getName());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
