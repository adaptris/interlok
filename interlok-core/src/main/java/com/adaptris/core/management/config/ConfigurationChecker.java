package com.adaptris.core.management.config;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

public interface ConfigurationChecker {

  String getFriendlyName();

  ConfigurationCheckReport performConfigCheck(ConfigurationCheckReport report, BootstrapProperties bootProperties,
      UnifiedBootstrap bootstrapProperties);

  default ConfigurationCheckReport performConfigCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrapProperties) {
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(getFriendlyName());
    report.setCheckClassName(getClass().getCanonicalName());

    return performConfigCheck(report, bootProperties, bootstrapProperties);
  }

}
