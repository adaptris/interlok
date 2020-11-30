package com.adaptris.core.management.config;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConsoleReporter implements ConfigurationReporter {

  @Override
  public boolean report(Collection<ConfigurationCheckReport> reports) {
    AtomicBoolean success = new AtomicBoolean(true);
    reports.forEach(report -> {
      System.err.println("\n" + report.toString());
      if (report.getFailureExceptions().size() > 0)
        success.set(false);
    });
    return success.get();
  }

}
