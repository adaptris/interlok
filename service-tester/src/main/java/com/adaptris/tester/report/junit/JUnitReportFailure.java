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
    super(TYPE);
    setMessage(message);
  }

  public JUnitReportFailure(String message, String text) {
    this(message);
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
