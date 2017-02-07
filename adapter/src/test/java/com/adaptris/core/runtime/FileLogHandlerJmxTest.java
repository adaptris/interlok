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

package com.adaptris.core.runtime;

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_LOG_HANDLER_TYPE;

import java.io.File;
import java.io.IOException;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.Adapter;
import com.adaptris.core.FileLogHandler;
import com.adaptris.core.FileLogHandlerJmxMBean;
import com.adaptris.core.LogHandlerTest;

@SuppressWarnings("deprecation")
public class FileLogHandlerJmxTest extends ComponentManagerCase {

  private static final String LOG_STATS = "stats.log";
  private static final String LOG_GRAPH = "graphs.log";
  private static final String LOG_FILE = "adapter.log";
  protected transient Log logR = LogFactory.getLog(this.getClass());
  private static final File LOG_DIRECTORY;

  public FileLogHandlerJmxTest(String name) {
    super(name);
  }

  static {
    try {
      LOG_DIRECTORY = File.createTempFile(FileLogHandlerJmxTest.class.getSimpleName(), null);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    LogHandlerTest.ensureDirectory(LOG_DIRECTORY);
  }


  @Override
  protected void tearDown() throws Exception {
    FileUtils.deleteQuietly(LOG_DIRECTORY);
    FileUtils.deleteDirectory(LOG_DIRECTORY);
    super.tearDown();
  }


  public void testExistsInAdapterManager() throws Exception {
    Adapter adapter = createAdapter(getName());
    FileLogHandler handler = createHandler();
    adapter.setLogHandler(handler);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName flhObjName = createFileHandlerObjectName(adapterManager);
    assertTrue(adapterManager.getChildRuntimeInfoComponents().contains(flhObjName));

  }

  public void testMBean_GetParentId() throws Exception {
    Adapter adapter = createAdapter(getName());
    FileLogHandler handler = createHandler();
    adapter.setLogHandler(handler);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName flhObjName = createFileHandlerObjectName(adapterManager);
    try {
      adapterManager.registerMBean();
      FileLogHandlerJmxMBean fileLogHandlerProxy = JMX.newMBeanProxy(mBeanServer, flhObjName, FileLogHandlerJmxMBean.class);
      assertNotNull(fileLogHandlerProxy);
      assertEquals(getName(), fileLogHandlerProxy.getParentId());
    }
    finally {
      adapterManager.unregisterMBean();
    }

  }

  public void testMBean_GetParentObjectName() throws Exception {
    Adapter adapter = createAdapter(getName());
    FileLogHandler handler = createHandler();
    adapter.setLogHandler(handler);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName flhObjName = createFileHandlerObjectName(adapterManager);
    try {
      adapterManager.registerMBean();
      FileLogHandlerJmxMBean fileLogHandlerProxy = JMX.newMBeanProxy(mBeanServer, flhObjName, FileLogHandlerJmxMBean.class);
      assertNotNull(fileLogHandlerProxy);
      assertEquals(adapterManager.createObjectName(), fileLogHandlerProxy.getParentObjectName());
    }
    finally {
      adapterManager.unregisterMBean();
    }
  }

  public void testMBean_CleanupLogfiles() throws Exception {
    Adapter adapter = createAdapter(getName());
    FileLogHandler handler = createHandler();
    adapter.setLogHandler(handler);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName flhObjName = createFileHandlerObjectName(adapterManager);
    LogHandlerTest.createLogFiles(LOG_DIRECTORY, LOG_FILE, 10);
    assertEquals(10, LOG_DIRECTORY.listFiles().length);
    try {
      adapterManager.registerMBean();
      FileLogHandlerJmxMBean fileLogHandlerProxy = JMX.newMBeanProxy(mBeanServer, flhObjName, FileLogHandlerJmxMBean.class);
      assertNotNull(fileLogHandlerProxy);
      fileLogHandlerProxy.cleanupLogfiles();
      assertEquals(1, LOG_DIRECTORY.listFiles().length);
    }
    finally {
      adapterManager.unregisterMBean();
    }
  }

  private FileLogHandler createHandler() throws IOException {
    FileLogHandler handler = new FileLogHandler();
    handler.setLogDirectory(LOG_DIRECTORY.getCanonicalPath());
    handler.setPeriod(1);
    handler.setLogFile(LOG_FILE);
    return handler;
  }

  private ObjectName createFileHandlerObjectName(AdapterManager parent) throws MalformedObjectNameException {
    return ObjectName.getInstance(JMX_LOG_HANDLER_TYPE + parent.createObjectHierarchyString() + ID_PREFIX
        + FileLogHandler.class.getSimpleName());
  }
}
