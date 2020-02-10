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

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import com.adaptris.core.management.jetty.WebServerProperties.WebServerPropertiesEnum;
import com.adaptris.core.util.PropertyHelper;

/**
 * Build a jetty server from xml.
 * <p>
 * The {@link XmlConfiguration} will be configured with a property set that contains all the system properties and local properties
 * (from bootstrap.properties) that are prefixed {@code jetty.}. Properties defined in bootstrap.properties override system
 * properties.
 * </p>
 * 
 */
final class FromXmlConfig extends ServerBuilder {

  private static final String JETTY_PREFIX = "jetty.";
  private String jettyConfigUrl;

  FromXmlConfig(Properties initialConfig) {
    super(initialConfig);
    jettyConfigUrl = WebServerPropertiesEnum.CONFIG_FILE.getValue(initialConfig);
  }

  @Override
  Server build() throws Exception {
    log.trace("Create Server from XML");
    final XmlConfiguration xmlConfiguration = new XmlConfiguration(toResource(jettyConfigUrl));
    xmlConfiguration.getProperties().putAll(mergeWithSystemProperties());
    return (Server) xmlConfiguration.configure();
  }

  private Map<String, String> mergeWithSystemProperties() {
    Map<String, String> result = PropertyHelper.asMap(PropertyHelper.getPropertySubset(System.getProperties(), JETTY_PREFIX));
    result.putAll(PropertyHelper.asMap(PropertyHelper.getPropertySubset(initialProperties, JETTY_PREFIX)));
    log.trace("Additional properties for XML Config {}", result);
    return result;
  }

  private Resource toResource(String urlString) throws Exception {
    final URL url = createUrlFromString(urlString);
    log.trace("Connecting to configured URL {}", url.toString());
    return Resource.newResource(url);
  }

  private static URL createUrlFromString(String s) throws Exception {
    String destToConvert = backslashToSlash(s);
    URI configuredUri = null;
    try {
      configuredUri = new URI(destToConvert);
    }
    catch (URISyntaxException e) {
      // Specifically here to cope with file:///c:/ (which is
      // technically illegal according to RFC2396 but we need
      // to support it
      if (destToConvert.split(":").length >= 3) {
        configuredUri = new URI(URLEncoder.encode(destToConvert, "UTF-8"));
      }
      else {
        throw e;
      }
    }
    return configuredUri.getScheme() == null ? relativeConfig(configuredUri) : new URL(configuredUri.toString());
  }

  private static URL relativeConfig(URI uri) throws Exception {
    String pwd = backslashToSlash(System.getProperty("user.dir"));
    String path = pwd + "/" + uri;
    URL result = new URL("file:///" + path);
    return result;
  }

  private static String backslashToSlash(String url) {
    if (!isEmpty(url)) {
      return url.replaceAll("\\\\", "/");
    }
    return url;
  }
}
