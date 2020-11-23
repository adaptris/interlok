package com.adaptris.core.management.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class ConfigurationReportRunnerTest {


  @Test
  public void testReportRunner_Success() throws Exception {
    Collection<ConfigurationCheckReport> reports = createReports(2, false);
    assertTrue(new ConfigurationReportRunner().report(reports));
  }

  @Test
  public void testReportRunner_Failure() throws Exception {
    Collection<ConfigurationCheckReport> reports = createReports(2, true);
    assertFalse(new ConfigurationReportRunner().report(reports));
  }

  private Collection<ConfigurationCheckReport> createReports(int count, boolean hasFatal) {
    List<ConfigurationCheckReport> result = new ArrayList<>();
    for (int i = 1; i < count; i++) {
      ConfigurationCheckReport r = new ConfigurationCheckReport();
      String txt = UUID.randomUUID().toString();
      r.setCheckName(txt);
      r.setCheckClassName(this.getClass().getCanonicalName());
      r.getWarnings().add("Warning for " + txt);
      result.add(r);
    }
    // Add the last one that might have a fatal
    ConfigurationCheckReport r = new ConfigurationCheckReport();
    String txt = UUID.randomUUID().toString();
    r.setCheckName(txt);
    r.setCheckClassName(this.getClass().getCanonicalName());
    r.getWarnings().add("Warning for " + txt);
    if (hasFatal) {
      r.getFailureExceptions().add(new Exception("Fatal for " + txt));
    }
    result.add(r);

    return result;
  }

}
