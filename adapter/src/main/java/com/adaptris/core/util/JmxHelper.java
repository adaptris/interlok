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

package com.adaptris.core.util;

import static com.adaptris.core.management.Constants.CFG_KEY_USE_MANAGEMENT_FACTORY_FOR_JMX;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.Constants;

/**
 * Helper for JMX
 *
 *
 * @author lchan
 *
 */
public class JmxHelper {

  private transient static MBeanServer mbeanServer = null;

  /**
   * Convenience method for directly passing in a {@link BootstrapProperties} object
   *
   * @param p property file that probably contains the key {@link Constants#CFG_KEY_USE_MANAGEMENT_FACTORY_FOR_JMX}
   * @return the MBeanServer against which to register JMX beans
   */
  public synchronized static MBeanServer findMBeanServer(Properties p) {
    if (mbeanServer == null) {
      mbeanServer = findMBeanServer(BootstrapProperties.isEnabled(p, CFG_KEY_USE_MANAGEMENT_FACTORY_FOR_JMX));
    }
    return mbeanServer;
  }

  /**
   * Find the {@link MBeanServer} to use.
   *
   * @return the MBeanServer against which to register JMX beans
   */
  public synchronized static MBeanServer findMBeanServer() {
    if (mbeanServer == null) {
      mbeanServer = findMBeanServer(true);
    }
    return mbeanServer;
  }

  /**
   * Register an object against the default {@link MBeanServer}.
   *
   * @param objName the ObjectName to register against.
   * @param obj the object.
   * @see #findMBeanServer()
   * @see #register(MBeanServer, ObjectName, Object)
   */
  public static void register(ObjectName objName, Object obj) throws MBeanRegistrationException, InstanceNotFoundException,
      InstanceAlreadyExistsException, NotCompliantMBeanException {
    register(null, objName, obj);
  }

  /**
   * Register an object against a {@link MBeanServer}.
   *
   * @param srv the {@link MBeanServer}
   * @param objName the ObjectName to register against.
   * @param obj the object.
   */
  public static void register(MBeanServer srv, ObjectName objName, Object obj) throws MBeanRegistrationException,
      InstanceNotFoundException,
      InstanceAlreadyExistsException, NotCompliantMBeanException {
    MBeanServer server = srv == null ? findMBeanServer() : srv;
    if (server.isRegistered(objName)) {
      server.unregisterMBean(objName);
    }
    server.registerMBean(obj, objName);
  }

  /**
   * Unregister an object from the default {@link MBeanServer}.
   *
   * @param objName the ObjectName to to unregister
   * @see #findMBeanServer()
   * @see #unregister(MBeanServer, ObjectName)
   */
  public static void unregister(ObjectName objName) throws MBeanRegistrationException,
      InstanceNotFoundException {
    unregister(null, objName);
  }

  /**
   * Unregister an object from a {@link MBeanServer}.
   *
   * @param objName the ObjectName to to unregister
   * @param srv the {@link MBeanServer}
   */
  public static void unregister(MBeanServer srv, ObjectName objName) throws MBeanRegistrationException,
      InstanceNotFoundException {
    MBeanServer server = srv == null ? findMBeanServer() : srv;
    if (server.isRegistered(objName)) {
      server.unregisterMBean(objName);
    }
  }

  /**
   * Find an appropriate MBeanServer to attach our JMX beans against.
   *
   * @param useJavaLangManagementFactory whether or not to use {@link ManagementFactory#getPlatformMBeanServer()}
   * @return an MBeanServer implementation either found using ManagementFactory or
   *         {@link MBeanServerFactory#findMBeanServer(String)} or just a new one.
   */
  private static MBeanServer findMBeanServer(boolean useJavaLangManagementFactory) {
    return useJavaLangManagementFactory ? ManagementFactory.getPlatformMBeanServer() : getMBeanServerRef();
  }

  private static MBeanServer getMBeanServerRef() {
    MBeanServer result = null;
    ArrayList<MBeanServer> serverList = MBeanServerFactory.findMBeanServer(null);
    if (serverList.isEmpty()) {
      result = MBeanServerFactory.newMBeanServer();
    }
    else {
      result = serverList.get(0);
    }
    return result;
  }

}
