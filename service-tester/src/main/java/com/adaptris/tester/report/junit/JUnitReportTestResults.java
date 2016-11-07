package com.adaptris.tester.report.junit;

import com.adaptris.tester.runtime.ServiceTestException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JUnitReportTestResults {

  private final String name;
  private final List<JUnitReportTestSuites> testSuites;

  public JUnitReportTestResults(final String name){
    this.name = name;
    this.testSuites = new ArrayList<>();
  }

  public void addTestSuites(final JUnitReportTestSuites testSuites){
    this.testSuites.add(testSuites);
  }

  public void writeReports(final File outputDirectory) throws ServiceTestException {
    for(JUnitReportTestSuites testSuites : this.testSuites) {
      testSuites.writeReports(outputDirectory);
    }
  }

  public boolean hasFailures(){
    for(JUnitReportTestSuites testSuites : this.testSuites) {
      if (testSuites.hasFailures()){
        return true;
      }
    }
    return false;
  }

}
