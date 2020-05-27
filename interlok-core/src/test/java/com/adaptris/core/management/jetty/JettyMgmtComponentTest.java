package com.adaptris.core.management.jetty;

import static org.junit.Assert.assertNotNull;
import java.time.Duration;
import java.util.Properties;
import org.awaitility.Awaitility;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.core.PortManager;
import com.adaptris.core.management.Constants;
import com.adaptris.core.management.webserver.JettyServerManager;
import com.adaptris.core.management.webserver.ServerManager;
import com.adaptris.core.management.webserver.WebServerManagementUtil;

public class JettyMgmtComponentTest {

  private static final String JETTY_MGMT_XML = "jetty.mgmt.config.absolute";
  private static final String JETTY_MGMT_XML_RELATIVE = "jetty.mgmt.config.relative";

  private static final Duration MAX_STARTUP_WAIT = Duration.ofSeconds(5);
  private static final Duration STARTUP_POLL = Duration.ofMillis(100);
  @Test
  public void testFromProperties() throws Exception {
    JettyServerComponent jetty = new JettyServerComponent();
    int portForServer = PortManager.nextUnusedPort(18080);
    try {
      Properties jettyConfig = new Properties();
      jettyConfig.setProperty(ServerBuilder.WEB_SERVER_PORT_CFG_KEY, String.valueOf(portForServer));
      jetty.init(jettyConfig);
      jetty.setClassLoader(Thread.currentThread().getContextClassLoader());
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      Awaitility.await().atMost(MAX_STARTUP_WAIT).with().pollInterval(STARTUP_POLL).until(() -> mgr.isStarted());
    } finally {
      stopAndDestroy(jetty);
      PortManager.release(portForServer);
    }
  }

  @Test
  public void testFromProperties_withWebappurl() throws Exception {
    JettyServerComponent jetty = new JettyServerComponent();
    int portForServer = PortManager.nextUnusedPort(18080);
    try {
      Properties jettyConfig = new Properties();
      jettyConfig.setProperty(ServerBuilder.WEB_SERVER_PORT_CFG_KEY, String.valueOf(portForServer));
      jettyConfig.setProperty(ServerBuilder.WEB_SERVER_WEBAPP_URL_CFG_KEY, "./webapps");
      jetty.init(jettyConfig);
      jetty.setClassLoader(Thread.currentThread().getContextClassLoader());
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      Awaitility.await().atMost(MAX_STARTUP_WAIT).with().pollInterval(STARTUP_POLL).until(() -> mgr.isStarted());
    } finally {
      stopAndDestroy(jetty);
      PortManager.release(portForServer);
    }
  }

  @Test
  public void testFromXml_Relative() throws Exception {
    JettyServerComponent jetty = new JettyServerComponent();
    String xmlFile = BaseCase.PROPERTIES.getProperty(JETTY_MGMT_XML_RELATIVE);
    int portForServer = PortManager.nextUnusedPort(18080);
    try {
      Properties jettyConfig = new Properties();
      jettyConfig.setProperty("jetty.http.port", String.valueOf(portForServer));
      jettyConfig.setProperty(ServerBuilder.WEB_SERVER_CONFIG_FILE_NAME_CGF_KEY, xmlFile);
      jetty.init(jettyConfig);
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      Awaitility.await().atMost(MAX_STARTUP_WAIT).with().pollInterval(STARTUP_POLL).until(() -> mgr.isStarted());
    } finally {
      stopAndDestroy(jetty);
      PortManager.release(portForServer);
    }
  }

  @Test
  public void testFromXml() throws Exception {
    JettyServerComponent jetty = new JettyServerComponent();
    String xmlFile = BaseCase.PROPERTIES.getProperty(JETTY_MGMT_XML);
    int portForServer = PortManager.nextUnusedPort(18080);
    try {
      Properties jettyConfig = new Properties();
      jettyConfig.setProperty("jetty.http.port", String.valueOf(portForServer));
      jettyConfig.setProperty(ServerBuilder.WEB_SERVER_CONFIG_FILE_NAME_CGF_KEY, xmlFile);
      jetty.init(jettyConfig);
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      Awaitility.await().atMost(MAX_STARTUP_WAIT).with().pollInterval(STARTUP_POLL).until(() -> mgr.isStarted());
    } finally {
      stopAndDestroy(jetty);
      PortManager.release(portForServer);
    }
  }

  @Test
  public void testFailsafe() throws Exception {
    JettyServerComponent jetty = new JettyServerComponent();
    String xmlFile = BaseCase.PROPERTIES.getProperty(JETTY_MGMT_XML);
    int portForServer = PortManager.nextUnusedPort(18080);
    try {
      Properties jettyConfig = new Properties();
      jettyConfig.setProperty(Constants.CFG_JMX_LOCAL_ADAPTER_UID, "service:jmx:jmxmp://localhost:5555");
      jettyConfig.setProperty(Constants.BOOTSTRAP_PROPERTIES_RESOURCE_KEY, "bootstrap.properties");
      jettyConfig.setProperty("jetty.http.port", String.valueOf(portForServer));
      jetty.init(jettyConfig);
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      Awaitility.await().atMost(MAX_STARTUP_WAIT).with().pollInterval(STARTUP_POLL).until(() -> mgr.isStarted());
    } finally {
      stopAndDestroy(jetty);
      PortManager.release(portForServer);
    }
  }

  @Test
  public void testFailsafe_WithOverrideDescriptor() throws Exception {
    JettyServerComponent jetty = new JettyServerComponent();
    String xmlFile = BaseCase.PROPERTIES.getProperty(JETTY_MGMT_XML);
    int portForServer = PortManager.nextUnusedPort(18080);
    try {
      Properties jettyConfig = new Properties();
      jettyConfig.setProperty("jetty.http.port", String.valueOf(portForServer));
      jettyConfig.put("jetty.deploy.defaultsDescriptorPath",
          getClass().getClassLoader().getResource(JettyServerManager.DEFAULT_DESCRIPTOR_XML).toString());
      jetty.init(jettyConfig);
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      Awaitility.await().atMost(MAX_STARTUP_WAIT).with().pollInterval(STARTUP_POLL).until(() -> mgr.isStarted());
    } finally {
      stopAndDestroy(jetty);
      PortManager.release(portForServer);
    }
  }

  private void stopAndDestroy(JettyServerComponent c) {
    try {
      if (c != null) {
        c.stop();
        c.destroy();
      }
    } catch (Exception e) {

    }
  }
}
