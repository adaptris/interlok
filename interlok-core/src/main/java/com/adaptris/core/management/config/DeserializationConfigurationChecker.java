package com.adaptris.core.management.config;

import com.adaptris.core.Adapter;
import com.adaptris.core.util.LifecycleHelper;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DeserializationConfigurationChecker extends AdapterConfigurationChecker {

  private static final String FRIENDLY_NAME = "Configuration loading test";

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

  @Override
  protected void validate(Adapter adapter, ConfigurationCheckReport report) {
    try {
      LifecycleHelper.prepare(adapter);

    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }
  }

}
