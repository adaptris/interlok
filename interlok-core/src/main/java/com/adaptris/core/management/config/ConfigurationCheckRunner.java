package com.adaptris.core.management.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import com.adaptris.annotation.Removal;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public class ConfigurationCheckRunner {

  private transient Collection<ConfigurationChecker> configurationCheckers = new ArrayList<>();

  public ConfigurationCheckRunner() {
    for (ConfigurationChecker r : ServiceLoader.load(ConfigurationChecker.class)) {
      configurationCheckers.add(r);
    }
  }

  public List<ConfigurationCheckReport> runChecks(BootstrapProperties bootProperties) {
    List<ConfigurationCheckReport> reports = new ArrayList<>();
    configurationCheckers.forEach(checker -> {
      reports.add(checker.performConfigCheck(bootProperties));
    });

    return reports;
  }


  /**
   * @deprecated since 3.11.0 use {{@link #runChecks(BootstrapProperties)} instead as
   *             UnifiedBootstrap is ignored.
   */
  @Deprecated
  @Removal(version = "4.0.0")
  public List<ConfigurationCheckReport> runChecks(BootstrapProperties bootProperties,
      UnifiedBootstrap bootstrap) {
    return runChecks(bootProperties);
  }

}
