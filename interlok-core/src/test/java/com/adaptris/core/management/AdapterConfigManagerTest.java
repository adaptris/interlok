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

import java.util.Properties;

import com.adaptris.core.BaseCase;
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

}
