package com.adaptris.core.management.config;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;
import com.adaptris.core.util.LifecycleHelper;

public class DeserializationConfigurationChecker implements ConfigurationChecker {
  
  private static final String FRIENDLY_NAME = "Configuration loading test";
  
  private static final String DESCRIPTION = "This test will attempt to create an Interlok adapter from your configuration files and then pre-initialize each component.";

  @Override
  public void performConfigCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) throws ConfigurationException {
    // This seems a bit cheaty, but we're going to exit anyway, so
    // calling prepare probably makes no difference.
    try {
      Adapter clonedAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(bootstrap.createAdapter().getConfiguration());
      LifecycleHelper.prepare(clonedAdapter);
    } catch (Exception ex) {
      throw new ConfigurationException(ex);
    }
  }

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

}
