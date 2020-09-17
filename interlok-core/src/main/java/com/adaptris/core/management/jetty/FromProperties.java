/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.management.jetty;

import static com.adaptris.core.management.jetty.JettyServerComponent.DEFAULT_JETTY_PORT;
import java.util.Properties;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * Build a jetty server from properties.
 */
final class FromProperties extends ServerBuilder {

  public FromProperties(Properties initialConfig) {
    super(initialConfig);
  }

  @Override
  protected Server build() throws Exception {
    log.trace("Create Server from Properties");
    log.warn("You are starting Jetty without a configuration file. This is NOT suggested for production environments.");
    final Server server = createSimpleServer();

    configureThreadPool(server.getThreadPool());
    server.addConnector(createConnector(server));

    // Setting up handler collection
    final HandlerCollection handlerCollection = new HandlerCollection();
    final ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
    handlerCollection.addHandler(contextHandlerCollection);
    handlerCollection.addHandler(new DefaultHandler());

    server.setHandler(handlerCollection);
    addDeploymentManager(server, contextHandlerCollection);
    return server;
  }

  // Adding monitored webapp directory if specified
  private void addDeploymentManager(final Server server, final ContextHandlerCollection ctx) {
    final String jettyWebappBase = getConfigItem(WEB_SERVER_WEBAPP_URL_CFG_KEY);
    if (jettyWebappBase != null) {
      final DeploymentManager deploymentManager = new DeploymentManager();
      deploymentManager.setContexts(ctx);
      final WebAppProvider webAppProvider = new WebAppProvider();
      webAppProvider.setMonitoredDirName(jettyWebappBase);
      webAppProvider.setExtractWars(true);
      webAppProvider.setScanInterval(60);
      deploymentManager.addAppProvider(webAppProvider);
      server.addBean(deploymentManager);
    }
  }

  private Server createSimpleServer() {
    final Server server = new Server();
    // Setting up extra options
    server.setStopAtShutdown(true);
    server.setStopTimeout(5000);
    server.setDumpAfterStart(false);
    server.setDumpBeforeStop(false);
    return server;
  }

  private void configureThreadPool(ThreadPool threadPool) {
    if (threadPool instanceof QueuedThreadPool) {
      QueuedThreadPool qp = (QueuedThreadPool) threadPool;
      qp.setMinThreads(10);
      qp.setMaxThreads(200);
      qp.setDetailedDump(false);
      qp.setIdleTimeout(60000);
    }
  }

  private ServerConnector createConnector(Server server) {
    ServerConnector connector = new ServerConnector(server, -1, -1,
        new HttpConnectionFactory(configure(new HttpConfiguration()), HttpCompliance.RFC2616));
    connector.setPort(Integer.parseInt(getConfigItem(WEB_SERVER_PORT_CFG_KEY, DEFAULT_JETTY_PORT)));
    return connector;
  }

  private HttpConfiguration configure(final HttpConfiguration httpConfig) {
    httpConfig.setSecureScheme("http");
    httpConfig.setSecurePort(8443);
    httpConfig.setOutputBufferSize(32768);
    httpConfig.setOutputAggregationSize(8192);
    httpConfig.setRequestHeaderSize(8192);
    httpConfig.setResponseHeaderSize(8192);
    httpConfig.setSendDateHeader(true);
    httpConfig.setSendServerVersion(true);
    httpConfig.setHeaderCacheSize(512);
    httpConfig.setDelayDispatchUntilContent(true);
    httpConfig.setMaxErrorDispatches(10);
    // httpConfig.setBlockingTimeout(-1);
    httpConfig.setMinRequestDataRate(-1);
    httpConfig.setMinResponseDataRate(-1);
    httpConfig.setPersistentConnectionsEnabled(true);
    return httpConfig;
  }

}
