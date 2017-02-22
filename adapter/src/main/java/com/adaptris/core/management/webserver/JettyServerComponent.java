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

package com.adaptris.core.management.webserver;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Properties;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.Constants;
import com.adaptris.core.management.ManagementComponent;
import com.adaptris.core.management.webserver.WebServerProperties.WebServerPropertiesEnum;
import com.adaptris.core.util.ManagedThreadFactory;

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

  public static final String ATTR_JMX_SERVICE_URL = "com.adaptris.core.webapp.local.jmxserviceurl";
  public static final String ATTR_JMX_ADAPTER_UID = "com.adaptris.core.webapp.local.jmxuid";
  public static final String ATTR_BOOTSTRAP_PROPERTIES = "com.adaptris.core.webapp.local.bootstrap";

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private static final long STARTUP_WAIT = TimeUnit.SECONDS.toMillis(60L);

  private ClassLoader classLoader;
  private Properties properties;

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
  public void setClassLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void init(final Properties properties) throws Exception {
    this.properties = properties;
  }

  /**
   * Had to do some tricks for proper classloading.
   */
  @Override
  public void start() throws Exception {
    if (classLoader == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }
    final CyclicBarrier barrier = new CyclicBarrier(2);
    // This is to make sure we don't break the barrier before the real delay is up.
    //
    final long barrierDelay = STARTUP_WAIT;
    ManagedThreadFactory.createThread("EmbeddedJettyStart", new Runnable() {
      @Override
      public void run() {
        try {
          log.debug("Creating Jetty wrapper");
          Thread.currentThread().setContextClassLoader(classLoader);
          wrapper = createWrapper(properties);
          wrapper.register();
          // Wait until at least the server is registered.
          barrier.await(barrierDelay, TimeUnit.MILLISECONDS);
          wrapper.start();
        } catch (final Exception e) {
          log.error("Could not create wrapper", e);
        }
      }
    }).start();
    barrier.await(barrierDelay, TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() {
    wrapper.stop();
  }

  @Override
  public void destroy() throws Exception {
    wrapper.destroy();
  }

  private JettyServerWrapper createWrapper(final Properties config) throws Exception {
    final String jettyConfigUrl = WebServerPropertiesEnum.CONFIG_FILE.getValue(config, null);

    JettyServerWrapperImpl wrapper = null;
    if (!isEmpty(jettyConfigUrl)) {
      wrapper = new JettyServerWrapperImpl(configure(new FromXmlConfig(jettyConfigUrl, config).build(), config));
    } else {
      wrapper = new JettyServerWrapperImpl(configure(new FromProperties(config).build(), config));
      log.warn("You are starting Jetty without a configuration file. This is NOT suggested for production environments.");
    }
    return wrapper;
  }

  private Server configure(final Server server, final Properties config) throws Exception {
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

  abstract class JettyServerWrapper {
    abstract void start() throws Exception;

    abstract void stop();

    abstract void destroy();

    abstract void register();

  }

  private class JettyServerWrapperImpl extends JettyServerWrapper {
    Server server;

    JettyServerWrapperImpl(final Server s) {
      server = s;
    }

    @Override
    void register() {
      final JettyServerManager jettyManager = (JettyServerManager)WebServerManagementUtil.getServerManager();
      jettyManager.addServer(server);
    }

    @Override
    void start() {
      try {
        server.start();
        log.trace(JettyServerComponent.class.getSimpleName() + " Started");
      } catch (final Exception ex) {
        log.error("Exception while starting Jetty", ex);
      }
    }

    @Override
    void stop() {
      try {
        server.stop();
        server.join();
        log.trace(JettyServerComponent.class.getSimpleName() + " Stopped");
      } catch (final Exception ex) {
        log.error("Exception while stopping Jetty", ex);
      }

    }

    @Override
    void destroy() {
      try {
        final JettyServerManager jettyManager = (JettyServerManager)WebServerManagementUtil.getServerManager();
        jettyManager.removeServer(server);
        server.destroy();
        log.trace(JettyServerComponent.class.getSimpleName() + " Destroyed");
      } catch (final Exception ex) {
        log.error("Exception while destroying Jetty", ex);
      }
    }

  }
}
