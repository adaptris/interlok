package com.adaptris.core.stubs;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

//For testing the @@XStreamImplicit annotation being parsed by adp-core-apt for for XStreamMarshaller
@XStreamAlias("xstream-implicit-wrapper")
public class XStreamImplicitWrapper extends XStreamImplicitWrapperImpl {

  @XStreamImplicit(itemFieldName = "marshalled-string")
  private List<String> marshalledStrings = new ArrayList<String>();

  public XStreamImplicitWrapper() {
  }

  public List<String> getMarshalledStrings() {
    return marshalledStrings;
  }

  public void setMarshalledStrings(List<String> marshalledStrings) {
    this.marshalledStrings = marshalledStrings;
  }

  public void addMarshalledString(String s) {
    marshalledStrings.add(s);
  }
}
