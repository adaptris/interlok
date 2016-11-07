package com.adaptris.tester.report.junit;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XStreamAlias("testsuite")
public class JUnitReportTestSuite {

  @XStreamAsAttribute
  private String name;
  @XStreamAsAttribute
  private String hostname;
  @XStreamAsAttribute
  private int failures;
  @XStreamAsAttribute
  private int errors;
  @XStreamAsAttribute
  private int tests;
  @XStreamAsAttribute
  private int skipped;
  @XStreamAsAttribute
  private String timestamp;
  @XStreamAsAttribute
  private double time;

  private final List<JUnitReportProperty> properties = new ArrayList<>();

  @XStreamImplicit
  private final List<JUnitReportTestCase> testCases = new ArrayList<>();

  public JUnitReportTestSuite(String name){
    this.name = name;
    this.failures = 0;
    this.errors = 0;
    this.tests = 0;
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    this.timestamp = sdf.format(date);
    try {
      InetAddress ip = InetAddress.getLocalHost();
      hostname = ip.getHostName();
    } catch (UnknownHostException e) {
      hostname = null;
    }
  }

  public void addProperty(JUnitReportProperty property){
    properties.add(property);
  }

  public List<JUnitReportProperty> getProperties() {
    return properties;
  }

  public void addTestCase(JUnitReportTestCase testCase){
    this.testCases.add(testCase);
    this.tests++;
    if(testCase.isFailure()){
      this.failures++;
    }
    if(testCase.isError()){
      this.errors++;
    }
    if(testCase.isSkipped()){
      this.skipped ++;
    }
  }

  public List<JUnitReportTestCase> getTestCases() {
    return testCases;
  }

  boolean hasFailures(){
    for(JUnitReportTestCase testCase : this.testCases) {
      if (testCase.isFailure() || testCase.isError()){
        return true;
      }
    }
    return false;
  }

  public String getName() {
    return name;
  }

  public void setTime(double time) {
    this.time = time;
  }

  public double getTime() {
    return time;
  }

  public int getTests() {
    return tests;
  }

  public int getFailures() {
    return failures;
  }

  public int getErrors() {
    return errors;
  }

  public int getSkipped() {
    return skipped;
  }
}
