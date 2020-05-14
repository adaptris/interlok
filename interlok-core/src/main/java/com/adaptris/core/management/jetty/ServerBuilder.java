/*
 * Copyright 2017 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.adaptris.core.management.jetty;

import static com.adaptris.core.management.jetty.JettyServerComponent.ATTR_BOOTSTRAP_KEYS;
import static com.adaptris.core.management.jetty.JettyServerComponent.ATTR_JMX_ADAPTER_UID;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.management.Constants;

public abstract class ServerBuilder {
  public static final String WEB_SERVER_PORT_CFG_KEY = "webServerPort";
  public static final String WEB_SERVER_CONFIG_FILE_NAME_CGF_KEY = "webServerConfigUrl";
  public static final String WEB_SERVER_WEBAPP_URL_CFG_KEY = "webServerWebappUrl";

  private enum Builder {
    XML() {
      @Override
      public boolean canBuild(Properties p) {
        return p.containsKey(WEB_SERVER_CONFIG_FILE_NAME_CGF_KEY);
      }

      @Override
      public ServerBuilder builder(Properties cfg) {
        return new FromXmlConfig(cfg);
      }

    },
    PROPERTIES() {
      @Override
      public boolean canBuild(Properties p) {
        return p.containsKey(WEB_SERVER_PORT_CFG_KEY);
      }

      @Override
      public ServerBuilder builder(Properties cfg) {
        return new FromProperties(cfg);
      }
    },
    FAILSAFE() {
      @Override
      public boolean canBuild(Properties p) {
        return true;
      }
      @Override
      public ServerBuilder builder(Properties cfg) {
        return new FromClasspath(cfg);
      }
    };
    
    public abstract boolean canBuild(Properties p);

    public abstract ServerBuilder builder(Properties p);

  }

  private static List<Builder> FACTORIES = Collections
      .unmodifiableList(Arrays.asList(Builder.XML, Builder.PROPERTIES, Builder.FAILSAFE));

  protected static Logger log = LoggerFactory.getLogger(JettyServerComponent.class);
  private Properties initialProperties;

  protected ServerBuilder(Properties p) {
    initialProperties = p;
  }

  protected abstract Server build() throws Exception;

  protected static final Server build(final Properties config) throws Exception {
    return FACTORIES.stream().filter((b) -> b.canBuild(config)).findFirst().get().builder(config).build();
  }

  private static Server configure(final Server server, final Properties config) throws Exception {
    // TODO This is all wrong. Can't get server attributes from the ServletContext
    // OLD-SKOOL Do it via SystemProperties!!!!!
    // Add Null Prodction in to avoid System.setProperty issues during tests.
    // Or in fact if people decide to not enable JMXServiceUrl in bootstrap.properties
    if (config.containsKey(Constants.CFG_JMX_LOCAL_ADAPTER_UID)) {
      // server.setAttribute(ATTR_JMX_ADAPTER_UID, config.getProperty(Constants.CFG_JMX_LOCAL_ADAPTER_UID));
      System.setProperty(ATTR_JMX_ADAPTER_UID, config.getProperty(Constants.CFG_JMX_LOCAL_ADAPTER_UID));
    }
    if (config.containsKey(Constants.BOOTSTRAP_PROPERTIES_RESOURCE_KEY)) {
      for (String s : ATTR_BOOTSTRAP_KEYS) {
        System.setProperty(s, config.getProperty(Constants.BOOTSTRAP_PROPERTIES_RESOURCE_KEY));
      }
    }
    return server;
  }

  protected Properties getConfig() {
    return initialProperties;
  }

  protected String getConfigItem(String key) {
    return getConfigItem(key, null);
  }

  protected String getConfigItem(String key, String defaultValue) {
    return initialProperties.getProperty(key, defaultValue);
  }
}
