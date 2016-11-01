package com.adaptris.tester.runtime;

import com.adaptris.tester.report.junit.JUnitReportFailure;
import com.adaptris.tester.runtime.messages.assertion.AssertionResult;
import com.adaptris.tester.report.junit.JUnitReportTestIssue;
import com.adaptris.tester.runtime.messages.TestMessage;
import com.adaptris.tester.runtime.messages.assertion.Assertion;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@XStreamAlias("assertions")
public class Assertions extends AbstractCollection<Assertion> {

  @XStreamImplicit
  private List<Assertion> assertions;

  public Assertions(){
    assertions = new ArrayList<>();
  }

  @Override
  public Iterator<Assertion> iterator() {
    return assertions.listIterator();
  }

  @Override
  public int size() {
    return assertions.size();
  }

  public void setAssertions(List<Assertion> assertion) {
    this.assertions = assertion;
  }

  public List<Assertion> getAssertions() {
    return assertions;
  }

  public void addMatcher(Assertion assertion){
    this.assertions.add(assertion);
  }

  public JUnitReportTestIssue execute(TestMessage returnMessage){
    JUnitReportTestIssue result = null;
    for(Assertion assertion : getAssertions()) {
      AssertionResult assertionResult = assertion.execute(returnMessage);
      if (!assertionResult.isPassed()) {
        String message = "Returned\n"
            + returnMessage.toString()
            + "\n\n"
            + "Expected\n"
            + assertion.expected();
        result = new JUnitReportFailure(assertionResult.getMessage(),message);
        break;
      }
    }
    return result;
  }
}
