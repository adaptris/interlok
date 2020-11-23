package com.adaptris.core.management.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

public class ConfigurationReportRunner {

  private transient Collection<ConfigurationReporter> reporters = new ArrayList<>();

  public ConfigurationReportRunner() {
    for (ConfigurationReporter r : ServiceLoader.load(ConfigurationReporter.class)) {
      reporters.add(r);
    }
  }

  public boolean report(Collection<ConfigurationCheckReport> reports) {
    boolean result = true;
    for (ConfigurationReporter r : reporters) {
      boolean reportResult = r.report(reports);
      if (!reportResult) {
        result = false;
      }
    }
    return result;
  }

}
