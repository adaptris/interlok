package com.adaptris.tester.runtime;

import com.adaptris.tester.report.junit.JUnitReportTestResults;
import com.adaptris.tester.runtime.clients.TestClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.io.IOUtils;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isEmpty;

@XStreamAlias("service-test")
public class ServiceTest implements TestComponent {

  private String uniqueId;

  private TestClient testClient;
  @XStreamImplicit
  private List<TestList> testLists;


  public void setUniqueId(String uniqueId) {
    if (isEmpty(uniqueId)) {
      throw new IllegalArgumentException();
    }
    this.uniqueId = uniqueId;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  public void setTestClient(TestClient testClient) {
    this.testClient = testClient;
  }

  public TestClient getTestClient() {
    return testClient;
  }

  public void setTestLists(List<TestList> adapterTestLists) {
    this.testLists = adapterTestLists;
  }

  public List<TestList> getTestLists() {
    return testLists;
  }

  public void addTestList(TestList adapterTestList){
    this.testLists.add(adapterTestList);
  }

  public JUnitReportTestResults execute() throws ServiceTestException {
    testClient.init();
    try {
      JUnitReportTestResults results = new JUnitReportTestResults(uniqueId);
      for(TestList tests : getTestLists()){
        results.addTestSuites(tests.execute(testClient));
      }
      return results;
    } finally {
      IOUtils.closeQuietly(testClient);
    }
  }
}
