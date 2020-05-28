package com.adaptris.core.management.config;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public interface ConfigurationChecker {

  public String getFriendlyName();
  
  public String getDescription();
  
  public ConfigurationCheckReport performConfigCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrapProperties);
  
}
