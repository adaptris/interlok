package com.adaptris.core.management.config;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public enum ConfigurationCheckersEnum {
  
  // **************
  // Available checkers

  DESERIALIZATION (new DeserializationConfigurationChecker()),
  
  CLASSPATH_DUPLICATION (new ClasspathDupConfigurationChecker()),
  
  SHARED_CONNECTION_USAGE (new SharedConnectionConfigurationChecker());
  
//**************
  
  private ConfigurationChecker checker;
  
  ConfigurationCheckersEnum(ConfigurationChecker configurationChecker) {
    this.setChecker(configurationChecker);
  }
  
  public void performCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) throws ConfigurationException {
    this.getChecker().performConfigCheck(bootProperties, bootstrap);
  };

  public ConfigurationChecker getChecker() {
    return checker;
  }

  public void setChecker(ConfigurationChecker checker) {
    this.checker = checker;
  }
}
