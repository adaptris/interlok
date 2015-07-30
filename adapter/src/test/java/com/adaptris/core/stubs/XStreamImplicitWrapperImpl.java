package com.adaptris.core.stubs;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

//For testing the @@XStreamImplicit annotation being parsed by adp-core-apt for for XStreamMarshaller
public abstract class XStreamImplicitWrapperImpl {

  private String marshalledIdentity;

  @XStreamImplicit(itemFieldName = "parent-string")
  private List<String> parentStrings = new ArrayList<String>();

  public XStreamImplicitWrapperImpl() {
    marshalledIdentity = new GuidGenerator().getUUID();
  }

  public String getMarshalledIdentity() {
    return marshalledIdentity;
  }

  public void setMarshalledIdentity(String marshalledIdentity) {
    this.marshalledIdentity = marshalledIdentity;
  }

  public List<String> getParentStrings() {
    return parentStrings;
  }

  public void setParentStrings(List<String> list) {
    parentStrings = list;
  }

  public void addParentString(String s) {
    parentStrings.add(s);
  }
}
