package com.adaptris.core.management.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public class ConfigurationCheckRunner {

  private ServiceLoader<ConfigurationChecker> configurationCheckers = ServiceLoader.load(ConfigurationChecker.class);

  public List<ConfigurationCheckReport> runChecks(BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) {
    List<ConfigurationCheckReport> reports = new ArrayList<>();
    configurationCheckers.forEach(checker -> {
      reports.add(checker.performConfigCheck(bootProperties, bootstrap));
    });

    return reports;
  }

}
