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

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.AdapterBuilderMBean;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.URLString;

abstract class ReadWriteConfigManager extends ConfigManagerImpl {

  protected AdaptrisMarshaller marshaller;

  protected ReadWriteConfigManager() throws Exception {

  }

  @Override
  public AdapterManagerMBean createAdapter() throws Exception {
//    Adapter result = null;
    String adapterConfigUrl = bootstrapProperties.findAdapterResource();
    if (adapterConfigUrl == null) {
      adapterConfigUrl = getDefaultAdapterResourceName();
    }
    return createAdapter(adapterConfigUrl);
  }

  protected abstract String getDefaultAdapterResourceName();

  @Override
  public AdapterManagerMBean createAdapter(String adapterConfigUrl) throws Exception {

    if (adapterConfigUrl == null) {
      return createAdapter();
    }
    AdapterBuilderMBean builder = getAdapterRegistry().getBuilder(bootstrapProperties);
    return JMX.newMBeanProxy(JmxHelper.findMBeanServer(bootstrapProperties),
        builder.createAdapter(new URLString(adapterConfigUrl)), AdapterManagerMBean.class);
  }


  @Override
  public void syncAdapterConfiguration(AdapterManagerMBean adapter) throws Exception {
    if (!bootstrapProperties.isPrimaryUrlAvailable()) {
      log.trace("primary URL configuration not available, no synchronisation");
      return;
    }
    String[] urls = bootstrapProperties.getConfigurationUrls();
    for (int i = 0; i < urls.length; i++) {
      URLString slave = new URLString(urls[i]);
      if (!slave.equals(bootstrapProperties.getPrimaryUrl())) {
        syncAdapterConfiguration(adapter, bootstrapProperties.getPrimaryUrl(), slave);
      }
    }
  }

  private void syncAdapterConfiguration(AdapterManagerMBean adapter, URLString master, URLString slave)
      throws MalformedObjectNameException, MalformedURLException, IOException, CoreException {
    log.trace("persisting [" + master + "] to [" + slave + "]");
    getAdapterRegistry().persistAdapter(adapter, slave);
  }

}
