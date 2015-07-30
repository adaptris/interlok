package com.adaptris.core.management.config;

import com.adaptris.core.runtime.AdapterManagerMBean;

abstract class ReadonlyConfigManager extends ConfigManagerImpl {

  @Override
  public void syncAdapterConfiguration(AdapterManagerMBean adapter) throws Exception {
    log.warn("Method [syncAdapterConfiguration(Adapter adapter)] is not available for this implementation!");
  }
}
