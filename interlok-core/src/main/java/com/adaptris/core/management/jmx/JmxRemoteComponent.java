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

package com.adaptris.core.management.jmx;

import static com.adaptris.core.management.Constants.CFG_KEY_JMX_SERVICE_URL_KEY;
import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;
import static com.adaptris.core.util.PropertyHelper.getPropertySubset;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.adaptris.core.management.ManagementComponent;
import com.adaptris.core.management.properties.PropertyResolver;
import com.adaptris.core.runtime.AdapterComponentMBean;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.ManagedThreadFactory;

/**
 * Implementation of the {@link ManagementComponent} interface for JSR160.
 *
 */
public class JmxRemoteComponent extends JmxComponentImpl {


  /**
   * Bootstrap property key that defines the optional object name
   * <p>
   * If {@value} is not defined then the default object name will be used, otherwise the value of the property key will be used as
   * the objectname, so make it conforms to the standard {@link ObjectName} specification.
   * </p>
   *
   * @see #DEFAULT_JMX_OBJECT_NAME
   *
   */
  public static final String CFG_KEY_JMX_SERVICE_URL_OBJECT_NAME = "jmxserviceurl.objectname";
  /**
   * Bootstrap property key prefix that controls what environment is passed through to the JMXConnectorServer.
   * <p>
   * Each property that matches the prefix of {@value} is passed through to the JMXConnectorServer as part of its environment (minus
   * the prefix); if you need specific configuration for the JMXConnectorServer then this is how you would do it. e.g. The presence
   * of the property <code>jmxserviceurl.env.myEnvironment=ABCDE</code> would cause an environment containing
   * <code>myEnvironment=ABCDE</code> to be passed through to
   * {@link JMXConnectorServerFactory#newJMXConnectorServer(JMXServiceURL, Map, MBeanServer)}. Obviously due to the inherent
   * restrictions on properties, the environment will be the equivalent to a <code>Map&lt;String, String&gt;</code>
   * </p>
   *
   * @see JMXConnectorServerFactory#newJMXConnectorServer(JMXServiceURL, Map, MBeanServer)
   */
  public static final String JMX_SERVICE_URL_ENV_PREFIX = "jmxserviceurl.env.";

  /**
   * Bootstrap property that controls the username required for connecting to this {@link JMXConnectorServer} if the {@link
   * JMXServiceURL} indicates the jmxmp protocol.
   * 
   * <p>If no {@code jmx.remote.profiles} setting is specified then {@code SASL/PLAIN} is used. Different profiles may be specified
   * according to <a href="http://docs.oracle.com/cd/E19698-01/816-7609/6mdjrf86r/index.html">the Oracle JDMK tutorial</a>.
   * Specifying a profile of TLS may require additional {@code javax.net.ssl.*} system properties.
   * </p>
   */
  public static final String JMX_JMXMP_SASL_USERNAME = "jmxserviceurl.jmxmp.username";

  /**
   * Bootstrap property that controls the password required for connecting to this {@link JMXConnectorServer} if the {@link
   * JMXServiceURL} indicates the jmxmp protocol.
   * 
   * <p>If no {@code jmx.remote.profiles} setting is specified then {@code SASL/PLAIN} is used. Different profiles may be specified
   * according to <a href="http://docs.oracle.com/cd/E19698-01/816-7609/6mdjrf86r/index.html">the Oracle JDMK tutorial</a>.
   * Specifying a profile of TLS may require additional {@code javax.net.ssl.*} system properties.
   * </p>
   */
  public static final String JMX_JMXMP_SASL_PASSWORD = "jmxserviceurl.jmxmp.password";

  private static final String PROTOCOL_JMXMP = "jmxmp";


  /**
   * The default object name {@value}
   *
   */
  public static final String DEFAULT_JMX_OBJECT_NAME = AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=JmxConnectorServer";

  private static final String PROVIDER_SASL = "JavaDMKSASL";
  private static final String SASL_PLAIN = "SASL/PLAIN";
  private static final String JMX_REMOTE_SASL_CALLBACK_HANDLER = "jmx.remote.sasl.callback.handler";
  private static final String JMX_REMOTE_PROFILES = "jmx.remote.profiles";


  public JmxRemoteComponent() {
  }


  @Override
  protected JmxComponent createJmxWrapper(Properties config) throws Exception {

    JmxConnectorServerWrapper wrapper = null;
    if (getPropertyIgnoringCase(config, CFG_KEY_JMX_SERVICE_URL_KEY) != null) {
      JMXServiceURL jmxServiceUrl = new JMXServiceURL(getPropertyIgnoringCase(config, CFG_KEY_JMX_SERVICE_URL_KEY));
      Map<String, Object> environment = createConnectorServerProperties(jmxServiceUrl, config);
      log.trace("JMX Environment : {}", environment);
      JMXConnectorServer jmx = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceUrl, environment, null);
      wrapper = new JmxConnectorServerWrapper(config, jmx, ObjectName.getInstance(getPropertyIgnoringCase(config,
          CFG_KEY_JMX_SERVICE_URL_OBJECT_NAME, DEFAULT_JMX_OBJECT_NAME)));
    }
    return wrapper;
  }


  private Map<String, Object> createConnectorServerProperties(JMXServiceURL serviceUrl, Properties config) throws Exception {
    return configureSecurity(serviceUrl, initialEnv(serviceUrl, config), config);
  }

  private Map<String, Object> configureSecurity(JMXServiceURL serviceUrl, Map<String, Object> env, Properties config) {
    log.trace("JMX Protocol=[{}]", serviceUrl.getProtocol());
    if (serviceUrl.getProtocol().equalsIgnoreCase(PROTOCOL_JMXMP) && config.containsKey(JMX_JMXMP_SASL_USERNAME)) {
      log.trace("JXMP Security specified via [{}={}]", JMX_JMXMP_SASL_USERNAME, config.getProperty(JMX_JMXMP_SASL_USERNAME));
      addSaslProvider();
      SimpleCallbackHandler handler =
          new SimpleCallbackHandler(config.getProperty(JMX_JMXMP_SASL_USERNAME), config.getProperty(JMX_JMXMP_SASL_PASSWORD));
      addIfMissing(env, JMX_REMOTE_PROFILES, SASL_PLAIN);
      addIfMissing(env, JMX_REMOTE_SASL_CALLBACK_HANDLER, handler);
    }
    return env;
  }

  private Map<String, Object> initialEnv(JMXServiceURL serviceUrl, Properties config) throws Exception {
    Map<String, Object> env = new HashMap<>();
    Properties bootstrapJmxEnv = getPropertySubset(config, JMX_SERVICE_URL_ENV_PREFIX, true);
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    for (String keyWithPrefix : bootstrapJmxEnv.stringPropertyNames()) {
      String realKey = keyWithPrefix.substring(JMX_SERVICE_URL_ENV_PREFIX.length());
      String toBeResolved = bootstrapJmxEnv.getProperty(keyWithPrefix);
      String realValue = resolver.resolve(toBeResolved);
      addIfMissing(env, realKey, realValue);
    }
    return env;
  }


  private void addIfMissing(Map<String, Object> map, String key, Object value) {
    if (!map.containsKey(key)) {
      map.put(key, value);
    }
  }

  private static void addSaslProvider() {
    if (Security.getProvider(PROVIDER_SASL) == null) {
      Security.addProvider(new com.sun.jdmk.security.sasl.Provider());
    }
  }

  private class JmxConnectorServerWrapper extends JmxComponent {
    ObjectName serverName;
    JMXConnectorServer jmxServer;
    Properties config;

    JmxConnectorServerWrapper(Properties p, JMXConnectorServer jmx, ObjectName name) {
      config = p;
      jmxServer = jmx;
      serverName = name;
    }

    @Override
    public void start() throws Exception {

      ManagedThreadFactory.createThread("JmxRemoteComponent", new Runnable() {
        @Override
        public void run() {
          try {
            log.debug("Starting JMXConnectorServer : {}", serverName);
            if (!jmxServer.isActive()) {
              jmxServer.start();
            }
            log.debug("Started JMXConnectorServer : {}", serverName);
          } catch (final Exception e) {
            log.error("Could not start JMXConnectorServer", e);
          }
        }
      }).start();

    }

    @Override
    public void stop() throws Exception {
      jmxServer.stop();
    }

    @Override
    public void unregister() throws Exception {
      MBeanServer mBeanServer = JmxHelper.findMBeanServer(config);
      mBeanServer.unregisterMBean(serverName);
      log.trace("MBean [" + serverName + "] unregistered");

    }

    @Override
    public void register() throws Exception {
      MBeanServer mBeanServer = JmxHelper.findMBeanServer(config);
      mBeanServer.registerMBean(jmxServer, serverName);
      log.trace("MBean [" + serverName + "] registered");
    }

    @Override
    public boolean isStarted() {
      return jmxServer.isActive();
    }

  }

}
