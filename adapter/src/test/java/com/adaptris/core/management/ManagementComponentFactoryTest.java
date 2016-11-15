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

import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.BaseCase;
import com.adaptris.core.PortManager;
import com.adaptris.core.management.webserver.WebServerProperties.WebServerPropertiesEnum;

public class ManagementComponentFactoryTest extends BaseCase {

  protected transient Log logR = LogFactory.getLog(this.getClass());

  public ManagementComponentFactoryTest(final String name) {
    super(name);
  }

  public void testCreateJsr160Component() throws Exception {
    final BootstrapProperties p = new BootstrapProperties();
    final int port = PortManager.nextUnusedPort(5555);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, "jmx");
      p.setProperty(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, "service:jmx:jmxmp://localhost:" + port);
      ManagementComponentFactory.create(p);
      final List<Object> list = ManagementComponentFactory.getManagementComponents();
      assertEquals(4, list.size());
      // assertEquals(JmxRemoteComponent.class, list.get(0).getClass());
      testLifecycle(list, p, false);
    } finally {
      PortManager.release(port);
    }
  }

  public void testCreateJettyComponent() throws Exception {
    final BootstrapProperties p = new BootstrapProperties();
    final int port = PortManager.nextUnusedPort(5555);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, "jetty");
      p.setProperty(WebServerPropertiesEnum.PORT.getOverridingBootstrapPropertyKey(), String.valueOf(port));
      ManagementComponentFactory.create(p);
      final List<Object> list = ManagementComponentFactory.getManagementComponents();
      assertEquals(1, list.size());
      // assertEquals(JettyServerComponent.class, list.get(0).getClass());
      testLifecycle(list, p, true);
    } finally {
      PortManager.release(port);
    }
  }

  public void testCreateMultipleComponents() throws Exception {
    final BootstrapProperties p = new BootstrapProperties();
    final int port = PortManager.nextUnusedPort(8080);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, DummyManagementComponent.class.getCanonicalName() + ":" + "jmx");
      ManagementComponentFactory.create(p);
      final List<Object> list = ManagementComponentFactory.getManagementComponents();
      assertEquals(3, list.size());
      // assertEquals(DummyManagementComponent.class, list.get(0).getClass());
      // assertEquals(JmxRemoteComponent.class, list.get(1).getClass());
      testLifecycle(list, p, false);
    } finally {
      PortManager.release(port);
    }
  }

  private void testLifecycle(final List<Object> list, final Properties p, final boolean sleepAWhile) throws Exception {
    final long aWhile = 500;
    ManagementComponentFactory.initCreated();
    if (sleepAWhile) {
      Thread.sleep(aWhile);
    }
    ManagementComponentFactory.startCreated();
    if (sleepAWhile) {
      Thread.sleep(aWhile);
    }
    ManagementComponentFactory.stopCreated();
    if (sleepAWhile) {
      Thread.sleep(aWhile);
    }
    ManagementComponentFactory.closeCreated();
  }
}
