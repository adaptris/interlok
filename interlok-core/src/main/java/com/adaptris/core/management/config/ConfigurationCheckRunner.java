package com.adaptris.core.management.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public class ConfigurationCheckRunner {

  public List<ConfigurationCheckReport> runChecks(BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) {
    List<ConfigurationCheckReport> reports = new ArrayList<>();
    Arrays.asList(ConfigurationCheckersEnum.values()).forEach( checker -> {
      reports.add(checker.performCheck(bootProperties, bootstrap)); 
    });
    
    return reports;
  }
  
}
