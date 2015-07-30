package com.adaptris.core.management;

import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.BaseCase;
import com.adaptris.core.PortManager;
import com.adaptris.core.management.jmx.JmxRemoteComponent;
import com.adaptris.core.management.webserver.JettyServerComponent;
import com.adaptris.core.management.webserver.WebServerProperties.WebServerPropertiesEnum;

public class ManagementComponentFactoryTest extends BaseCase {

  protected transient Log logR = LogFactory.getLog(this.getClass());

  public ManagementComponentFactoryTest(String name) {
    super(name);
  }

  public void testCreateJsr160Component() throws Exception {
    BootstrapProperties p = new BootstrapProperties();
    int port = PortManager.nextUnusedPort(5555);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, "jmx");
      p.setProperty(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, "service:jmx:jmxmp://localhost:" + port);
      List<ManagementComponent> list = ManagementComponentFactory.create(p);
      assertEquals(1, list.size());
      assertEquals(JmxRemoteComponent.class, list.get(0).getClass());
      testLifecycle(list, p, false);
    }
    finally {
      PortManager.release(port);
    }
  }

  public void testCreateJettyComponent() throws Exception {
    BootstrapProperties p = new BootstrapProperties();
    int port = PortManager.nextUnusedPort(5555);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, "jetty");
      p.setProperty(WebServerPropertiesEnum.PORT.getOverridingBootstrapPropertyKey(), String.valueOf(port));
      List<ManagementComponent> list = ManagementComponentFactory.create(p);
      assertEquals(1, list.size());
      assertEquals(JettyServerComponent.class, list.get(0).getClass());
      testLifecycle(list, p, true);
    }
    finally {
      PortManager.release(port);
    }
  }

  public void testCreateMultipleComponents() throws Exception {
    BootstrapProperties p = new BootstrapProperties();
    int port = PortManager.nextUnusedPort(8080);
    try {
      p.setProperty(Constants.CFG_KEY_MANAGEMENT_COMPONENT, DummyManagementComponent.class.getCanonicalName() + ":" + "jmx");
      List<ManagementComponent> list = ManagementComponentFactory.create(p);
      assertEquals(2, list.size());
      assertEquals(DummyManagementComponent.class, list.get(0).getClass());
      assertEquals(JmxRemoteComponent.class, list.get(1).getClass());
      testLifecycle(list, p, false);
    }
    finally {
      PortManager.release(port);
    }
  }

  private void testLifecycle(List<ManagementComponent> list, Properties p, boolean sleepAWhile) throws Exception {
    long aWhile = 500;
    for (ManagementComponent m : list) {
      m.init(p);
      if (sleepAWhile) {
        Thread.sleep(aWhile);
      }
      m.start();
      if (sleepAWhile) {
        Thread.sleep(aWhile);
      }
      m.stop();
      if (sleepAWhile) {
        Thread.sleep(aWhile);
      }
      m.destroy();
    }
  }
}