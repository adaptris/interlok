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

import static com.adaptris.core.management.jetty.JettyServerComponent.ATTR_BOOTSTRAP_KEYS;
import static com.adaptris.core.management.jetty.JettyServerComponent.ATTR_JMX_ADAPTER_UID;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.Constants;
import com.adaptris.core.management.jetty.WebServerProperties.WebServerPropertiesEnum;

abstract class ServerBuilder {

  protected static Logger log = LoggerFactory.getLogger(JettyServerComponent.class);
  protected Properties initialProperties;

  public ServerBuilder(Properties p) {
    initialProperties = p;
  }

  abstract Server build() throws Exception;

  public static final Server build(final Properties config) throws Exception {
    final String jettyConfigUrl = WebServerPropertiesEnum.CONFIG_FILE.getValue(config, null);
    if (!isEmpty(jettyConfigUrl)) {
      return configure(new FromXmlConfig(config).build(), config);
    }
    else {
      log.warn("You are starting Jetty without a configuration file. This is NOT suggested for production environments.");
      return configure(new FromProperties(config).build(), config);
    }
  }

  private static Server configure(final Server server, final Properties config) throws Exception {
    // TODO This is all wrong. Can't get server attributes from the SErvletContext
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

}
