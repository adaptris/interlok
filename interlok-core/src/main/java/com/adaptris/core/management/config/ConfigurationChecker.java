package com.adaptris.core.management.config;

import java.util.ServiceLoader;
import com.adaptris.core.management.BootstrapProperties;

/**
 * {@link ServiceLoader} interface that allows for config checks to be performed.
 *
 *
 */
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
