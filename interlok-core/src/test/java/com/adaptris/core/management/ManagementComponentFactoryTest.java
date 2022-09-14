/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.management;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.adaptris.core.ClosedState;
import com.adaptris.core.management.jetty.ServerBuilder;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import com.adaptris.interlok.junit.scaffolding.util.PortManager;

public class ManagementComponentFactoryTest extends BaseCase {

  @Test
  public void testCreateJsr160Component() throws Exception {
    final BootstrapProperties p = new BootstrapProperties();
    final int port = PortManager.nextUnusedPort(5555);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, "jmx");
      p.setProperty(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, "service:jmx:jmxmp://localhost:" + port);
      final List<ManagementComponentInfo> list = ManagementComponentFactory.create(p);
      assertEquals(1, list.size());
      // assertEquals(JmxRemoteComponent.class, list.get(0).getClass());
      testLifecycle(list, p, false);
    } finally {
      PortManager.release(port);
    }
  }

  @Test
  public void testCreateJettyComponent() throws Exception {
    final BootstrapProperties p = new BootstrapProperties();
    final int port = PortManager.nextUnusedPort(5555);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, "jetty");
      p.setProperty(ServerBuilder.WEB_SERVER_PORT_CFG_KEY, String.valueOf(port));
      final List<ManagementComponentInfo> list = ManagementComponentFactory.create(p);
      assertEquals(1, list.size());
      // assertEquals(JettyServerComponent.class, list.get(0).getClass());
      testLifecycle(list, p, true);
    } finally {
      PortManager.release(port);
    }
  }

  @Test
  public void testCreateMultipleComponents() throws Exception {
    final BootstrapProperties p = new BootstrapProperties();
    final int port = PortManager.nextUnusedPort(8080);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, DummyManagementComponent.class.getCanonicalName() + ":" + "jmx");

      final List<ManagementComponentInfo> list = ManagementComponentFactory.create(p);
      assertEquals(2, list.size());
      assertEquals(DummyManagementComponent.class.getName(), list.get(0).getClassName());
      assertEquals(DummyManagementComponent.class.getCanonicalName(), list.get(0).getName());
      assertEquals(ClosedState.getInstance(), list.get(0).getState());
      // assertEquals(JmxRemoteComponent.class, list.get(1).getClass());
      testLifecycle(list, p, false);
    } finally {
      PortManager.release(port);
    }
  }

  @Test
  public void testCreateComponentDoNotErrorOnLifecycleFail() throws Exception {
    final BootstrapProperties p = new BootstrapProperties();
    final int port = PortManager.nextUnusedPort(8080);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, AlwaysFailDummyManagementComponent.class.getCanonicalName());

      final List<ManagementComponentInfo> list = ManagementComponentFactory.create(p);
      testLifecycle(list, p, false);
    } finally {
      PortManager.release(port);
    }
  }

  private void testLifecycle(final List<ManagementComponentInfo> list, final BootstrapProperties p, final boolean sleepAWhile) throws Exception {
    final long aWhile = 500;
    ManagementComponentFactory.initCreated(p);
    if (sleepAWhile) {
      Thread.sleep(aWhile);
    }
    ManagementComponentFactory.startCreated(p);
    if (sleepAWhile) {
      Thread.sleep(aWhile);
    }
    ManagementComponentFactory.stopCreated(p, true);
    if (sleepAWhile) {
      Thread.sleep(aWhile);
    }
    ManagementComponentFactory.closeCreated(p, false);
  }
}
