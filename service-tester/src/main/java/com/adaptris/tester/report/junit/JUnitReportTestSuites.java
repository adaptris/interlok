package com.adaptris.tester.report.junit;


import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.tester.runtime.ServiceTestException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("testsuites")
public class JUnitReportTestSuites {


  @XStreamOmitField
  private String name;
  @XStreamImplicit
  private final List<JUnitReportTestSuite> testSuites;

  public JUnitReportTestSuites(String name){
    this.name = name;
    this.testSuites = new ArrayList<>();
  }

  public void addTestSuite(JUnitReportTestSuite JUnitReportTestSuite){
    this.testSuites.add(JUnitReportTestSuite);
  }

  public void writeReports(final File outputDirectory) throws ServiceTestException{
    try {
      for (JUnitReportTestSuite suite : testSuites) {
        String result = DefaultMarshaller.getDefaultMarshaller().marshal(suite);
        FileUtils.writeStringToFile(new File(outputDirectory, "TEST-" + suite.getName() + ".xml"), result, StandardCharsets.UTF_8);
      }
    } catch (CoreException | IOException e) {
      throw new ServiceTestException(e);
    }
  }

  boolean hasFailures(){
    for(JUnitReportTestSuite testSuites : this.testSuites) {
      if (testSuites.hasFailures()){
        return true;
      }
    }
    return false;
  }
}
