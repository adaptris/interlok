package com.adaptris.core.management.config;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.config.ConfigPreProcessorLoader;
import com.adaptris.core.config.DefaultPreProcessorLoader;
import com.adaptris.core.management.BootstrapProperties;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AdapterConfigurationChecker implements ConfigurationChecker {

  private transient ConfigPreProcessorLoader loader = new DefaultPreProcessorLoader();

  @Override
  public ConfigurationCheckReport performConfigCheck(ConfigurationCheckReport report, BootstrapProperties config) {
    try {
      String xml = loader.load(config).process(readAdapterXml(config));
      Adapter adapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml);
      validate(adapter, report);
    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }
    return report;
  }

  private String readAdapterXml(BootstrapProperties config) throws Exception {
    String result = "";
    try (InputStream in = config.getConfigurationStream()) {
      result = IOUtils.toString(in, Charset.defaultCharset());
    }
    return result;
  }


  protected abstract void validate(Adapter adapter, ConfigurationCheckReport report);

}
