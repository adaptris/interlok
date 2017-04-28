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

import static com.adaptris.core.management.Constants.CFG_KEY_CONFIG_MANAGER;
import static com.adaptris.core.management.Constants.CFG_KEY_CONFIG_URL;
import static com.adaptris.core.management.Constants.DBG;
import static com.adaptris.core.management.Constants.DEFAULT_CONFIG_MANAGER;
import static com.adaptris.core.management.Constants.DEFAULT_PROPS_RESOURCE;
import static com.adaptris.core.management.Constants.PROTOCOL_FILE;

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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.management.logging.LoggingConfigurator;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.PropertyHelper;
import com.adaptris.util.URLString;

/**
 * This class holds the necessary information for startup and provides a extra method for getting the available adapter
 * configuration.
 *
 */
public class BootstrapProperties extends Properties {

  private static final long serialVersionUID = 2010101401L;
  private static final String[] BOOTSTRAP_PROP_OVERRIDE =
      {Constants.CFG_KEY_CONFIG_URL, Constants.CFG_KEY_LOGGING_URL, Constants.CFG_KEY_JMX_SERVICE_URL_KEY};

  private static final String[] BOOTSTRAP_SYSPROP_OVERRIDE =
      {"interlok.config.url", "interlok.logging.url", "interlok.jmxserviceurl"};

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


  public BootstrapProperties() {
    super();
  }

  public BootstrapProperties(String resourceName) throws Exception {
    this();
    putAll(overrideWithSystemProperties(createProperties(resourceName)));
    setProperty(Constants.BOOTSTRAP_PROPERTIES_RESOURCE_KEY, resourceName);
  }

  public BootstrapProperties(Properties p) {
    this();
    putAll(overrideWithSystemProperties(p));
  }

  private static Properties createProperties(final String resourceName) throws Exception {
    String propertiesFile = StringUtils.defaultIfBlank(resourceName, DEFAULT_PROPS_RESOURCE);
    Properties config = new Properties();
    log.trace("Properties resource is [{}]", propertiesFile);
    try (InputStream r = Args.notNull(openResource(propertiesFile), "resourceName")) {
      config.load(r);
    }
    return config;
  }

  private static InputStream openResource(String r) throws Exception {
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
   * Convenience method to create an adapter based on the existing bootstrap properties
   * @return the adapter object.
   * @throws Exception if an exception occured.
   * @deprecated use {@link #getConfigManager()} to create an AdapterManagerMBean instead.
   */
  @Deprecated
  public synchronized Adapter createAdapter() throws Exception {
    // First of all make sure the the config manager has made the default marshaller correct.
    getConfigManager();
    Adapter result = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(new URLString(findAdapterResource()));
    log.info("Adapter created");
    return result;

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
    if (adapterXml == null) {
      log.trace("Sourcing configuration from [{}] property", Constants.CFG_KEY_CONFIG_RESOURCE);
      adapterXml = getProperty(Constants.CFG_KEY_CONFIG_RESOURCE);
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
      log.trace("[" + u.toString() + (rc ? "] found" : "] not found"));
    }
    return rc;
  }

  public synchronized AdapterConfigManager getConfigManager() throws Exception {
    if (configManager == null) {
      configManager = (AdapterConfigManager) Class.forName(
          PropertyHelper.getPropertyIgnoringCase(this, CFG_KEY_CONFIG_MANAGER, DEFAULT_CONFIG_MANAGER))
          .newInstance();
      configManager.configure(this);
    }
    return configManager;
  }

  @SuppressWarnings("deprecation")
  public void reconfigureLogging() {
    try {
      // Default to log4j12Url for backwards compat.
      String loggingUrl = getProperty(Constants.CFG_KEY_LOGGING_URL, getProperty(Constants.CFG_KEY_LOG4J12_URL, ""));
      if (!StringUtils.isEmpty(loggingUrl)) {
        log.trace("Attempting Logging reconfiguration using {}", loggingUrl);
        if (LoggingConfigurator.newConfigurator().initialiseFrom(new URLString(loggingUrl).getURL())) {
          log.trace("Successfully reconfigured logging using {}", loggingUrl);
        }
      }
    }
    catch (IOException ignoredIntentionally) {
      ;
    }
  }

  /**
   * @deprecated since 3.1.1 use {@link PropertyHelper#getPropertySubset(Properties, String)} instead.
   */
  @Deprecated
  public static Properties getPropertySubset(Properties p, String prefix) {
    return PropertyHelper.getPropertySubset(p, prefix, false);
  }

  /**
   * @deprecated since 3.1.1 use {@link PropertyHelper#getPropertySubset(Properties, String, boolean)} instead.
   */
  @Deprecated
  public static Properties getPropertySubset(Properties p, String prefix, boolean ignoreCase) {
    return PropertyHelper.getPropertySubset(p, prefix, ignoreCase);
  }

  /**
   * @deprecated since 3.1.1 use {@link PropertyHelper#getPropertyIgnoringCase(Properties, String, String)} instead.
   */
  @Deprecated
  public static String getPropertyIgnoringCase(Properties p, String key, String defaultValue) {
    return PropertyHelper.getPropertyIgnoringCase(p, key, defaultValue);
  }

  /**
   * @deprecated since 3.1.1 use {@link PropertyHelper#getPropertyIgnoringCase(Properties, String)} instead.
   */
  @Deprecated
  public static String getPropertyIgnoringCase(Properties p, String key) {
    return PropertyHelper.getPropertyIgnoringCase(p, key, null);
  }
}
