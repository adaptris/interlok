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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.core.PortManager;
import com.adaptris.core.management.Constants;
import com.adaptris.core.management.SystemPropertiesUtilTest;
import com.adaptris.core.management.jmx.provider.junit.ServerProvider;
import com.adaptris.core.management.properties.PropertyResolver;
import com.adaptris.core.runtime.AdapterComponentMBean;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.security.password.Password;

public class JmxRemoteComponentTest extends BaseCase {

  private static final String DEFAULT_USERNAME_PASSWORD = "admin";
  private static final String DEFAULT_VALUE = "Back At The Chicken Shack 1960";
  private static final String SASL_PLAIN = "SASL/PLAIN";
  private static final String JMX_REMOTE_PROFILES = "jmx.remote.profiles";
  private static final String JMXMP_PREFIX = "service:jmx:jmxmp://localhost:";

  private Integer unusedPort = -1;


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    unusedPort = PortManager.nextUnusedPort(5555);
    ObjectName jmxObjName = new ObjectName(JmxRemoteComponent.DEFAULT_JMX_OBJECT_NAME);
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    if (mBeanServer.isRegistered(jmxObjName)) {
      mBeanServer.unregisterMBean(jmxObjName);
    }
  }

  @After
  public void tearDown() throws Exception {
    PortManager.release(unusedPort);
    ObjectName jmxObjName = new ObjectName(JmxRemoteComponent.DEFAULT_JMX_OBJECT_NAME);
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    if (mBeanServer.isRegistered(jmxObjName)) {
      mBeanServer.unregisterMBean(jmxObjName);
    }
  }

  @Test
  public void testDefaultObjectName_Lifecycle() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    jmxr.init(createProperties());
    ObjectName jmxObjName = new ObjectName(JmxRemoteComponent.DEFAULT_JMX_OBJECT_NAME);
    try {
      assertTrue(mBeanServer.isRegistered(jmxObjName));
      jmxr.start();
      waitFor(jmxr);
      jmxr.stop();
    }
    finally {
      destroy(jmxr);
    }
    assertFalse(mBeanServer.isRegistered(jmxObjName));
  }

  @Test
  public void testDefaultObjectName_MultipleInstance() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    jmxr.init(createProperties());
    JmxRemoteComponent jmx2 = new JmxRemoteComponent();
    try {
      jmx2.init(createProperties());
      fail();
    }
    catch (InstanceAlreadyExistsException expected) {

    }
    finally {
      destroy(jmxr);
      destroy(jmx2);
    }
  }

  @Test
  public void testNoProperties() throws Exception {
    ObjectName jmxObjName = new ObjectName(JmxRemoteComponent.DEFAULT_JMX_OBJECT_NAME);
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    jmxr.init(new Properties());
    try {
      assertFalse(mBeanServer.isRegistered(jmxObjName));
      jmxr.start();
      waitFor(jmxr);
      jmxr.stop();
    }
    finally {
      destroy(jmxr);
    }
  }

  @Test
  public void testNonDefaultObjectName_Lifecycle() throws Exception {
    ObjectName jmxObjName = new ObjectName(AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=" + getName());
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    Properties p = createProperties();
    p.setProperty(JmxRemoteComponent.CFG_KEY_JMX_SERVICE_URL_OBJECT_NAME, jmxObjName.toString());
    try {
      jmxr.init(p);
      assertTrue(mBeanServer.isRegistered(jmxObjName));
      jmxr.start();
      waitFor(jmxr);
      jmxr.stop();
    }
    finally {
      destroy(jmxr);
    }
    assertFalse(mBeanServer.isRegistered(jmxObjName));
  }

  @Test
  public void testResolveEnvironment() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    ObjectName jmxObjName = new ObjectName(AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=" + getName());
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    Properties p = new Properties();
    p.put(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, "service:jmx:junit://localhost:9999");
    p.setProperty(JmxRemoteComponent.JMX_SERVICE_URL_ENV_PREFIX + "encodedProperty", SystemPropertiesUtilTest.encode("myPassword"));
    p.setProperty(JmxRemoteComponent.JMX_SERVICE_URL_ENV_PREFIX + "plainProperty", "Blah blah");
    p.setProperty(JmxRemoteComponent.CFG_KEY_JMX_SERVICE_URL_OBJECT_NAME, jmxObjName.toString());

    // Add our dummy provider to the list.
    p.setProperty(JmxRemoteComponent.JMX_SERVICE_URL_ENV_PREFIX + JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES,
        "com.adaptris.core.management.jmx.provider");
    System.out.append(p.toString());
    jmxr.init(p);
    // Now make sure the password has been decrypted.
    assertNotNull(ServerProvider.getConnectorServer());
    Map<String, ?> attr = ServerProvider.getConnectorServer().getAttributes();
    assertEquals("myPassword", attr.get("encodedProperty"));
    assertEquals("Blah blah", attr.get("plainProperty"));
  }

  @Test
  public void testNoDecoding() throws Exception {
    PropertyResolver resolver = PropertyResolver.getDefaultInstance();
    resolver.init();
    assertEquals(DEFAULT_VALUE, resolver.resolve(DEFAULT_VALUE));
  }

  @Test
  public void testSASL_Authentication() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    JMXConnector jmxc = null;
    Properties p = createProperties();
    p.setProperty(JmxRemoteComponent.JMX_JMXMP_SASL_USERNAME, DEFAULT_USERNAME_PASSWORD);
    p.setProperty(JmxRemoteComponent.JMX_JMXMP_SASL_PASSWORD, DEFAULT_USERNAME_PASSWORD);
    try {
      jmxr.init(p);
      jmxr.start();
      waitFor(jmxr);
      jmxc = createClientConnector(p.getProperty(Constants.CFG_KEY_JMX_SERVICE_URL_KEY), DEFAULT_USERNAME_PASSWORD, DEFAULT_USERNAME_PASSWORD);
      MBeanServerConnection myMbeanServer = jmxc.getMBeanServerConnection();
      assertTrue(Arrays.asList(myMbeanServer.getDomains()).contains("com.adaptris"));
    } finally {
      destroy(jmxr);
      closeQuietly(jmxc);
    }
  }

  @Test
  public void testSASL_Authentication_EncodedPassword() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    JMXConnector jmxc = null;
    Properties p = createProperties();
    p.setProperty(JmxRemoteComponent.JMX_SERVICE_URL_ENV_PREFIX + JMX_REMOTE_PROFILES, SASL_PLAIN);
    p.setProperty(JmxRemoteComponent.JMX_JMXMP_SASL_USERNAME, DEFAULT_USERNAME_PASSWORD);
    p.setProperty(JmxRemoteComponent.JMX_JMXMP_SASL_PASSWORD, Password.encode(DEFAULT_USERNAME_PASSWORD, Password.PORTABLE_PASSWORD));
    try {
      jmxr.init(p);
      jmxr.start();
      waitFor(jmxr);
      jmxc = createClientConnector(p.getProperty(Constants.CFG_KEY_JMX_SERVICE_URL_KEY), DEFAULT_USERNAME_PASSWORD, DEFAULT_USERNAME_PASSWORD);
      MBeanServerConnection myMbeanServer = jmxc.getMBeanServerConnection();
      assertTrue(Arrays.asList(myMbeanServer.getDomains()).contains("com.adaptris"));
    } finally {
      destroy(jmxr);
      closeQuietly(jmxc);
    }
  }

  @Test
  public void testSASL_Authentication_BadPassword() throws Exception {
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    JMXConnector jmxc = null;
    Properties p = createProperties();
    p.setProperty(JmxRemoteComponent.JMX_JMXMP_SASL_USERNAME, DEFAULT_USERNAME_PASSWORD);
    p.setProperty(JmxRemoteComponent.JMX_JMXMP_SASL_PASSWORD, DEFAULT_USERNAME_PASSWORD);
    try {
      jmxr.init(p);
      jmxr.start();
      waitFor(jmxr);
      jmxc = createClientConnector(p.getProperty(Constants.CFG_KEY_JMX_SERVICE_URL_KEY), DEFAULT_USERNAME_PASSWORD, "Hello World");
      fail();
    } catch (Exception expected) {
    } finally {
      destroy(jmxr);
      closeQuietly(jmxc);
    }
  }

  private JMXConnector createClientConnector(String urlString, String user, String password)
      throws MalformedURLException, IOException {
    HashMap env = new HashMap();
    env.put(JMX_REMOTE_PROFILES, SASL_PLAIN);
    env.put(JMXConnector.CREDENTIALS, new String[] {user, password});
    return JMXConnectorFactory.connect(new JMXServiceURL(urlString), env);
  }


  private Properties createProperties() {
    Properties result = new Properties();
    result.put(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, JMXMP_PREFIX + unusedPort);
    return result;
  }

  private void closeQuietly(JMXConnector c) {
    try {
      if (c != null) {
        c.close();
      }
    } catch (Exception e) {
    }
  }

  private void destroy(JmxRemoteComponent c) {
    if (c != null) {
      try {
        c.stop();
      } catch (Exception e) {
        
      }
      try {
        c.destroy();
      } catch (Exception e) {
        
      }
    }
  }

  private static void waitFor(JmxRemoteComponent component) throws Exception {
    waitFor(component, MAX_WAIT);
  }

  private static void waitFor(JmxRemoteComponent component, long maxWaitMs) throws Exception {
    long waitTime = 0;
    while (waitTime < maxWaitMs && !component.isStarted()) {
      waitTime += DEFAULT_WAIT_INTERVAL;
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
    }
    assertTrue(component.isStarted());
  }

}
