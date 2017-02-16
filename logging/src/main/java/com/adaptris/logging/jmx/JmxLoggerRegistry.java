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

package com.adaptris.logging.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;


class JmxLoggerRegistry {

  private static final Map<ObjectName, List<JmxLogger>> jmxLoggerRegistry = new ConcurrentHashMap<ObjectName, List<JmxLogger>>();

  private JmxLoggerRegistry() {}

  static synchronized ObjectInstance registerJmxLogger(ObjectName objectName, JmxLogger jmxLogger, JmxLoggingNotification notifier)
      throws MBeanRegistrationException, InstanceNotFoundException, InstanceAlreadyExistsException, NotCompliantMBeanException {
    addJmxLogger(objectName, jmxLogger);
    // Unregister previous version mbean if we have one
    if (ManagementFactory.getPlatformMBeanServer().isRegistered(objectName)) {
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
    }
    try {
      return ManagementFactory.getPlatformMBeanServer().registerMBean(notifier, objectName);
    } catch (Exception expt) {
      // The mbean didn't register successfully so we remove it from the registry
      removeJmxLogger(objectName, jmxLogger);
      throw expt;
    }
  }

  static synchronized void unregisterJmxLogger(ObjectName objectName, JmxLogger jmxLogger) throws MBeanRegistrationException,
  InstanceNotFoundException {
    removeJmxLogger(objectName, jmxLogger);
    // We only unregister the mbean if no JmxLogger exists in the registry
    if (!JmxLoggerRegistry.hasJmxLoggerForObjectName(objectName)) {
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
    }
  }

  private static synchronized boolean addJmxLogger(ObjectName objectName, JmxLogger jmxLogger) {
    List<JmxLogger> jmxLoggers = jmxLoggerRegistry.get(objectName);
    if (jmxLoggers == null) {
      jmxLoggers = Collections.synchronizedList(new ArrayList<JmxLogger>());
      jmxLoggerRegistry.put(objectName, jmxLoggers);
    }
    return jmxLoggers.add(jmxLogger);
  }

  private static synchronized boolean removeJmxLogger(ObjectName objectName, JmxLogger jmxLogger) {
    List<JmxLogger> jmxLoggers = jmxLoggerRegistry.get(objectName);
    if (jmxLoggers != null) {
      return jmxLoggers.remove(jmxLogger);
    }
    return false;
  }

  private static synchronized boolean hasJmxLoggerForObjectName(ObjectName objectName) {
    List<JmxLogger> jmxLoggers = jmxLoggerRegistry.get(objectName);
    return jmxLoggers != null && jmxLoggers.size() > 0;
  }

}

