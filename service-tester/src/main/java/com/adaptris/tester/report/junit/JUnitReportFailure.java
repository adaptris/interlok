package com.adaptris.tester.report.junit;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("failure")
@XStreamConverter(value=ToAttributedValueConverter.class, strings={"text"})
public class JUnitReportFailure extends JUnitReportTestIssueTyped {

  @XStreamAsAttribute
  @XStreamAlias("type")
  private final static String TYPE = "failure";
  private String text;


  public JUnitReportFailure(String message) {
    super(message, TYPE);
  }

  public JUnitReportFailure(String message, String text) {
    super(message, TYPE);
    this.text = text;
  }
}
