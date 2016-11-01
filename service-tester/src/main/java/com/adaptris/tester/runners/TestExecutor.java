package com.adaptris.tester.runners;

import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.tester.report.junit.JUnitReportTestResults;
import com.adaptris.tester.runtime.ServiceTest;
import com.adaptris.tester.runtime.ServiceTestException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class TestExecutor {

  public static void main(String args[]) throws ServiceTestException, CoreException, IOException {
    String inputFilePath = args[0];
    String outputFilePath = args[1];
    execute(new File(inputFilePath), new File(outputFilePath));
  }

  public static void execute(File input, File outputDirectory) throws ServiceTestException {
    try {
      final byte[] encoded = Files.readAllBytes(input.toPath());
      final String contents = new String(encoded, Charset.defaultCharset());
      final JUnitReportTestResults result = execute(contents);
      result.writeReports(outputDirectory);
      if(result.hasFailures()){
        System.exit(1);
      }
    } catch (IOException e) {
      throw new ServiceTestException(e);
    }
  }

  public static JUnitReportTestResults execute(String text) throws ServiceTestException {
    try {
      ServiceTest serviceTest = (ServiceTest) DefaultMarshaller.getDefaultMarshaller().unmarshal(text);
      return execute(serviceTest);
    } catch (CoreException e) {
      throw new ServiceTestException(e);
    }
  }

  public static JUnitReportTestResults execute(ServiceTest serviceTest) throws ServiceTestException {
    return serviceTest.execute();
  }
}
