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

package com.adaptris.core.management.jetty;

import java.util.Properties;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.management.ManagementComponent;
import com.adaptris.core.management.webserver.JettyServerManager;
import com.adaptris.core.management.webserver.WebServerManagementUtil;
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
  private static final String ATTR_BOOTSTRAP_PROPERTIES = "com.adaptris.core.webapp.local.bootstrap";
  private static final String ATTR_BOOTSTRAP_PROPERTIES_ALT = "interlok.webapp.local.bootstrap";

  public static final String SERVER_ID = JettyServerComponent.class.getSimpleName();
  private static final String FRIENDLY_NAME = JettyServerComponent.class.getSimpleName();
  
  public static final String[] ATTR_BOOTSTRAP_KEYS = {
      ATTR_BOOTSTRAP_PROPERTIES, ATTR_BOOTSTRAP_PROPERTIES_ALT
  };

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
          wrapper = new JettyServerWrapperImpl(ServerBuilder.build(properties));
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
      jettyManager.addServer(SERVER_ID, server);
    }

    @Override
    void start() {
      try {
        if (server.isStopped()) {
          server.start();
          log.debug("{} Started", FRIENDLY_NAME);
        }
      } catch (final Exception ex) {
        log.error("Exception while starting Jetty", ex);
      }
    }

    @Override
    void stop() {
      try {
        server.stop();
        server.join();
        log.debug("{} Stopped", FRIENDLY_NAME);
      } catch (final Exception ex) {
        log.error("Exception while stopping Jetty", ex);
      }

    }

    @Override
    void destroy() {
      try {
        final JettyServerManager jettyManager = (JettyServerManager)WebServerManagementUtil.getServerManager();
        jettyManager.removeServer(SERVER_ID);
        server.destroy();
        log.debug("{} Destroyed", FRIENDLY_NAME);
      } catch (final Exception ex) {
        log.error("Exception while destroying Jetty", ex);
      }
    }

  }
}
