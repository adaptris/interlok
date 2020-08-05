package com.adaptris.core.management.config;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public enum ConfigurationCheckersEnum {

  // **************
  // Available checkers

  DESERIALIZATION (new DeserializationConfigurationChecker()),

  CLASSPATH_DUPLICATION (new ClasspathDupConfigurationChecker()),

  JAVAX_VALIDATION(new JavaxValidationChecker()),

  SHARED_CONNECTION_USAGE(new SharedConnectionConfigurationChecker());

//**************

  private ConfigurationChecker checker;

  ConfigurationCheckersEnum(ConfigurationChecker configurationChecker) {
    setChecker(configurationChecker);
  }

  public ConfigurationCheckReport performCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) {
    return getChecker().performConfigCheck(bootProperties, bootstrap);
  };

  public ConfigurationChecker getChecker() {
    return checker;
  }

  public void setChecker(ConfigurationChecker checker) {
    this.checker = checker;
  }
}
