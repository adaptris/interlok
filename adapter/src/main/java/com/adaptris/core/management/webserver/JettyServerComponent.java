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

package com.adaptris.core.management.webserver;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.Constants;
import com.adaptris.core.management.ManagementComponent;
import com.adaptris.core.management.webserver.WebServerProperties.WebServerPropertiesEnum;
import com.adaptris.util.URLString;

/**
 * This class can be used for configuring and starting a Jetty webserver for the adapter.
 * <p>
 * You must provide the jetty config file in the bootstrap.properties file for a Jetty instance to startup.<br/>
 * See the Jetty Guide for more information on configuration options.
 * <p>
 * For adding webapps to the server the jettyWebappBase parameter can be used.
 *
 * @author gcsiki
 *
 */
public class JettyServerComponent implements ManagementComponent {

  public static final String DEFAULT_JETTY_PORT = "8080";

  public static final String DEFAULT_JETTY_HOST_IP = "127.0.0.1";
  public static final String DEFAULT_JETTY_HOST_NAME = "localhost";

  public static final String ATTR_JMX_SERVICE_URL = "com.adaptris.core.webapp.local.jmxserviceurl";
  public static final String ATTR_JMX_ADAPTER_UID = "com.adaptris.core.webapp.local.jmxuid";
  public static final String ATTR_BOOTSTRAP_PROPERTIES = "com.adaptris.core.webapp.local.bootstrap";

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private JettyServerWrapper wrapper = new JettyServerWrapper() {
    @Override
    void start() {
    }

    @Override
    void stop() {
    }

    @Override
    void destroy() {
    }

    @Override
    void register() {
    }
  };

  public JettyServerComponent() {
  }

  @Override
  public void init(Properties properties) throws Exception {
    wrapper = createWrapper(properties);
    wrapper.register();
  }

  /**
   * Had to do some tricks for proper classloading.
   */
  @Override
  public void start() throws Exception {
    wrapper.start();
  }

  @Override
  public void stop() {
    wrapper.stop();
  }

  @Override
  public void destroy() throws Exception {
    wrapper.destroy();
  }

  private JettyServerWrapper createWrapper(Properties config) throws Exception {
    String jettyConfigUrl = WebServerPropertiesEnum.CONFIG_FILE.getValue(config, null);
    
    JettyServerWrapperImpl wrapper = null;
    if (!isEmpty(jettyConfigUrl)) {
      wrapper = new JettyServerWrapperImpl(createServer(config));
    }
    else {
      wrapper = new JettyServerWrapperImpl(createSimpleServer(config));
      log.warn("You are starting Jetty without a configuration file. This is NOT suggested for production environments.");
    }
    return wrapper;
  }

  private Server configure(Server server, Properties config) throws Exception {
    // TODO This is all wrong. Can't get server attributes from the SErvletContext
    // OLD-SKOOL Do it via SystemProperties!!!!!
    // Add Null Prodction in to avoid System.setProperty issues during tests.
    // Or in fact if people decide to not enable JMXServiceUrl in bootstrap.properties
    if (config.containsKey(Constants.CFG_JMX_LOCAL_ADAPTER_UID)) {
      // server.setAttribute(ATTR_JMX_ADAPTER_UID, config.getProperty(Constants.CFG_JMX_LOCAL_ADAPTER_UID));
      System.setProperty(ATTR_JMX_ADAPTER_UID, config.getProperty(Constants.CFG_JMX_LOCAL_ADAPTER_UID));
    }
    if (config.containsKey(Constants.BOOTSTRAP_PROPERTIES_RESOURCE_KEY)) {
      System.setProperty(ATTR_BOOTSTRAP_PROPERTIES, config.getProperty(Constants.BOOTSTRAP_PROPERTIES_RESOURCE_KEY));
    }
    return server;
  }

  private Server createSimpleServer(Properties config) throws Exception {
    log.trace("Create Server from Properties");
    Server server = createSimpleServer();

    server.setThreadPool(createSimpleThreadPool(config));
    server.addConnector(createConnector(config));

    // Setting up handler collection
    HandlerCollection handlerCollection = new HandlerCollection();
    ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
    handlerCollection.addHandler(contextHandlerCollection);
    handlerCollection.addHandler(new DefaultHandler());

    server.setHandler(handlerCollection);
    addDeploymentManager(server, contextHandlerCollection, config);
    return configure(server, config);
  }

  // Adding monitored webapp directory if specified
  private void addDeploymentManager(Server server, ContextHandlerCollection ctx, Properties config) {
    String jettyWebappBase = WebServerPropertiesEnum.WEBAPP_URL.getValue(config, null);
    if (jettyWebappBase != null) {
      DeploymentManager deploymentManager = new DeploymentManager();
      deploymentManager.setContexts(ctx);
      WebAppProvider webAppProvider = new WebAppProvider();
      webAppProvider.setMonitoredDirName(jettyWebappBase);
      webAppProvider.setExtractWars(true);
      webAppProvider.setScanInterval(60);
      deploymentManager.addAppProvider(webAppProvider);
      server.addBean(deploymentManager);
    }
  }

  private Server createSimpleServer() {
    Server server = new Server();
    // Setting up extra options
    server.setStopAtShutdown(true);
    server.setSendServerVersion(true);
    server.setSendDateHeader(true);
    server.setGracefulShutdown(1000);
    server.setDumpAfterStart(false);
    server.setDumpBeforeStop(false);
    return server;
  }

  private QueuedThreadPool createSimpleThreadPool(Properties config) {
    QueuedThreadPool threadPool = new QueuedThreadPool();
    threadPool.setName("ThreadPool");
    threadPool.setMinThreads(10);
    threadPool.setMaxThreads(200);
    threadPool.setDetailedDump(false);
    return threadPool;
  }

  // Setting up web connector
  private SelectChannelConnector createConnector(Properties config) {
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setHost(WebServerPropertiesEnum.HOST_NAME.getValue(config, DEFAULT_JETTY_HOST_NAME));
    connector.setPort(Integer.parseInt(WebServerPropertiesEnum.PORT.getValue(config, DEFAULT_JETTY_PORT)));
    // connector.setName(getPropertyIgnoringCase(config, CFG_KEY_JETTY_HOST, DEFAULT_JETTY_HOST_NAME) + ":"
    // + getPropertyIgnoringCase(config, CFG_KEY_JETTY_PORT, DEFAULT_JETTY_PORT));
    connector.setMaxIdleTime(300000);
    connector.setAcceptors(10);
    connector.setStatsOn(false);
    // connector.setConfidentialPort(8443);
    connector.setLowResourcesConnections(20000);
    connector.setLowResourcesMaxIdleTime(5000);
    return connector;
  }
  
  private Server createServer(Properties config) throws Exception {
    Server server = null;
    InputStream in = connectToUrl(new URLString(WebServerPropertiesEnum.CONFIG_FILE.getValue(config)));
    try {
      log.trace("Create Server from XML");
      XmlConfiguration xmlConfiguration = new XmlConfiguration(in);
      server = (Server) xmlConfiguration.configure();
    }
    finally {
      closeQuietly(in);
    }
    return configure(server, config);
  }

  private InputStream connectToUrl(URLString loc) throws IOException {
    log.trace("Connecting to " + loc.toString());
    if (loc.getProtocol() == null || "file".equals(loc.getProtocol())) {
      return connectToFile(loc.getFile());
    }
    URL url = new URL(loc.toString());
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  private InputStream connectToFile(String localFile) throws IOException {
    InputStream in = null;
    File f = new File(localFile);
    if (f.exists()) {
      in = new FileInputStream(f);
    }
    else {
      ClassLoader c = this.getClass().getClassLoader();
      URL u = c.getResource(localFile);
      if (u != null) {
        in = u.openStream();
      }
    }
    return in;
  }

  abstract class JettyServerWrapper {
    abstract void start() throws Exception;

    abstract void stop();

    abstract void destroy();

    abstract void register();

  }

  private class JettyServerWrapperImpl extends JettyServerWrapper {
    Server server;

    JettyServerWrapperImpl(Server s) {
      server = s;
    }

    @Override
    void register() {
      JettyServerManager jettyManager = new JettyServerManager();
      jettyManager.addServer(server);
      WebServerManagementUtil.setServerManager(jettyManager);
    }

    @Override
    void start() throws Exception {
      ClassLoader actClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(WebAppClassLoader.class.getClassLoader());
      new Thread(new Runnable() {
        @Override
        public void run() {
          Thread.currentThread().setName(JettyServerComponent.class.getSimpleName());
          try {
            server.start();
          }
          catch (Exception e) {
            log.error(e.getMessage(), e);
          }
        }
      }).start();
      Thread.currentThread().setContextClassLoader(actClassLoader);
      log.trace(JettyServerComponent.class.getSimpleName() + " Started");
    }

    @Override
    void stop() {
      try {
        server.stop();
        server.join();
        log.trace(JettyServerComponent.class.getSimpleName() + " Stopped");
      }
      catch (Exception ex) {
        log.error("Exception while stopping Jetty", ex);
      }

    }

    @Override
    void destroy() {
      try {
        JettyServerManager jettyManager = (JettyServerManager) WebServerManagementUtil.getServerManager();
        jettyManager.removeServer(server);
        server.destroy();
        log.trace(JettyServerComponent.class.getSimpleName() + " Destroyed");
      }
      catch (Exception ex) {
        log.error("Exception while destroying Jetty", ex);
      }
    }

  }
}
