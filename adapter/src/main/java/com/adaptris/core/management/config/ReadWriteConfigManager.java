package com.adaptris.core.management.config;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;
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
    return JMX.newMBeanProxy(JmxHelper.findMBeanServer(bootstrapProperties),
        getAdapterRegistry().createAdapter(new URLString(adapterConfigUrl).getURL()), AdapterManagerMBean.class);
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
    return;
  }

  private void syncAdapterConfiguration(AdapterManagerMBean adapter, URLString master, URLString slave)
      throws MalformedObjectNameException, MalformedURLException, IOException, CoreException {
    log.trace("persisting [" + master + "] to [" + slave + "]");
    getAdapterRegistry().persistAdapter(adapter, slave.getURL());
  }

}
