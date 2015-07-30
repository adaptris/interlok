package com.adaptris.core.stubs;

import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

// For testing the @GenerateBeanInfo annotation is doing it's job for XStreamMarshaller
@XStreamAlias("xstream-bean-info-wrapper")
@GenerateBeanInfo
public class XStreamBeanInfoWrapper {

  private String marshalledIdentity;
  private transient boolean setterCalled = false;

  public XStreamBeanInfoWrapper() {
    marshalledIdentity = new GuidGenerator().getUUID();
  }

  public String getMarshalledIdentity() {
    return marshalledIdentity;
  }

  public void setMarshalledIdentity(String s) {
    setterCalled = true;
    marshalledIdentity = s;
  }

  public boolean getSetterCalled() {
    return setterCalled;
  }

}
