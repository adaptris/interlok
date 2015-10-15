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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.lang.reflect.Constructor;

import javax.management.MalformedObjectNameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.AdapterConfigManager;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.runtime.AdapterRegistry;
import com.adaptris.core.runtime.AdapterRegistryMBean;
import com.adaptris.core.util.ExceptionHelper;

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
      adapterRegistry = createAdapterRegistry();
      adapterRegistry.registerMBean();
    }
    return adapterRegistry;
  }

  private AdapterRegistryMBean createAdapterRegistry() throws CoreException, MalformedObjectNameException {
    AdapterRegistryMBean result = null;
    if (!isEmpty(System.getProperty(ADAPTER_REGISTRY_IMPL))) {
      result = createCustomRegistry();
    }
    else {
      result = new AdapterRegistry(bootstrapProperties);
    }
    return result;
  }

  private AdapterRegistryMBean createCustomRegistry() throws CoreException {
    AdapterRegistryMBean result = null;
    String classname = System.getProperty(ADAPTER_REGISTRY_IMPL);
    Class[] paramTypes =
    {
      BootstrapProperties.class
    };
    Object[] args =
    {
      bootstrapProperties
    };

    Class clazz;
    try {
      clazz = Class.forName(classname);
      Constructor cnst = clazz.getDeclaredConstructor(paramTypes);
      result = (AdapterRegistryMBean) cnst.newInstance(args);
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

}
