package com.adaptris.core.runtime;

import java.net.URL;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.BootstrapProperties;

/**
 * <p>
 * An abstract implementation of the {@link ConfigurationPreProcessor}
 * </p>
 * <p>
 * All concrete classes will have access to the bootstrap.properties should their pre-processing
 * need extra configuration.
 * </p>
 * @author amcgrath
 *
 */
public abstract class AbstractConfigurationPreProcessor implements ConfigurationPreProcessor {

  private BootstrapProperties bootstrapProperties;
  
  public AbstractConfigurationPreProcessor(BootstrapProperties properties) {
    this.setBootstrapProperties(properties);
  }
  
  @Override
  public abstract String process(String xml) throws CoreException;

  @Override
  public abstract String process(URL urlToXml) throws CoreException;

  public BootstrapProperties getBootstrapProperties() {
    return bootstrapProperties;
  }

  public void setBootstrapProperties(BootstrapProperties bootstrapProperties) {
    this.bootstrapProperties = bootstrapProperties;
  }

}
