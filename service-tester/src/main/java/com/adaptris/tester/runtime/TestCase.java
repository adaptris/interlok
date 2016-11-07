package com.adaptris.tester.runtime;

import com.adaptris.tester.report.junit.*;
import com.adaptris.tester.runtime.clients.TestClient;
import com.adaptris.tester.runtime.messages.TestMessage;
import com.adaptris.tester.runtime.messages.TestMessageProvider;
import com.adaptris.tester.runtime.services.ServiceToTest;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.collections.bag.SynchronizedBag;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@XStreamAlias("test-case")
public class TestCase implements TestComponent {

  private static final String TEST_FILTER =  "test.glob.filter";

  private String uniqueId;
  @Deprecated
  private TestMessage inputMessage;
  private TestMessageProvider messageProvider;
  private Assertions assertions;
  private ExpectedException expectedException;
  @XStreamOmitField
  private String globFilter;

  public TestCase(){
    setAssertions(new Assertions());
    setExpectedException(null);
    if (System.getProperties().containsKey(TEST_FILTER)) {
      setGlobFilter(System.getProperty(TEST_FILTER));
    }
  }

  public TestCase(final String globFilter){
    this();
    setGlobFilter(globFilter);
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Deprecated
  public void setInputMessage(TestMessage inputMessage) {
    this.inputMessage = inputMessage;
  }

  @Deprecated
  public TestMessage getInputMessage() {
    return inputMessage;
  }

  public TestMessageProvider getMessageProvider() {
    return messageProvider;
  }

  public void setMessageProvider(TestMessageProvider messageProvider) {
    this.messageProvider = messageProvider;
  }

  public void setAssertions(Assertions assertions) {
    this.assertions = assertions;
  }

  public Assertions getAssertions() {
    return assertions;
  }

  public void setExpectedException(ExpectedException expectedException) {
    this.expectedException = expectedException;
  }

  public ExpectedException getExpectedException() {
    return expectedException;
  }

  private String globFilter(){
    return getGlobFilter() == null ? "*" : getGlobFilter();
  }

  public void setGlobFilter(String globFilter) {
    this.globFilter = globFilter;
  }

  public String getGlobFilter() {
    return globFilter;
  }

  boolean isTestToBeExecuted(final String fqName){
    String regexFilter = createRegexFromGlob(globFilter());
    return fqName.matches(regexFilter);
  }

  JUnitReportTestCase execute(String parentId, TestClient client, ServiceToTest serviceToTest) throws ServiceTestException {
    final String fqName = parentId + "." + getUniqueId();

    JUnitReportTestCase result = new JUnitReportTestCase(getUniqueId());
    if(!isTestToBeExecuted(fqName)){
      result.setTestIssue(new JUnitReportSkipped());
      result.setTime(0);
      return result;
    }
    long startTime = System.nanoTime();
    try {
      TestMessage input;
      if(getInputMessage() == null){
        input = getMessageProvider().createTestMessage();
      } else {
        input = getInputMessage();
      }
      TestMessage returnMessage = client.applyService(serviceToTest.getProcessedSource(), input);
      if(getExpectedException() != null){
        //Exception should have been thrown
        result.setTestIssue(new JUnitReportFailure("Assertion Failure: Expected Exception [" + getExpectedException().getClassName() + "]", "No Exception thrown"));
      } else {
        result.setTestIssue(getAssertions().execute(returnMessage));
      }
    } catch (Exception e){
      if(getExpectedException() == null){
        JUnitReportError issue = new JUnitReportError("Test Error: [" + e.toString() + "]", ExceptionUtils.getStackTrace(e));
        result.setTestIssue(issue);
      } else {
        JUnitReportTestIssue issue = getExpectedException().check(e);
        result.setTestIssue(issue);
      }

    }
    long endTime = System.nanoTime();
    long elapsedTime = endTime - startTime;
    result.setTime((double)elapsedTime / 1000000000.0);
    return result;
  }

  private String createRegexFromGlob(String glob)
  {
    String out = "^";
    for(int i = 0; i < glob.length(); ++i)
    {
      final char c = glob.charAt(i);
      switch(c)
      {
        case '*': out += ".*"; break;
        case '?': out += '.'; break;
        case '.': out += "\\."; break;
        case '\\': out += "\\\\"; break;
        default: out += c;
      }
    }
    out += '$';
    return out;
  }

}
