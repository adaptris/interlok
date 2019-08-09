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
package com.adaptris.core.jmx;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.PortManager;
import com.adaptris.core.management.Constants;
import com.adaptris.core.management.jmx.JmxRemoteComponent;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.TimeInterval;

public class JmxConnectionTest {
  private static final String DEFAULT_USERNAME_PASSWORD = "admin";
  private static final String SASL_PLAIN = "SASL/PLAIN";
  private static final String JMX_REMOTE_PROFILES = "jmx.remote.profiles";
  private static final String JMXMP_PREFIX = "service:jmx:jmxmp://localhost:";

  @Test
  public void testLocalJmx() throws Exception {
    JmxConnection conn = new JmxConnection().withAdditionalDebug(true);
    LifecycleHelper.prepare(conn);
    LifecycleHelper.init(conn);
    assertNotNull(conn.mbeanServerConnection());
  }

  @Test
  public void testRemoteJmx() throws Exception {
    Integer port = PortManager.nextUnusedPort(12345);
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    JmxConnection conn = new JmxConnection();
    try {
      jmxr.init(createProperties(port));
      jmxr.start();
      conn.withJmxServiceUrl(JMXMP_PREFIX + port);
      conn.setConnectionRetryInterval(new TimeInterval(2L, TimeUnit.SECONDS));
      conn.setAdditionalDebug(true);
      conn.setConnectionAttempts(5);
      LifecycleHelper.prepare(conn);
      LifecycleHelper.init(conn);
      assertNotNull(conn.mbeanServerConnection());
    } finally {
      LifecycleHelper.stopAndClose(conn);
      PortManager.release(port);
      destroy(jmxr);
    }
  }

  @Test
  public void testRemoteJmx_Retry() throws Exception {
    Integer port = PortManager.nextUnusedPort(23456);
    final JmxRemoteComponent jmxr = new JmxRemoteComponent();
    JmxConnection conn = new JmxConnection();
    try {
      jmxr.init(createProperties(port));
      conn.withJmxServiceUrl(JMXMP_PREFIX + port);
      conn.setConnectionRetryInterval(new TimeInterval(1L, TimeUnit.SECONDS));
      conn.setConnectionAttempts(10);
      Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
        @Override
        public void run() {
          try {
            jmxr.start();
          } catch (Exception e) {
            throw new RuntimeException();
          }
        }
      }, 2, TimeUnit.SECONDS);
      LifecycleHelper.prepare(conn);
      LifecycleHelper.init(conn);
      assertNotNull(conn.mbeanServerConnection());
    } finally {
      LifecycleHelper.stopAndClose(conn);
      PortManager.release(port);
      destroy(jmxr);
    }
  }

  @Test
  public void testRemoteJmx_RetryAndFail() throws Exception {
    Integer port = PortManager.nextUnusedPort(34567);
    JmxConnection conn = new JmxConnection();
    try {
      conn.setJmxServiceUrl(JMXMP_PREFIX + port);
      conn.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));
      conn.setAdditionalDebug(true);
      conn.setConnectionAttempts(2);
      LifecycleHelper.initAndStart(conn);
      fail();
    } catch (CoreException expected) {

    } finally {
      LifecycleHelper.stopAndClose(conn);
      PortManager.release(port);
    }
  }


  @Test
  public void testRemoteJMX_WithAuthentication() throws Exception {
    Integer port = PortManager.nextUnusedPort(45678);
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    JmxConnection conn = new JmxConnection();
    try {
      jmxr.init(createProperties(port, DEFAULT_USERNAME_PASSWORD, DEFAULT_USERNAME_PASSWORD));
      jmxr.start();
      conn.withJmxServiceUrl(JMXMP_PREFIX + port).withUsername(DEFAULT_USERNAME_PASSWORD).withPassword(DEFAULT_USERNAME_PASSWORD);
      conn.setConnectionRetryInterval(new TimeInterval(2L, TimeUnit.SECONDS));
      conn.setAdditionalDebug(true);
      conn.setConnectionAttempts(5);
      LifecycleHelper.prepare(conn);
      LifecycleHelper.init(conn);
      assertNotNull(conn.mbeanServerConnection());
    } finally {
      LifecycleHelper.stopAndClose(conn);
      PortManager.release(port);
      destroy(jmxr);
    }
  }

  @Test
  public void testRemoteJMX_WithAuthentication_AndEnv() throws Exception {
    Integer port = PortManager.nextUnusedPort(56789);
    JmxRemoteComponent jmxr = new JmxRemoteComponent();
    JmxConnection conn = new JmxConnection();
    conn.getJmxProperties().add(new KeyValuePair(JMX_REMOTE_PROFILES, SASL_PLAIN));
    try {
      jmxr.init(createProperties(port, DEFAULT_USERNAME_PASSWORD, DEFAULT_USERNAME_PASSWORD));
      jmxr.start();
      conn.setJmxServiceUrl(JMXMP_PREFIX + port);
      conn.setUsername(DEFAULT_USERNAME_PASSWORD);
      conn.setPassword(DEFAULT_USERNAME_PASSWORD);
      conn.setConnectionRetryInterval(new TimeInterval(2L, TimeUnit.SECONDS));
      conn.setAdditionalDebug(true);
      conn.setConnectionAttempts(5);
      LifecycleHelper.prepare(conn);
      LifecycleHelper.init(conn);
      assertNotNull(conn.mbeanServerConnection());
    } finally {
      LifecycleHelper.stopAndClose(conn);
      PortManager.release(port);
      destroy(jmxr);
    }
  }

  private Properties createProperties(int unusedPort) {
    Properties result = new Properties();
    result.put(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, JMXMP_PREFIX + unusedPort);
    return result;
  }

  private Properties createProperties(int unusedPort, String username, String password) {
    Properties p = createProperties(unusedPort);
    p.setProperty(JmxRemoteComponent.JMX_JMXMP_SASL_USERNAME, username);
    p.setProperty(JmxRemoteComponent.JMX_JMXMP_SASL_PASSWORD, password);
    return p;
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
}
