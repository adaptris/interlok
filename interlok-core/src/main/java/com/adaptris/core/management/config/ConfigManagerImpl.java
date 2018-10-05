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

package com.adaptris.core.management.config;

import javax.management.MalformedObjectNameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.AdapterConfigManager;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.runtime.AdapterRegistry;
import com.adaptris.core.runtime.AdapterRegistryMBean;

/**
 * Basic implemetation of {@link AdapterConfigManager}, which sets the BootstrapProperties.
 *
 * @author gcsiki
 *
 */
abstract class ConfigManagerImpl implements AdapterConfigManager {

  protected transient Logger log = LoggerFactory.getLogger(getClass());

  protected BootstrapProperties bootstrapProperties;
  private transient AdapterRegistryMBean adapterRegistry;

  @Override
  public void configure(BootstrapProperties bootstrapProperties) throws Exception {
    this.bootstrapProperties = bootstrapProperties;
  }

  public synchronized AdapterRegistryMBean getAdapterRegistry() throws MalformedObjectNameException, CoreException {
    if (adapterRegistry == null) {
      adapterRegistry = AdapterRegistry.findInstance(bootstrapProperties);
    }
    return adapterRegistry;
  }
}
