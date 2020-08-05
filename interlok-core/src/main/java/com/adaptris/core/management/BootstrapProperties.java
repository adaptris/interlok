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

package com.adaptris.core.management;

import static com.adaptris.core.management.Constants.BOOTSTRAP_PROPERTIES_RESOURCE_KEY;
import static com.adaptris.core.management.Constants.CFG_KEY_CONFIG_MANAGER;
import static com.adaptris.core.management.Constants.CFG_KEY_CONFIG_URL;
import static com.adaptris.core.management.Constants.CFG_KEY_JMX_SERVICE_URL_KEY;
import static com.adaptris.core.management.Constants.CFG_KEY_LOGGING_RECONFIGURE;
import static com.adaptris.core.management.Constants.CFG_KEY_LOGGING_URL;
import static com.adaptris.core.management.Constants.CFG_KEY_MANAGEMENT_COMPONENT;
import static com.adaptris.core.management.Constants.DBG;
import static com.adaptris.core.management.Constants.DEFAULT_CONFIG_MANAGER;
import static com.adaptris.core.management.Constants.DEFAULT_PROPS_RESOURCE;
import static com.adaptris.core.management.Constants.PROTOCOL_FILE;
import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;
import static com.adaptris.core.util.PropertyHelper.getPropertySubset;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.management.logging.LoggingConfigurator;
import com.adaptris.core.util.PropertyHelper;
import com.adaptris.util.URLHelper;
import com.adaptris.util.URLString;

/**
 * This class holds the necessary information for startup and provides a extra method for getting the available adapter
 * configuration.
 *
 */
public class BootstrapProperties extends Properties {

  private static final long serialVersionUID = 2010101401L;
  private static final String[] BOOTSTRAP_PROP_OVERRIDE =
  {
      CFG_KEY_CONFIG_URL, CFG_KEY_LOGGING_URL, CFG_KEY_JMX_SERVICE_URL_KEY, CFG_KEY_MANAGEMENT_COMPONENT
  };

  private static final String[] BOOTSTRAP_SYSPROP_OVERRIDE =
  {
      "interlok.config.url", "interlok.logging.url", "interlok.jmxserviceurl", "interlok.mgmt.components"
  };

  private static final Map<String, String> BOOTSTRAP_OVERRIDES;

  private transient URLString primaryUrl;
  private static transient Logger log = LoggerFactory.getLogger(BootstrapProperties.class);
  private transient AdapterConfigManager configManager = null;

  static {
    Map<String, String> overrides = new HashMap<>();
    for (int i = 0; i < BOOTSTRAP_SYSPROP_OVERRIDE.length; i++) {
      overrides.put(BOOTSTRAP_SYSPROP_OVERRIDE[i], BOOTSTRAP_PROP_OVERRIDE[i]);
    }
    BOOTSTRAP_OVERRIDES = Collections.unmodifiableMap(overrides);
  }

  private enum BootstrapFeature {
    // Matches the Constants#CFG_XSTREAM_BEAUTIFIED_OUTPUT
    BEAUTIFYXSTREAMOUTPUT(false),
    // Matches Constants#CFG_KEY_PROXY_AUTHENTICATOR
    HTTPENABLEPROXYAUTH(true),
    // Constants#CFG_KEY_USE_MANAGEMENT_FACTORY_FOR_JMX
    USEJAVALANGMANAGEMENTFACTORY(true),
    // CFG_KEY_LOGGING_RECONFIGURE
    LOGGINGRECONFIGURE(true),
    // Constants#CFG_KEY_START_QUIETLY
    STARTADAPTERQUIETLY(true),
    // Constants#CFG_KEY_JNDI_SERVER
    ENABLELOCALJNDISERVER(false);

    private boolean defaultState;

    private BootstrapFeature(boolean defaultState) {
      this.defaultState = defaultState;
    }

    public boolean enabledByDefault() {
      return defaultState;
    }

  }

  public BootstrapProperties() {
    super();
  }

  public BootstrapProperties(String resourceName) throws Exception {
    this();
    putAll(overrideWithSystemProperties(createProperties(resourceName)));
    setProperty(BOOTSTRAP_PROPERTIES_RESOURCE_KEY, resourceName);
  }

  public BootstrapProperties(Properties p) {
    this();
    putAll(overrideWithSystemProperties(p));
  }

  private static Properties createProperties(final String resourceName) throws Exception {
    String propertiesFile = StringUtils.defaultIfBlank(resourceName, DEFAULT_PROPS_RESOURCE);
    log.trace("Properties resource is [{}]", propertiesFile);
    Properties config = PropertyHelper.loadQuietly(() -> {
      return openResource(propertiesFile);
    });
    return config;
  }

  private static InputStream openResource(String r) throws IOException {
    if (new File(r).exists()) {
      return new FileInputStream(r);
    }
    return BootstrapProperties.class.getClassLoader().getResourceAsStream(r);
  }

  private static Properties overrideWithSystemProperties(Properties p) {
    for (Map.Entry<String, String> kv : BOOTSTRAP_OVERRIDES.entrySet()) {
      String override = System.getProperty(kv.getKey());
      if (!StringUtils.isBlank(override)) {
        log.trace("Overriding [{}] with [{}]", kv.getValue(), override);
        p.setProperty(kv.getValue(), override);
      }
    }
    return p;
  }

  /**
   * Add overloaded method to get numerical values from bootstrap.properties.
   *
   * @param key
   *      The property key to get.
   * @param defaultValue
   *      The default numerical value if the key isn't found (or cannot be parsed as numercial).
   *
   * @return The numerical value for the given property key, or default if necessary.
   */
  public Long getProperty(String key, Long defaultValue) {
    return NumberUtils.toLong( getProperty(key), defaultValue);
  }

  /**
   * Convenience method to create an adapter based on the existing bootstrap properties
   *
   * @return the adapter object.
   * @throws Exception if an exception occured.
   * @deprecated use {@link #getConfigManager()} to create an AdapterManagerMBean instead.
   */
  @Deprecated
  public synchronized Adapter createAdapter() throws Exception {
    // First of all make sure the the config manager has made the default marshaller correct.
    getConfigManager();
    Adapter result = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(getConfigurationStream());
    log.info("Adapter created");
    return result;
  }

  public InputStream getConfigurationStream() throws Exception {
    return URLHelper.connect(new URLString(findAdapterResource()));
  }

  public String[] getConfigurationUrls() {
    List<String> list = new ArrayList<String>();
    Properties p = getPropertySubset(this, CFG_KEY_CONFIG_URL, true);
    Object[] urlKeys = p.keySet().toArray();
    Arrays.sort(urlKeys);
    for (int i = 0; i < urlKeys.length; i++) {
      String url = p.getProperty(urlKeys[i].toString());
      list.add(url);
    }
    if (DBG) {
      log.trace("Configuration URLS [{}]", list);
    }

    return list.toArray(new String[0]);
  }

  @SuppressWarnings("deprecation")
  public String findAdapterResource() {
    String[] urls = getConfigurationUrls();
    String adapterXml = null;
    for (int i = 0; i < urls.length; i++) {

      if (DBG) {
        log.trace("trying [{}]", urls[i]);
      }

      if (i == 0) {
        primaryUrl = new URLString(urls[i]);
        if (checkExists(primaryUrl)) {
          adapterXml = urls[i];
          break;
        }
        else {
          primaryUrl = null;
        }
      }
      else {
        if (checkExists(new URLString(urls[i]))) {
          adapterXml = urls[i];
          break;
        }
      }
    }
    return adapterXml;
  }

  public boolean isPrimaryUrlAvailable() {
    return primaryUrl != null;
  }

  public URLString getPrimaryUrl() {
    return primaryUrl;
  }

  /**
   * Check that the specified location exists.
   *
   * @param u the URL representing the location
   * @return true if the location exists.
   */
  private boolean checkExists(URLString u) {
    if (DBG) {
      log.trace("Checking availability of [{}]", u.toString());
    }
    boolean rc = true;
    try {
      if (u.getProtocol() == null || PROTOCOL_FILE.equals(u.getProtocol())) {
        rc = new File(u.getFile()).exists();
      }
      else {
        // This is technically all catered for by connectToUrl(URLString)
        // However, we have some special handling for those
        // urls that are considered HTTP urls, as we could get 404's but still
        // return a valid InputStream.
        URL url = new URL(u.toString());
        URLConnection conn = url.openConnection();
        // Not needed because ProxyAuthenticator does it's thing now first redmineID #5765
        // com.adaptris.core.util.ProxyUtil.applyBasicProxyAuthorisation(conn);
        InputStream in = conn.getInputStream();
        if (conn instanceof HttpURLConnection) {
          HttpURLConnection http = (HttpURLConnection) conn;
          if (http.getResponseCode() < 200 || http.getResponseCode() > 299) {
            rc = false;
          }
        }
        in.close();
      }
    }
    catch (Exception e) {
      rc = false;
    }
    if (DBG) {
      log.trace("[{}] {}", u.toString(), rc ? "found" : "not found");
    }
    return rc;
  }

  public synchronized AdapterConfigManager getConfigManager() throws Exception {
    if (configManager == null) {
      configManager = (AdapterConfigManager) Class
          .forName(PropertyHelper.getPropertyIgnoringCase(this, CFG_KEY_CONFIG_MANAGER, DEFAULT_CONFIG_MANAGER)).newInstance();
      configManager.configure(this);
    }
    return configManager;
  }

  public void reconfigureLogging() {
    if (isEnabled(CFG_KEY_LOGGING_RECONFIGURE)) {
      String loggingUrl = getPropertyIgnoringCase(this, CFG_KEY_LOGGING_URL, "");
      if (!StringUtils.isEmpty(loggingUrl)) {
        log.trace("Attempting Logging reconfiguration using {}", loggingUrl);
        LoggingConfigurator.newConfigurator().initialiseFrom(loggingUrl);
      }
    }
  }

  public boolean isEnabled(String key) {
    return isEnabled(this, key);
  }

  private static boolean enabledByDefault(String key) {
    try {
      return BootstrapFeature.valueOf(key.toUpperCase()).enabledByDefault();
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isEnabled(Properties p, String key) {
    String val = PropertyHelper.getPropertyIgnoringCase(p, key);
    return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(val), enabledByDefault(key));
  }

}
