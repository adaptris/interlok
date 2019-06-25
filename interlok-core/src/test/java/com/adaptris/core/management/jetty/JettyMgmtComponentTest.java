package com.adaptris.core.management.jetty;

import java.util.Properties;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.core.PortManager;
import com.adaptris.core.management.jetty.WebServerProperties.WebServerPropertiesEnum;
import com.adaptris.core.management.webserver.ServerManager;
import com.adaptris.core.management.webserver.WebServerManagementUtil;

public class JettyMgmtComponentTest {

  private static final String JETTY_MGMT_XML = "jetty.mgmt.config.absolute";
  private static final String JETTY_MGMT_XML_RELATIVE = "jetty.mgmt.config.relative";

  @Test
  public void testFromProperties() throws Exception {
    JettyServerComponent jetty = new JettyServerComponent();
    int portForServer = PortManager.nextUnusedPort(18080);
    try {
      Properties jettyConfig = new Properties();
      jettyConfig.setProperty(WebServerPropertiesEnum.PORT.getOverridingBootstrapPropertyKey(),
          "" + portForServer);
      jetty.init(jettyConfig);
      jetty.setClassLoader(Thread.currentThread().getContextClassLoader());
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      while (!mgr.isStarted()) {
        Thread.sleep(250);
      }
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
      jettyConfig.setProperty("jetty.http.port", "" + portForServer);
      jettyConfig.setProperty(
          WebServerPropertiesEnum.CONFIG_FILE.getOverridingBootstrapPropertyKey(),
          xmlFile);
      jetty.init(jettyConfig);
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      while (!mgr.isStarted()) {
        Thread.sleep(250);
      }
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
      jettyConfig.setProperty("jetty.http.port", "" + portForServer);
      jettyConfig.setProperty(
          WebServerPropertiesEnum.CONFIG_FILE.getOverridingBootstrapPropertyKey(), xmlFile);
      jetty.init(jettyConfig);
      jetty.start();
      Thread.sleep(250);
      final ServerManager mgr = WebServerManagementUtil.getServerManager();
      while (!mgr.isStarted()) {
        Thread.sleep(250);
      }
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
