package com.adaptris.interlok.config;

public class ConstantDataDestination implements DataDestination {

  private String value;
  
  public ConstantDataDestination() {
  }
  
  @Override
  public Object getData(AdaptrisMessage message) throws CoreException {
    return this.getValue();
  }

  @Override
  public void setData(AdaptrisMessage message, Object data) throws CoreException {
    throw new CoreException("setData not supported for " + this.getClass().getName());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
