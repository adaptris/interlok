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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.management.config.XStreamConfigManager;
import com.adaptris.core.stubs.JunitBootstrapProperties;

public class AdapterConfigManagerTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  private BootstrapProperties bootstrap;

  public AdapterConfigManagerTest() {
  }

  @Before
  public void setUp() throws Exception {
    bootstrap = new JunitBootstrapProperties(new Properties());
  }

  @Test
  public void testBootstrap_getConfigManager() throws Exception {
    assertNotNull(bootstrap.getConfigManager());
    assertEquals(XStreamConfigManager.class, bootstrap.getConfigManager().getClass());
  }

  @Test
  public void testGetAdapterRegistryMBean() throws Exception {
    AdapterConfigManager configManager = bootstrap.getConfigManager();
    assertNotNull(configManager.getAdapterRegistry());
  }
}
