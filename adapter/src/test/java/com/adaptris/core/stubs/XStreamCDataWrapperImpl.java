package com.adaptris.core.stubs;

import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.util.GuidGenerator;

// For testing the @CDATA annotation is doing it's job for XStreamMarshaller
public abstract class XStreamCDataWrapperImpl {

  @MarshallingCDATA
  private String parentRawValue;

  public XStreamCDataWrapperImpl() {
    parentRawValue = new GuidGenerator().getUUID();
  }

  public String getParentRawValue() {
    return parentRawValue;
  }

  public void setParentRawValue(String s) {
    parentRawValue = s;
  }

}
