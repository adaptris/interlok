package com.adaptris.core.management.config;

import java.util.Arrays;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public class ConfigurationCheckRunner {

  public void runChecks(BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) {
    Arrays.asList(ConfigurationCheckersEnum.values()).forEach( checker -> runCheck(checker, bootProperties, bootstrap) );
  }

  private void runCheck(ConfigurationCheckersEnum configCheck, BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) {
    System.err.println("\n\n**************************************************");
    System.err.println("Performing configuration check: " + configCheck.getChecker().getFriendlyName());
    System.err.println(configCheck.getChecker().getDescription() + "\n");
    
    try {
      configCheck.performCheck(bootProperties, bootstrap);
      
      System.err.println(configCheck.getChecker().getFriendlyName() + " Passed.");
    } catch (ConfigurationException exception) {
      System.err.println(configCheck.getChecker().getFriendlyName() + " Failed with exception(s):");
      System.err.println(exception.getMessage());
    }
    
  }
  
}
