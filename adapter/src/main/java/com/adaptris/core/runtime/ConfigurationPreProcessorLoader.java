package com.adaptris.core.runtime;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.BootstrapProperties;

public interface ConfigurationPreProcessorLoader {
  
  PreProcessorsList load(BootstrapProperties bootstrapProperties) throws CoreException;

}
