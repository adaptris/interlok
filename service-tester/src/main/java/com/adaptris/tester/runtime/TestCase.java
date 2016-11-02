package com.adaptris.tester.runtime;

import com.adaptris.tester.report.junit.*;
import com.adaptris.tester.runtime.clients.TestClient;
import com.adaptris.tester.runtime.messages.TestMessage;
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
  private TestMessage inputMessage;
  private Assertions assertions;
  private ExpectedException expectedException;
  @XStreamOmitField
  private String globFilter;

  public TestCase(){
    assertions = new Assertions();
    expectedException = null;
    if (System.getProperties().containsKey(TEST_FILTER)) {
      globFilter = System.getProperty(TEST_FILTER);
    } else {
      globFilter = "*";
    }
  }
  public TestCase(final String globFilter){
    this.globFilter = globFilter;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  public void setInputMessage(TestMessage inputMessage) {
    this.inputMessage = inputMessage;
  }

  public TestMessage getInputMessage() {
    return inputMessage;
  }

  public void setAssertions(Assertions assertions) {
    this.assertions = assertions;
  }

  public Assertions getAssertions() {
    return assertions;
  }

  boolean isTestToBeExecuted(final String fqName){
    String regexFilter = createRegexFromGlob(globFilter);
    return fqName.matches(regexFilter);
  }

  JUnitReportTestCase execute(String parentId, TestClient client, ServiceToTest serviceToTest) throws ServiceTestException {
    final String fqName = parentId + "." + getUniqueId();

    JUnitReportTestCase result = new JUnitReportTestCase(uniqueId);
    if(!isTestToBeExecuted(fqName)){
      result.setTestIssue(new JUnitReportSkipped());
      result.setTime(0);
      return result;
    }
    long startTime = System.nanoTime();
    try {
      TestMessage returnMessage = client.applyService(serviceToTest.getProcessedSource(), inputMessage);
      if(expectedException != null){
        //Exception should have been thrown
        result.setTestIssue(new JUnitReportFailure("Assertion Failure: Expected Exception [" + expectedException.getClassName() + "]", "No Exception thrown"));
      } else {
        result.setTestIssue(assertions.execute(returnMessage));
      }
    } catch (Exception e){
      if(expectedException == null){
        JUnitReportError issue = new JUnitReportError("Test Error: [" + e.toString() + "]", ExceptionUtils.getStackTrace(e));
        result.setTestIssue(issue);
      } else {
        JUnitReportTestIssue issue = expectedException.check(e);
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
