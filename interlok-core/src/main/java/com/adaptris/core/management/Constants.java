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

/**
 * Constants and lots of them.
 *
 * @author lchan
 *
 */
public final class Constants {
  public static final boolean DBG = Boolean.getBoolean("adp.bootstrap.debug") || Boolean.getBoolean("interlok.bootstrap.debug");

  /**
   * Key representing the name of the file that was used to bootstrap the adapter.
   *
   */
  public static final String BOOTSTRAP_PROPERTIES_RESOURCE_KEY = BootstrapProperties.class.getCanonicalName();

  /**
   * Bootstrap Property containing the the URL of the adapter config.
   */
  public static final String CFG_KEY_CONFIG_URL = "adapterConfigUrl";

  /**
   * Bootstrap Property containing the URL of the license file.
   */
  public static final String CFG_KEY_LICENSE_URL = "licenseUrl";
  /**
   * Bootstrap Property containing the classname that will manage configuration style.
   *
   * @see AdapterConfigManager
   */
  public static final String CFG_KEY_CONFIG_MANAGER = "configManager";
  /**
   * Bootstrap Property containing an enum value that defines the Data Binder output format to generate ie XML/JSON.
   *
   * @see com.adaptris.core.AdapterMarshallerFactory.MarshallingOutput for a list of valid values
   */
  public static final String CFG_KEY_MARSHALLER_OUTPUT_TYPE = "marshallerOutputType";

  /**
   * Bootstrap property that enables default HTTP Proxy authentication via the user of standard java.net system properties.
   *
   */
  public static final String CFG_KEY_PROXY_AUTHENTICATOR = "httpEnableProxyAuth";

  /**
   * Bootstrap property containing a colon separated list of items that implement {@link ManagementComponent}
   *
   */
  public static final String CFG_KEY_MANAGEMENT_COMPONENT = "managementComponents";

  /**
   * The default license file.
   *
   */
  public static final String DEFAULT_LICENSE_URL = "license.properties";

  /**
   * The default bootstrap resource file.
   *
   */
  public static final String DEFAULT_PROPS_RESOURCE = "bootstrap.properties";

  /**
   * The default configuration manager.
   *
   * @see com.adaptris.core.management.config.XStreamConfigManager
   */
  public static final String DEFAULT_CONFIG_MANAGER = com.adaptris.core.management.config.XStreamConfigManager.class.getCanonicalName();

  /**
   * The default configuration resource for {@link com.adaptris.core.management.config.XStreamConfigManager}
   */
  public static final String DEFAULT_XSTREAM_RESOURCE_NAME = "adapter.xml";

  /**
   * Bootstrap Property telling us whether to use {@link java.lang.management.ManagementFactory} to find the platform MBeanServer.
   */
  public static final String CFG_KEY_USE_MANAGEMENT_FACTORY_FOR_JMX = "useJavaLangManagementFactory";

  /**
   * the file protocol.
   *
   */
  public static final String PROTOCOL_FILE = "file";

  /**
   * The prefix that will be used to define system properties within the standard bootstrap resource.
   *
   */
  public static final String SYSTEM_PROPERTY_PREFIX = "sysprop.";

  /**
   * The property key that will contain include the adapter's unique id for JMX purposes
   *
   */
  public static final String CFG_JMX_LOCAL_ADAPTER_UID = "jmxLocalAdapterUid";

  /**
   * Bootstrap property key that defines whether a java remote management {@link javax.management.remote.JMXConnectorServer} is
   * created or not.
   * <p>
   * If {@value} is specified in the properties file, then a {@link javax.management.remote.JMXServiceURL} is created from the
   * property key.
   * </p>
   */
  public static final String CFG_KEY_JMX_SERVICE_URL_KEY = "jmxserviceurl";

  /**
   * Bootstrap Property specifying whether or not to enable the localJndiServer.
   */
  public static final String CFG_KEY_JNDI_SERVER = "enableLocalJndiServer";

  /**
   * Bootstrap Property that specifies whether or not to start the adapter quietly
   *
   * <p>
   * The default value for this is true, if set to false, then any exceptions starting the adapter will be propagated back to the
   * caller (in most cases the <code>main</code> method); which may terminate the JVM depending on how the adapter has been started.
   * </p>
   */
  public static final String CFG_KEY_START_QUIETLY = "startAdapterQuietly";

  /**
   * Bootstrap Property specifying the logging configuration URL.
   *
   * @since 3.1.0
   */
  public static final String CFG_KEY_LOGGING_URL = "loggingConfigUrl";


  /**
   * Bootstrap Property specifying whether or not reconfigure logging
   *
   * @since 3.7.0 defaults to true.
   */
  public static final String CFG_KEY_LOGGING_RECONFIGURE = "loggingReconfigure";


}
