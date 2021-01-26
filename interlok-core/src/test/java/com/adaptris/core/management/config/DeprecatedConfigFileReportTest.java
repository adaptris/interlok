package com.adaptris.core.management.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.core.stubs.TempFileUtils;

public class DeprecatedConfigFileReportTest {

  @Test
  public void testReport_NoProperty() throws Exception {
    Object tracker = new Object();
    File f = TempFileUtils.createTrackedFile(tracker);
    assertFalse(f.exists());
    System.setProperty(WarningsToFile.SYSPROP_FILENAME, "");
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckClassName(DeprecatedConfigurationChecker.class.getCanonicalName());
    report.setCheckName("blah blah");
    assertTrue(new WarningsToFile().report(Arrays.asList(report)));
    assertFalse(f.exists());
  }

  @Test
  public void testReport_Property() throws Exception {
    Object tracker = new Object();
    File f = TempFileUtils.createTrackedFile(tracker);
    assertFalse(f.exists());
    System.setProperty(WarningsToFile.SYSPROP_FILENAME, f.getCanonicalPath());
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.getWarnings().add("hello world");
    report.setCheckClassName(DeprecatedConfigurationChecker.class.getCanonicalName());
    report.setCheckName("blah blah");
    assertTrue(new WarningsToFile().report(Arrays.asList(report)));
    assertTrue(f.exists());
    try (FileReader in = new FileReader(f)) {
      List<String> lines = IOUtils.readLines(in);
      assertEquals(1, lines.size());
      assertTrue(lines.get(0).contains("hello world"));
    }
  }

}
