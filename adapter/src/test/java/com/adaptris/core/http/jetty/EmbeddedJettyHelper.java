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

package com.adaptris.core.http.jetty;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.PortManager;
import com.adaptris.core.management.webserver.JettyServerComponent;
import com.adaptris.core.management.webserver.ServerManager;
import com.adaptris.core.management.webserver.WebServerManagementUtil;
import com.adaptris.core.management.webserver.WebServerProperties.WebServerPropertiesEnum;

/**
 * @author gcsiki
 *
 */
public class EmbeddedJettyHelper {
  public static final String URL_TO_POST_TO = "/url/to/post/to";
  public static final String XML_PAYLOAD = "<root><document>value</document></root>";
  private Logger log = LoggerFactory.getLogger(this.getClass());
  private JettyServerComponent jetty;
  private Properties jettyConfig = new Properties();
  private int portForServer;

  public EmbeddedJettyHelper() {
    portForServer = PortManager.nextUnusedPort(18080);
    jettyConfig.setProperty(WebServerPropertiesEnum.PORT.getOverridingBootstrapPropertyKey(), "" + portForServer);
  }

  public void startServer() throws Exception {
    jetty = new JettyServerComponent();
    jetty.init(jettyConfig);
    jetty.start();
    ServerManager mgr = WebServerManagementUtil.getServerManager();
    while (!mgr.isStarted()) {
      Thread.sleep(250);
    }
  }

  public void stopServer() throws Exception {
    jetty.stop();
    jetty.destroy();
    PortManager.release(portForServer);
    jetty = null;
  }

  public ConfiguredProduceDestination createProduceDestination() {
    log.trace("Destination is " + "http://localhost:" + portForServer + URL_TO_POST_TO);
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("http://localhost:" + portForServer + URL_TO_POST_TO);
    return d;
  }

}
