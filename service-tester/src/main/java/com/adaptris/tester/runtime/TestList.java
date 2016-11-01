package com.adaptris.tester.runtime;

import com.adaptris.tester.report.junit.JUnitReportTestSuite;
import com.adaptris.tester.report.junit.JUnitReportTestSuites;
import com.adaptris.tester.runtime.clients.TestClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@XStreamAlias("test-list")
public class TestList extends AbstractCollection<Test> implements TestComponent {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private String uniqueId;
  @XStreamImplicit
  private List<Test> testCases;

  public TestList(){
    testCases = new ArrayList<Test>();
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  public void setTestCases(List<Test> testCases) {
    this.testCases = testCases;
  }

  public List<Test> getTestCases() {
    return testCases;
  }

  public void addTestCase(Test testCase){
    add(testCase);
  }

  @Override
  public Iterator<Test> iterator() {
    return testCases.listIterator();
  }

  @Override
  public int size() {
    return testCases.size();
  }

  JUnitReportTestSuites execute(TestClient client) throws ServiceTestException {
    JUnitReportTestSuites result = new JUnitReportTestSuites(uniqueId);
    for (Test testCase : testCases) {
      JUnitReportTestSuite suite = testCase.execute(uniqueId, client);
      result.addTestSuite(suite);
    }
    return result;
  }
}
