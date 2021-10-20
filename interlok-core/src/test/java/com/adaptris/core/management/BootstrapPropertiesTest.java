/*
 * Copyright 2017 Adaptris Ltd.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.stubs.TempFileUtils;

@SuppressWarnings("deprecation")
public class BootstrapPropertiesTest {
  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetOperationTimeout() {
    BootstrapProperties boot = new BootstrapProperties();
    assertEquals(Constants.DEFAULT_OPERATION_TIMEOUT.toMilliseconds(), boot.getOperationTimeout());
    boot.setProperty(Constants.OPERATION_TIMEOUT_PROPERTY, "0");
    assertEquals(Constants.DEFAULT_OPERATION_TIMEOUT.toMilliseconds(), boot.getOperationTimeout());
    boot.setProperty(Constants.OPERATION_TIMEOUT_PROPERTY, "5000");
    assertEquals(5000, boot.getOperationTimeout());
  }

  @Test
  public void testConstructor() throws Exception {
    new BootstrapProperties();
    Object marker = new Object();
    File filename = TempFileUtils.createTrackedFile(testName.getMethodName(), null, marker);
    try (OutputStream o = new FileOutputStream(filename)) {
      createTestSample().store(o, testName.getMethodName());
    }
    BootstrapProperties boot = new BootstrapProperties(filename.getCanonicalPath());
    new BootstrapProperties();
  }

  @Test
  public void testCreateAdapter() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + testName.getMethodName();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(testName.getMethodName());
    File filename = TempFileUtils.createTrackedFile(testName.getMethodName(), null, adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    BootstrapProperties boot = new BootstrapProperties(createTestSample());
    boot.put("adapterConfigUrl.1", filename.toURI().toURL().toString());
    assertNotNull(boot.createAdapter());
  }

  @Test
  public void testIsPrimaryUrlAvailable() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + testName.getMethodName();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(testName.getMethodName());
    File filename = TempFileUtils.createTrackedFile(testName.getMethodName(), null, adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    BootstrapProperties boot = new BootstrapProperties(createTestSample());
    boot.put("adapterConfigUrl.1", filename.toURI().toURL().toString());
    assertNotNull(boot.findAdapterResource());
    assertTrue(boot.isPrimaryUrlAvailable());
    assertNotNull(boot.getPrimaryUrl());

    boot = new BootstrapProperties(createTestSample());
    boot.put("adapterConfigUrl.1", "file://localhost/./blah/blah/blah");
    boot.put("adapterConfigUrl.2", filename.toURI().toURL().toString());
    assertNotNull(boot.findAdapterResource());
    assertFalse(boot.isPrimaryUrlAvailable());
    assertNull(boot.getPrimaryUrl());
  }

  @Test
  public void testGetConfigManager() throws Exception {
    BootstrapProperties boot = new BootstrapProperties(createTestSample());
    AdapterConfigManager mgr = boot.getConfigManager();
    assertEquals(mgr, boot.getConfigManager());
  }

  @Test
  public void testReconfigureLogging() throws Exception {
    BootstrapProperties boot = new BootstrapProperties(createTestSample());
    boot.put(Constants.CFG_KEY_LOGGING_RECONFIGURE, "false");
    boot.reconfigureLogging();
    boot.put(Constants.CFG_KEY_LOGGING_RECONFIGURE, "true");
    boot.put(Constants.CFG_KEY_LOGGING_URL, "file://localhost/path/that/does/not/exist.xml");
    boot.reconfigureLogging();
    boot.remove(Constants.CFG_KEY_LOGGING_URL);
    boot.reconfigureLogging();
  }

  @Test
  public void testIsEnabled() {
    BootstrapProperties boot = new BootstrapProperties(createTestSample());

    assertTrue(boot.isEnabled(Constants.CFG_KEY_PROXY_AUTHENTICATOR));
    assertTrue(boot.isEnabled(Constants.CFG_KEY_USE_MANAGEMENT_FACTORY_FOR_JMX));
    assertTrue(boot.isEnabled(Constants.CFG_KEY_LOGGING_RECONFIGURE));
    assertTrue(boot.isEnabled(Constants.CFG_KEY_START_QUIETLY));
    assertFalse(boot.isEnabled(Constants.CFG_KEY_JNDI_SERVER));
    boot.put(Constants.CFG_KEY_LOGGING_RECONFIGURE, "false");
    assertFalse(boot.isEnabled(Constants.CFG_KEY_LOGGING_RECONFIGURE));
    assertFalse(boot.isEnabled("blahblahblah"));
    assertTrue(BootstrapProperties.isEnabled(createTestSample(), Constants.CFG_KEY_USE_MANAGEMENT_FACTORY_FOR_JMX));
  }

  private Properties createTestSample() {
    Properties result = new Properties();
    result.setProperty("a.key", "a.value");
    result.setProperty("a.anotherKey", "a.anotherKey");
    result.setProperty("b.key", "b.value");
    result.setProperty("b.anotherKey", "b.anotherKey");
    return result;
  }

  protected File deleteLater(Object marker) throws IOException {
    return TempFileUtils.createTrackedFile(testName.getMethodName(), null, marker);
  }

}
