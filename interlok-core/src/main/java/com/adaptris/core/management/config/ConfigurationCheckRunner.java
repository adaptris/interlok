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
      reports.add(runCheck(checker, bootProperties, bootstrap)); 
    });
    
    return reports;
  }

  private ConfigurationCheckReport runCheck(ConfigurationCheckersEnum configCheck, BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) {
    System.err.println("\n\n**************************************************");
    System.err.println("Performing configuration check: " + configCheck.getChecker().getFriendlyName());
    System.err.println(configCheck.getChecker().getDescription() + "\n");
    
    return configCheck.performCheck(bootProperties, bootstrap);    
  }
  
}
