package com.adaptris.core.management.config;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.config.ConfigPreProcessorLoader;
import com.adaptris.core.management.BootstrapProperties;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AdapterConfigurationChecker implements ConfigurationChecker {

  @Override
  public ConfigurationCheckReport performConfigCheck(ConfigurationCheckReport report, BootstrapProperties config) {
    try {
      String xml = ConfigPreProcessorLoader.loadInterlokConfig(config);
      Adapter adapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml);
      validate(adapter, report);
    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }
    return report;
  }

  protected abstract void validate(Adapter adapter, ConfigurationCheckReport report);

}
