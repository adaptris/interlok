package com.adaptris.core.stubs;

import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

// For testing the @CDATA annotation is doing it's job for XStreamMarshaller
@XStreamAlias("xstream-cdata-wrapper")
public class XStreamCDataWrapper extends XStreamCDataWrapperImpl {

  @MarshallingCDATA
  private String rawValue;

  public XStreamCDataWrapper() {
    rawValue = new GuidGenerator().getUUID();
  }

  public String getRawValue() {
    return rawValue;
  }

  public void setRawValue(String s) {
    rawValue = s;
  }

}
