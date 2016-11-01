package com.adaptris.tester.runtime.services.sources;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("inline-source")
public class InlineSource implements Source {

  public String xml;

  @Override
  public String getSource() {
    return getXml();
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  public String getXml() {
    return xml;
  }


}
