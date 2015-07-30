package com.adaptris.core.management;

import javax.management.MalformedObjectNameException;

import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.AdapterRegistryMBean;

/**
 * The interface which has all the methods that provide the functionality to create,
 * save or sync the adapter.
 *
 *
 */
public interface AdapterConfigManager {

  /**
   * System property that specifies a different {@link AdapterRegistryMBean} implementation to the default.
   * 
   */
  String ADAPTER_REGISTRY_IMPL = "com.adaptris.adapter.registry.impl";
  
  /**
   * Bootstrap property that lists the configuration pre-processors separated by colons.
   */
  String CONFIGURATION_PRE_PROCESSORS = "preProcessors";
  
  String getDefaultAdapterConfig();

  void configure(BootstrapProperties properties) throws Exception;

  AdapterManagerMBean createAdapter() throws Exception;

  AdapterManagerMBean createAdapter(String adapterConfigUrl) throws Exception;

  void syncAdapterConfiguration(AdapterManagerMBean adapter) throws Exception;

  AdapterRegistryMBean getAdapterRegistry() throws MalformedObjectNameException, CoreException;
}
