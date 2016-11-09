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

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.JMX;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.vcs.RuntimeVersionControl;
import com.adaptris.core.management.vcs.RuntimeVersionControlLoader;
import com.adaptris.core.management.vcs.VcsException;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.AdapterRegistry;
import com.adaptris.core.runtime.AdapterRegistryMBean;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.TimeInterval;

/**
 * This is the class that handles almost everything required for startup.
 * <p>
 * Classloading should be done before using this class, and a BootstrapProperties instance must be passed to this class which
 * contains all the reqired information for starting an adapter.
 * </p>
 * 
 * @author gcsiki
 * 
 */
public class UnifiedBootstrap {

  private transient BootstrapProperties bootstrapProperties;
  private transient AdapterRegistryMBean adapterRegistry;
  private transient List<ManagementComponent> mgmtComponents;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private static final TimeInterval DEFAULT_OPERATION_TIMEOUT = new TimeInterval(2L, TimeUnit.MINUTES);

  private enum MgmtComponentTransition {
    START() {
      @Override
      void doAction(ManagementComponent c) throws Exception {
        c.start();
      }

    },
    STOP() {
      @Override
      void doAction(ManagementComponent c) throws Exception {
        c.stop();
      }
    },
    DESTROY() {
      @Override
      void doAction(ManagementComponent c) throws Exception {
        c.destroy();
      }
    };
    abstract void doAction(ManagementComponent c) throws Exception;

    public void transition(List<ManagementComponent> list) throws Exception {
      for (ManagementComponent c : list) {
        doAction(c);
      }
    }
  };

  public UnifiedBootstrap(BootstrapProperties bootstrapProperties) {
    this.bootstrapProperties = bootstrapProperties;
  }

  public AdapterManagerMBean createAdapter() throws Exception {
    configurationUpdate();
    bootstrapProperties.reconfigureLogging();
    AdapterManagerMBean adapter = bootstrapProperties.getConfigManager().createAdapter(bootstrapProperties.findAdapterResource());
    log.info("Adapter created");
    return adapter;
  }

  private void configurationUpdate() throws VcsException {
    RuntimeVersionControl versionControlSystem = RuntimeVersionControlLoader.getInstance().load();
    if (versionControlSystem != null) {
      versionControlSystem.setBootstrapProperties(bootstrapProperties);
      versionControlSystem.update();
    }
  }

  public void init(AdapterManagerMBean adapter) throws Exception {
    bootstrapProperties.getConfigManager().syncAdapterConfiguration(adapter);
    adapterRegistry = bootstrapProperties.getConfigManager().getAdapterRegistry();
    mgmtComponents = ManagementComponentFactory.create(bootstrapProperties);
    bootstrapProperties.setProperty(Constants.CFG_JMX_LOCAL_ADAPTER_UID, adapter.getUniqueId());
    for (ManagementComponent c : mgmtComponents) {
      c.init(bootstrapProperties);
    }
  }

  public void start() throws Exception {
    MgmtComponentTransition.START.transition(mgmtComponents);
    tryStart(adapterRegistry.getAdapters());
  }

  private void tryStart(Set<ObjectName> adapters) throws Exception {
    for (ObjectName obj : adapters) {
      AdapterManagerMBean mgr = JMX.newMBeanProxy(JmxHelper.findMBeanServer(bootstrapProperties), obj, AdapterManagerMBean.class);
      try {
        mgr.requestStart();
      }
      catch (CoreException e) {
        mgr.requestClose();
        log.error("Failed to fully start [{}]; adapter closed to avoid inconsistent state", obj);
        throw e;
      }
    }
  }

  public void stop() throws Exception {
    AdapterRegistry.stop(adapterRegistry.getAdapters());
    MgmtComponentTransition.STOP.transition(mgmtComponents);
  }

  public void close() throws Exception {
    AdapterRegistry.close(adapterRegistry.getAdapters());
    for (ObjectName objName : adapterRegistry.getAdapters()) {
      AdapterManagerMBean manager = JMX.newMBeanProxy(JmxHelper.findMBeanServer(bootstrapProperties), objName,
          AdapterManagerMBean.class);
      manager.unregisterMBean();
    }
    MgmtComponentTransition.DESTROY.transition(mgmtComponents);
  }

}
