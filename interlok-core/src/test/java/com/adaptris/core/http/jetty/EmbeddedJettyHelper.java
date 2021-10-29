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

package com.adaptris.core.http.jetty;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.management.jetty.JettyServerComponent;
import com.adaptris.core.management.jetty.ServerBuilder;
import com.adaptris.core.management.webserver.JettyServerManager;
import com.adaptris.interlok.junit.scaffolding.util.PortManager;

/**
 * @author gcsiki
 *
 */
public class EmbeddedJettyHelper {
  public static final String URL_TO_POST_TO = "/url/to/post/to";
  public static final String XML_PAYLOAD = "<root><document>value</document></root>";
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private JettyServerComponent jetty;
  private final Properties jettyConfig = new Properties();
  private final int portForServer;

  public EmbeddedJettyHelper() {
    portForServer = PortManager.nextUnusedPort(18080);
    jettyConfig.setProperty(ServerBuilder.WEB_SERVER_PORT_CFG_KEY, String.valueOf(portForServer));
  }

  public void startServer() throws Exception {
    jetty = new JettyServerComponent();
    jetty.init(jettyConfig);
    jetty.start();
    Thread.sleep(250);
    final JettyServerManager mgr = JettyServerManager.getInstance();
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

  public String buildUrl(String uri) {
    String actualUri = uri.startsWith("/") ? uri : "/" + uri;
    log.trace("Destination is {}{}{}", "http://localhost:", portForServer, actualUri);
    return "http://localhost:" + portForServer + actualUri;
  }

  public String createProduceDestination() {
    return buildUrl(URL_TO_POST_TO);
  }

}
