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
import static com.adaptris.core.management.Constants.CFG_KEY_LICENSE_URL;
import static com.adaptris.core.management.Constants.DBG;
import static com.adaptris.core.management.Constants.DEFAULT_CONFIG_MANAGER;
import static com.adaptris.core.management.Constants.DEFAULT_LICENSE_URL;
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
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.util.URLString;
import com.adaptris.util.license.License;
import com.adaptris.util.license.LicenseException;
import com.adaptris.util.license.LicenseFactory;

/**
 * This class holds the necessary information for startup and provides a extra method for getting the available adapter
 * configuration.
 *
 */
public class BootstrapProperties extends Properties {

  private static final long serialVersionUID = 2010101401L;

  private transient URLString primaryUrl;
  private static transient Logger log = LoggerFactory.getLogger(BootstrapProperties.class);
  private transient AdapterConfigManager configManager = null;


  public BootstrapProperties() {
    super();
  }

  public BootstrapProperties(String resourceName) throws Exception {
    this();
    putAll(createProperties(resourceName));
    setProperty(Constants.BOOTSTRAP_PROPERTIES_RESOURCE_KEY, resourceName);
  }

  public BootstrapProperties(Properties p) {
    this();
    putAll(p);
  }

  private static Properties createProperties(final String resourceName) throws Exception {
    String propertiesFile = resourceName;
    Properties config = new Properties();
    InputStream in = null;
    if (propertiesFile == null) {
      propertiesFile = DEFAULT_PROPS_RESOURCE;
    }

    log.trace("Properties resource is [" + propertiesFile + "]");

    File f = new File(propertiesFile);
    if (f.exists()) {
      in = new FileInputStream(f);
    }
    else {
      in = BootstrapProperties.class.getClassLoader().getResourceAsStream(propertiesFile);
    }
    if (in == null) {
      throw new IOException("cannot locate resource [" + propertiesFile + "]");
    }
    try {
      config.load(in);
    } finally {
      in.close();
    }
    return config;
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
    result.registerLicense(getLicense());
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
      log.trace("Configuration URLS [" + list + "]");
    }

    return list.toArray(new String[0]);
  }

  @SuppressWarnings("deprecation")
  public String findAdapterResource() {
    String[] urls = getConfigurationUrls();
    String adapterXml = null;
    for (int i = 0; i < urls.length; i++) {

      if (DBG) {
        log.trace("trying [" + urls[i] + "]");
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
      log.trace("Sourcing configuration from [" + Constants.CFG_KEY_CONFIG_RESOURCE + "] property");
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
   * Get a subset of the configuration properties.
   *
   * @param prefix the key that must exist in the property key for it to belong.
   * @param p the properties
   * @return a Properties object containing matching keys and values.
   */
  public static Properties getPropertySubset(Properties p, String prefix) {
    return getPropertySubset(p, prefix, false);
  }

  /**
   * Get a subset of the configuration properties.
   *
   * @param prefix the key that must exist in the property key for it to belong.
   * @param p the properties
   * @param ignoreCase whether or not to ignore the case of the prefix and property key
   * @return a Properties object containing matching keys and values.
   */
  public static Properties getPropertySubset(Properties p, String prefix, boolean ignoreCase) {
    Properties tmp = new Properties();
    String propPrefix = ignoreCase ? prefix.toUpperCase() : prefix;
    for (Object keyObj : p.keySet()) {
      String key = keyObj.toString();
      String propKey = ignoreCase ? key.toUpperCase() : key;
      if (propKey.startsWith(propPrefix)) {
        tmp.setProperty(key, p.getProperty(key));
      }
    }
    return tmp;

  }

  /**
   * Convenience method to get a property value ignoring the case.
   *
   * <p>
   * While we could legitimately override the get/set/getProperty/setProperty methods and convert all the keys to a single case;
   * this would not be appropriate for the properties that we will convert into system properties. So we have a static method to
   * iterate through all the keys of the property doing a case insensitive match. It's not terribly performant, but performance
   * isn't a consideration at this point.
   * </p>
   *
   * @param p the properties you want to query.
   * @param key the key
   * @param defaultValue the default value if key is not matched.
   * @return the property value.
   */
  public static String getPropertyIgnoringCase(Properties p, String key, String defaultValue) {
    String value = defaultValue;
    for (Object keyObj : p.keySet()) {
      String propKey = keyObj.toString();
      if (propKey.equalsIgnoreCase(key)) {
        value = p.getProperty(propKey);
      }
    }
    return value;
  }

  /**
   * @see #getPropertyIgnoringCase(Properties, String, String)
   */
  public static String getPropertyIgnoringCase(Properties p, String key) {
    return getPropertyIgnoringCase(p, key, null);
  }

  /**
   * Check that the specified location exists.
   *
   * @param u the URL representing the location
   * @return true if the location exists.
   */
  private boolean checkExists(URLString u) {
    if (DBG) {
      log.trace("Checking availability of [" + u.toString() + "]");
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
          getPropertyIgnoringCase(this, CFG_KEY_CONFIG_MANAGER, DEFAULT_CONFIG_MANAGER))
          .newInstance();
      configManager.configure(this);
    }
    return configManager;
  }

  /**
   * Get the license URL.
   * 
   * @return the license url.
   */
  public String getLicenseUrl() {
    return getPropertyIgnoringCase(this, CFG_KEY_LICENSE_URL, DEFAULT_LICENSE_URL);
  }

  public License getLicense() throws LicenseException {
    return LicenseFactory.getLicense(getLicenseUrl());
  }

  public void reconfigureLogging() {
    try {
      String log4jUrl = getProperty(Constants.CFG_KEY_LOG4J12_URL, "");
      if (!StringUtils.isEmpty(log4jUrl)) {
        log.debug("Attempting Logging reconfiguration using {}", log4jUrl);
        if (Log4jInit.configure(new URLString(log4jUrl).getURL())) {
          log.debug("Sucessfully reconfigured logging using {}", log4jUrl);
        }
      }
    }
    catch (IOException ignoredIntentionally) {
      ;
    }
  }

}
