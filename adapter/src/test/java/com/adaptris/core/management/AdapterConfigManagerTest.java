package com.adaptris.core.management;

import java.util.Properties;

import com.adaptris.core.BaseCase;
import com.adaptris.core.CoreException;
import com.adaptris.core.management.config.XStreamConfigManager;
import com.adaptris.core.runtime.AdapterRegistry;
import com.adaptris.core.stubs.JunitBootstrapProperties;

public class AdapterConfigManagerTest extends BaseCase {

  private BootstrapProperties bootstrap;

  public AdapterConfigManagerTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    super.setUp();
    bootstrap = new JunitBootstrapProperties(new Properties());
  }

  public void testBootstrap_getConfigManager() throws Exception {
    assertNotNull(bootstrap.getConfigManager());
    assertEquals(XStreamConfigManager.class, bootstrap.getConfigManager().getClass());
  }

  public void testGetAdapterRegistryMBean() throws Exception {
    AdapterConfigManager configManager = bootstrap.getConfigManager();
    assertNotNull(configManager.getAdapterRegistry());
    assertEquals(AdapterRegistry.class, configManager.getAdapterRegistry().getClass());
  }

  public void testGetAdapterRegistryMBean_CustomImpl() throws Exception {
    AdapterConfigManager configManager = bootstrap.getConfigManager();
    System.setProperty(AdapterConfigManager.ADAPTER_REGISTRY_IMPL, AdapterRegistry.class.getCanonicalName());
    assertNotNull(configManager.getAdapterRegistry());
    assertEquals(AdapterRegistry.class, configManager.getAdapterRegistry().getClass());
    System.setProperty(AdapterConfigManager.ADAPTER_REGISTRY_IMPL, "");
  }

  public void testGetAdapterRegistryMBean_MissingImpl() throws Exception {
    AdapterConfigManager configManager = bootstrap.getConfigManager();
    System.setProperty(AdapterConfigManager.ADAPTER_REGISTRY_IMPL, "com.xyz.abcde");
    try {
      configManager.getAdapterRegistry();
      fail();
    }
    catch (CoreException expected) {

    }
    System.setProperty(AdapterConfigManager.ADAPTER_REGISTRY_IMPL, "");
  }

}
