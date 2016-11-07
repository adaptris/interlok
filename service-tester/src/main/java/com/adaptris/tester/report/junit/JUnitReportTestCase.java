package com.adaptris.tester.report.junit;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("testcase")
public class JUnitReportTestCase {


  @XStreamAsAttribute
  private final String name;
  @XStreamAsAttribute
  private String classname;
  @XStreamAsAttribute
  private double time;

  @XStreamImplicit
  private List<JUnitReportTestIssue> issue;

  public JUnitReportTestCase(final String name){
    this.name = name;
  }

  public boolean isFailure(){
    return issue != null && issue.get(0) instanceof JUnitReportFailure;
  }

  public boolean isError(){
    return issue != null && issue.get(0) instanceof JUnitReportError;
  }

  public boolean isSkipped(){
    return issue != null && issue.get(0) instanceof JUnitReportSkipped;
  }

  public String getName() {
    return name;
  }

  public void setTestIssue(JUnitReportTestIssue failure) {
    if(failure != null) {
      issue = new ArrayList<>();
      this.issue.add(failure);
    }
  }

  public List<JUnitReportTestIssue> getIssue() {
    return issue;
  }

  public void setTime(double time) {
    this.time = time;
  }

  public double getTime() {
    return time;
  }

  public void setClassname(String classname) {
    this.classname = classname;
  }

  public String getClassname() {
    return classname;
  }
}
