package com.adaptris.core.management.config;

import com.adaptris.core.management.BootstrapProperties;

public interface ConfigurationChecker {

  String getFriendlyName();

  ConfigurationCheckReport performConfigCheck(ConfigurationCheckReport report, BootstrapProperties bootProperties);

  default ConfigurationCheckReport performConfigCheck(BootstrapProperties bootProperties) {
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(getFriendlyName());
    report.setCheckClassName(getClass().getCanonicalName());

    return performConfigCheck(report, bootProperties);
  }

}
