package com.adaptris.tester.report.junit;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("property")
public class JUnitReportProperty {

  @XStreamAsAttribute
  private final String name;
  @XStreamAsAttribute
  private final String value;

  public JUnitReportProperty(final String name, final String value){
    this.name = name;
    this.value = value;
  }
}
