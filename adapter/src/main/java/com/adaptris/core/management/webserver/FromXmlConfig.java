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
package com.adaptris.core.management.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.util.PropertyHelper;
import com.adaptris.util.URLString;

/**
 * Build a jetty server from xml.
 * <p>
 * The {@link XmlConfiguration} will be configured with a property set that contains all the system properties and local properties
 * (from bootstrap.properties) that are prefixed {@code jetty.}. Properties defined in bootstrap.properties override system
 * properties.
 * </p>
 * 
 */
final class FromXmlConfig implements ServerBuilder {

  private static final String JETTY_PREFIX = "jetty.";

  private static Logger log = LoggerFactory.getLogger(ServerBuilder.class);

  private String jettyConfigUrl;
  private Properties initialProperties;

  FromXmlConfig(String configUrl, Properties initialConfig) {
    jettyConfigUrl = configUrl;
    initialProperties = initialConfig;
  }

  @Override
  public Server build() throws Exception {
    Server server = null;
    log.trace("Create Server from XML");
    try (InputStream in = connectToUrl(new URLString(jettyConfigUrl))) {
      final XmlConfiguration xmlConfiguration = new XmlConfiguration(in);
      xmlConfiguration.getProperties().putAll(mergeWithSystemProperties());
      server = (Server) xmlConfiguration.configure();
    }
    return server;
  }

  private Map<String, String> mergeWithSystemProperties() {
    Map<String, String> result = PropertyHelper.asMap(PropertyHelper.getPropertySubset(System.getProperties(), JETTY_PREFIX));
    result.putAll(PropertyHelper.asMap(PropertyHelper.getPropertySubset(initialProperties, JETTY_PREFIX)));
    return result;
  }

  private InputStream connectToUrl(final URLString loc) throws IOException {
    log.trace("Connecting to " + loc.toString());
    if (loc.getProtocol() == null || "file".equals(loc.getProtocol())) {
      return connectToFile(loc.getFile());
    }
    final URL url = new URL(loc.toString());
    final URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  private InputStream connectToFile(final String localFile) throws IOException {
    InputStream in = null;
    final File f = new File(localFile);
    if (f.exists()) {
      in = new FileInputStream(f);
    }
    else {
      final ClassLoader c = this.getClass().getClassLoader();
      final URL u = c.getResource(localFile);
      if (u != null) {
        in = u.openStream();
      }
    }
    return in;
  }
}
