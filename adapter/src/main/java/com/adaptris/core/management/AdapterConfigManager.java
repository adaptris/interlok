/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
