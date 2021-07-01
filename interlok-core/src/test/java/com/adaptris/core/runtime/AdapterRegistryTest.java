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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import com.adaptris.core.Adapter;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.JndiContextFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.XStreamJsonMarshaller;
import com.adaptris.core.config.ConfigPreProcessorLoader;
import com.adaptris.core.config.ConfigPreProcessors;
import com.adaptris.core.config.DefaultPreProcessorLoader;
import com.adaptris.core.config.DummyConfigurationPreProcessor;
import com.adaptris.core.event.AdapterShutdownEvent;
import com.adaptris.core.management.AdapterConfigManager;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.vcs.RuntimeVersionControl;
import com.adaptris.core.management.vcs.VcsException;
import com.adaptris.core.management.vcs.VersionControlSystem;
import com.adaptris.core.services.metadata.ReformatDateService;
import com.adaptris.core.services.metadata.ReformatMetadata;
import com.adaptris.core.stubs.JunitBootstrapProperties;
import com.adaptris.core.stubs.StaticMockEventProducer;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.URLString;

@SuppressWarnings("deprecation")
public class AdapterRegistryTest extends ComponentManagerCase {

  @Mock private DummyConfigurationPreProcessor mockPreProcessor;

  @Mock
  private DefaultPreProcessorLoader mockPreProcessorLoader;

  @Spy
  private ConfigPreProcessorLoader spyPreProcessorLoader;

  private transient Properties contextEnv = new Properties();

  public AdapterRegistryTest() {
  }


  @Before
  public void beforeMyTests() throws Exception {
    contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    spyPreProcessorLoader = new DefaultPreProcessorLoader();

    MockitoAnnotations.initMocks(this);
  }

  @Override
  public void tearDown() throws Exception {
    JmxHelper.findMBeanServer().unregisterMBean(ObjectName.getInstance(AdapterRegistry.STANDARD_REGISTRY_JMX_NAME));
    super.tearDown();
  }

  @Test
  public void testGetConfiguration() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    assertEquals(0, myAdapterRegistry.getConfiguration().size());
  }

  @Test
  public void testPutConfigurationUrl() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    URLString url = new URLString("http://localhost/1234");
    myAdapterRegistry.putConfigurationURL(myAdapterRegistry.createObjectName(), url);
    assertEquals(url, myAdapterRegistry.getConfigurationURL(myAdapterRegistry.createObjectName()));
  }

  @Test
  public void testPutConfigurationUrlString() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    myAdapterRegistry.putConfigurationURL(myAdapterRegistry.createObjectName(), "http://localhost/1234");
    assertEquals("http://localhost/1234", myAdapterRegistry.getConfigurationURLString(myAdapterRegistry.createObjectName()));
  }

  @Test
  public void testPreProcessorsLoaded() throws Exception {

    Properties bsProperties = new Properties();
    bsProperties.put(AdapterConfigManager.CONFIGURATION_PRE_PROCESSORS,
        DummyConfigurationPreProcessor.class.getName());
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry.findInstance(new JunitBootstrapProperties(bsProperties));
    AdapterBuilder builder = new ArrayList<AdapterBuilder>(myAdapterRegistry.builders()).get(0);
    builder.setConfigurationPreProcessorLoader(spyPreProcessorLoader);

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);

    myAdapterRegistry.createAdapter(new URLString(filename));

    verify(spyPreProcessorLoader, times(1)).load(any(BootstrapProperties.class));
  }

  @Test
  public void testPreProcessorCalled() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterBuilder builder = new ArrayList<AdapterBuilder>(myAdapterRegistry.builders()).get(0);
    builder.setConfigurationPreProcessorLoader(mockPreProcessorLoader);
    ConfigPreProcessors preProcessorsList = new ConfigPreProcessors();
    preProcessorsList.add(mockPreProcessor);

    when(mockPreProcessorLoader.load(any(BootstrapProperties.class))).thenReturn(preProcessorsList);
    when(mockPreProcessor.process(any(String.class))).thenReturn(FileUtils.readFileToString(filename));

    myAdapterRegistry.createAdapter(new URLString(filename));

    // Make sure our pre-processor was called - even though our pre-processor does nothing!
    verify(mockPreProcessor, times(1)).process(any(String.class));
  }

  @Test
  public void testMultiPreProcessorCalled() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterBuilder builder = new ArrayList<AdapterBuilder>(myAdapterRegistry.builders()).get(0);
    builder.setConfigurationPreProcessorLoader(mockPreProcessorLoader);
    ConfigPreProcessors preProcessorsList = new ConfigPreProcessors();
    preProcessorsList.add(mockPreProcessor);
    preProcessorsList.add(mockPreProcessor);
    preProcessorsList.add(mockPreProcessor);

    when(mockPreProcessorLoader.load(any(BootstrapProperties.class))).thenReturn(preProcessorsList);
    when(mockPreProcessor.process(any(String.class))).thenReturn(FileUtils.readFileToString(filename));

    myAdapterRegistry.createAdapter(new URLString(filename));

    // Make sure our pre-processors are called - even though our pre-processors do nothing!
    verify(mockPreProcessor, times(3)).process(any(String.class));
  }

  @Test
  public void testAddAdapterMBean() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObjectName = adapterManager.createObjectName();
    adapterManager.registerMBean();
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;

    myAdapterRegistry.addAdapter(adapterManager);
    assertEquals(ClosedState.getInstance(), adapterManager.getComponentState());
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testAddAdapterMBean_ExistingObjectName() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObjectName = adapterManager.createObjectName();
    adapterManager.registerMBean();
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));

    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;
    myAdapterRegistry.addAdapter(adapterManager);
    assertEquals(ClosedState.getInstance(), adapterManager.getComponentState());
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());

    try {
      myAdapterRegistry.addAdapter(adapterManager);
      fail();
    }
    catch (CoreException expected) {
      assertTrue(expected.getMessage().contains("already exists in the registry, remove it first"));
    }
  }

  @Test
  public void testCreateAdapter_URL() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;
    ObjectName objName = myAdapterRegistry.createAdapter(new URLString(filename));
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testCreateAdapter_NullUrl() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    try {
      ObjectName objName = myAdapterRegistry.createAdapter((URLString) null);
    }
    catch (CoreException expected) {
    }
  }

  @Test
  public void testCreateAdapterFromUrl_String() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;

    ObjectName objName = myAdapterRegistry.createAdapterFromUrl(filename.toURI().toString());
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testCreateAdapterFromUrl_NullUrlString() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    try {
      ObjectName objName = myAdapterRegistry.createAdapterFromUrl(null);
    }
    catch (CoreException expected) {
    }
  }

  @Test
  public void testProxy_CreateAdapter_URL() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;

    ObjectName objName = registry.createAdapter(new URLString(filename));
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
    assertEquals(expectedCount, registry.getAdapters().size());
  }

  @Test
  public void testCreateAdapter_String() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;

    ObjectName objName = myAdapterRegistry.createAdapter(xml);
    assertNotNull(objName);
    assertNotNull(myAdapterRegistry.getBuilder(objName));
    assertNotNull(myAdapterRegistry.getBuilderMBean(objName));
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testCreateAdapter_NoUniqueId_NoValidation() throws Exception {
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(new Adapter());
    AdapterRegistry myAdapterRegistry =
        (AdapterRegistry) AdapterRegistry.findInstance(new JunitBootstrapProperties(new Properties()));
    try {
      ObjectName objName = myAdapterRegistry.createAdapter(xml);
      fail();
    } catch (CoreException expected) {
      assertEquals("Adapter Unique ID is null/empty", expected.getMessage());
    }
  }

  @Test
  public void testProxy_CreateAdapter_String() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;

    ObjectName objName = registry.createAdapter(xml);
    assertNotNull(objName);
    assertNotNull(registry.getBuilder(objName));
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(expectedCount, registry.getAdapters().size());

  }

  @Test
  public void testValidateConfig_ValidXML() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    myAdapterRegistry.validateConfig(xml);
  }

  @Test
  public void testValidateConfig_InvalidXML() throws Exception {
    String xml = "<adapter><hello-world/></adapter>";
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    try {
      myAdapterRegistry.validateConfig(xml);
      fail();
    } catch (CoreException expected) {
      System.err.println(expected.getMessage());
    }
  }

  @Test
  public void testPersistAdapter_MBean_to_URL() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = myAdapterRegistry.createAdapter(xml);
    assertNotNull(objName);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    myAdapterRegistry.persistAdapter(manager, new URLString(filename));
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  @Test
  public void testProxy_PersistAdapter_Bean_to_URL() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    assertNotNull(objName);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    registry.persistAdapter(manager, new URLString(filename));
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  @Test
  public void testProxy_PersistAdapter_Bean_to_URL_String() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    assertNotNull(objName);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    registry.persistAdapter(manager, filename.toURI().toString());
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  @Test
  public void testPersistAdapter_ObjectName_To_File() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = myAdapterRegistry.createAdapter(xml);
    myAdapterRegistry.persistAdapter(objName, new URLString(filename));
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  @Test
  public void testProxy_PersistAdapter_ObjectName_To_URL() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    registry.persistAdapter(objName, new URLString(filename));
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  @Test
  public void testProxy_PersistAdapter_ObjectName_To_URL_String() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    registry.persistAdapter(objName, filename.toURI().toString());
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  @Test
  public void testDestroyAdapter_NotRegistered() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    int expectedCount = myAdapterRegistry.getAdapters().size();

    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ObjectName objName = adapterManager.createObjectName();
      assertTrue(mBeanServer.isRegistered(objName));
      assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
      myAdapterRegistry.destroyAdapter(adapterManager);
      assertFalse(mBeanServer.isRegistered(objName));
      assertEquals(ClosedState.getInstance(), adapterManager.getComponentState());
      assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
    }
    finally {
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testDestroyAdapter_MBean() throws Exception {
    AdapterRegistryMBean myAdapterRegistry = AdapterRegistry.findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);

    int expectedCount = myAdapterRegistry.getAdapters().size();

    ObjectName objName = myAdapterRegistry.createAdapter(xml);
    assertNotNull(myAdapterRegistry.getBuilder(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    myAdapterRegistry.destroyAdapter(manager);
    try {
      myAdapterRegistry.getBuilder(objName);
      fail();
    } catch (InstanceNotFoundException expected) {

    }
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testProxy_DestroyAdapter_MBean() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    int expectedCount = myAdapterRegistry.getAdapters().size();

    ObjectName objName = registry.createAdapter(xml);
    assertNotNull(registry.getBuilder(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    registry.destroyAdapter(manager);
    try {
      registry.getBuilder(objName);
      fail();
    } catch (InstanceNotFoundException expected) {

    }
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(expectedCount, registry.getAdapters().size());
  }

  @Test
  public void testProxy_DestroyAdapter_MBean_SharedConnection_JNDI() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    int expectedCount = myAdapterRegistry.getAdapters().size();

    ObjectName objName = registry.createAdapter(xml);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    InitialContext context = new InitialContext(contextEnv);
    context.lookup("adapter:comp/env/" + getName());
    myAdapterRegistry.destroyAdapter(manager);
    try {
      context.lookup("adapter:comp/env/" + getName());
    }
    catch (NamingException expected) {

    }
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(expectedCount, registry.getAdapters().size());
  }

  @Test
  public void testDestroyAdapter_ObjectName() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    int expectedCount = myAdapterRegistry.getAdapters().size();

    ObjectName objName = myAdapterRegistry.createAdapter(xml);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(1, myAdapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    myAdapterRegistry.destroyAdapter(objName);
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testProxy_DestroyAdapter_ObjectName() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);

    int expectedCount = myAdapterRegistry.getAdapters().size();

    ObjectName objName = myAdapterRegistry.createAdapter(xml);
    assertTrue(mBeanServer.isRegistered(objName));

    assertEquals(expectedCount + 1, myAdapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    myAdapterRegistry.destroyAdapter(objName);
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testStart() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    int expectedCount = adapterRegistry.getAdapters().size() + 1;

    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(expectedCount, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    AdapterRegistry.start(adapterRegistry.getAdapters());
    assertEquals(StartedState.getInstance(), manager.getComponentState());
  }

  @Test
  public void testStop() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    int expectedCount = adapterRegistry.getAdapters().size() + 1;

    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(expectedCount, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    AdapterRegistry.stop(adapterRegistry.getAdapters());
    assertEquals(StoppedState.getInstance(), manager.getComponentState());
  }

  @Test
  public void testClose() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);

    int expectedCount = adapterRegistry.getAdapters().size() + 1;
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(expectedCount, adapterRegistry.getAdapters().size());

    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    AdapterRegistry.close(adapterRegistry.getAdapters());
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
  }

  @Test
  public void testSendShutdownEvent() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    DefaultEventHandler evh = new DefaultEventHandler();
    StaticMockEventProducer producer = new StaticMockEventProducer(Arrays.asList(new Class[]
        {
        AdapterShutdownEvent.class
        }));
    producer.getMessages().clear();
    evh.setProducer(producer);
    adapter.setEventHandler(evh);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    int expectedCount = adapterRegistry.getAdapters().size() + 1;

    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(expectedCount, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    assertEquals(0, producer.messageCount());
    // This should send an extra event.
    AdapterRegistry.sendShutdownEvent(adapterRegistry.getAdapters());
    waitForMessages(producer, 1);
    assertEquals(1, producer.messageCount());
    AdapterRegistry.close(adapterRegistry.getAdapters());
    producer.getMessages().clear();
  }

  @Test
  public void testSendShutdownEvent_AdapterAlreadyClosed() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    DefaultEventHandler evh = new DefaultEventHandler();
    StaticMockEventProducer producer = new StaticMockEventProducer(Arrays.asList(new Class[]
        {
        AdapterShutdownEvent.class
        }));
    producer.getMessages().clear();
    evh.setProducer(producer);
    adapter.setEventHandler(evh);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);

    int expectedCount = adapterRegistry.getAdapters().size() + 1;

    ObjectName objName = adapterRegistry.createAdapter(xml);

    assertEquals(expectedCount, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    manager.requestClose();
    assertEquals(0, producer.messageCount());
    // This shouldn't send an extra event because the adapter is already closed.
    AdapterRegistry.sendShutdownEvent(adapterRegistry.getAdapters());
    Thread.sleep(1000);
    assertEquals(0, producer.messageCount());
    AdapterRegistry.close(adapterRegistry.getAdapters());
    producer.getMessages().clear();
  }

  @Test
  public void testGetConfigurationURL() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URLString expectedURL = new URLString(filename);
    ObjectName objName = adapterRegistry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, adapterRegistry.getConfigurationURL(objName));
  }

  @Test
  public void testGetConfigurationURL_NoURL() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertNull(adapterRegistry.getConfigurationURL(objName));
  }

  @Test
  public void testRemoveConfigurationURL() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URLString expectedURL = new URLString(filename);
    ObjectName objName = adapterRegistry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, adapterRegistry.getConfigurationURL(objName));
    assertTrue(adapterRegistry.removeConfigurationURL(objName));
    assertNull(adapterRegistry.getConfigurationURL(objName));
    assertFalse(adapterRegistry.removeConfigurationURL(objName));
  }

  @Test
  public void testDestroy_With_GetConfigurationURL() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URLString expectedURL = new URLString(filename);
    ObjectName objName = adapterRegistry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, adapterRegistry.getConfigurationURL(objName));
    adapterRegistry.destroyAdapter(objName);
    assertEquals(expectedURL, adapterRegistry.getConfigurationURL(objName));
  }

  @Test
  public void testProxy_GetConfigurationURL() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URLString expectedURL = new URLString(filename);
    ObjectName objName = registry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, registry.getConfigurationURL(objName));
  }

  @Test
  public void testProxy_GetConfigurationURL_NoURL() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, myAdapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertNull(registry.getConfigurationURL(objName));
  }

  @Test
  public void testProxy_RemoveConfigurationURL() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, adapterRegistry.createObjectName(),
        AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URLString expectedURL = new URLString(filename);
    ObjectName objName = registry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, registry.getConfigurationURL(objName));
    assertTrue(registry.removeConfigurationURL(objName));
    assertNull(registry.getConfigurationURL(objName));
    assertNull(adapterRegistry.getConfigurationURL(objName));
    assertFalse(registry.removeConfigurationURL(objName));
  }

  @Test
  public void testProxy_Destroy_With_GetConfigurationURL() throws Exception {
    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, adapterRegistry.createObjectName(), AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URLString expectedURL = new URLString(filename);
    ObjectName objName = registry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, registry.getConfigurationURL(objName));
    registry.destroyAdapter(objName);
    assertEquals(expectedURL, registry.getConfigurationURL(objName));
  }

  @Test
  public void testReloadFromVersionControl_NoVCS() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    try {
      myAdapterRegistry.reloadFromVersionControl();
      fail();
    }
    catch (CoreException expected) {
      assertEquals("No Runtime Version Control", expected.getMessage());
    }
  }

  @Test
  public void testReloadFromVersionControl_WithVCS() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    Properties p = new Properties();
    p.put("adapterConfigUrl.1", filename.toURI().toURL().toString());
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry.findInstance(new JunitBootstrapProperties(p));

    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;

    ObjectName myObjectName = myAdapterRegistry.createAdapter(new URLString(filename));
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
    AdapterBuilder builder = new ArrayList<AdapterBuilder>(myAdapterRegistry.builders()).get(0);
    builder.overrideRuntimeVCS(new MockRuntimeVersionControl());
    // This should destroy the adapter just created; and create a new one...
    myAdapterRegistry.reloadFromVersionControl();
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testReloadFromVersionControl_WithVCS_2Builders() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter1 = createAdapter(adapterName, 2, 2);
    File firstFile = deleteLater(adapter1);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter1, firstFile);
    Adapter adapter2 = createAdapter(adapterName + "2", 2, 2);
    File secondFile = deleteLater(adapter2);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter2, secondFile);

    Properties first = new Properties();
    first.put("adapterConfigUrl.1", firstFile.toURI().toURL().toString());
    Properties second = new Properties();
    second.put("adapterConfigUrl.1", secondFile.toURI().toURL().toString());

    AdapterRegistry adapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(first);
    int preTestSize = adapterRegistry.getAdapters().size();

    adapterRegistry.addConfiguration(second);
    // No adapters created yet.
    assertEquals(preTestSize, adapterRegistry.getAdapters().size());

    AdapterBuilder builder = new ArrayList<AdapterBuilder>(adapterRegistry.builders()).get(1);
    builder.overrideRuntimeVCS(new MockRuntimeVersionControl());

    // Should create 2 instances
    adapterRegistry.reloadFromVersionControl();
    assertEquals(preTestSize + 2, adapterRegistry.getAdapters().size());

  }

  @Test
  public void testReloadFromConfig() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    Properties p = new Properties();
    p.put("adapterConfigUrl.1", filename.toURI().toURL().toString());
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry.findInstance(new JunitBootstrapProperties(p));

    int expectedCount = myAdapterRegistry.getAdapters().size() + 1;

    ObjectName myObjectName = myAdapterRegistry.createAdapter(new URLString(filename));

    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());

    // This should destroy the adapter just created; and create a new one...
    myAdapterRegistry.reloadFromConfig();
    assertEquals(expectedCount, myAdapterRegistry.getAdapters().size());
  }

  @Test
  public void testGetVersionControl() throws Exception {
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry
        .findInstance(new JunitBootstrapProperties(new Properties()));
    assertNull(myAdapterRegistry.getVersionControl());
    AdapterBuilder builder = new ArrayList<AdapterBuilder>(myAdapterRegistry.builders()).get(0);
    builder.overrideRuntimeVCS(new MockRuntimeVersionControl());
    assertEquals("MOCK", myAdapterRegistry.getVersionControl());
  }

  @Test
  public void testGetClassDescription() throws Exception {
    Properties custom = new Properties();
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry.findInstance(new JunitBootstrapProperties(custom));

    String addMetadataServiceJsonDef = myAdapterRegistry.getClassDefinition("com.adaptris.core.services.metadata.AddMetadataService");
    ClassDescriptor addMetadataServiceDef = (ClassDescriptor) new XStreamJsonMarshaller().unmarshal(addMetadataServiceJsonDef);

    assertEquals("com.adaptris.core.services.metadata.AddMetadataService", addMetadataServiceDef.getClassName());
    assertEquals("add-metadata-service", addMetadataServiceDef.getAlias());
    assertEquals("Add Static Metadata to a Message", addMetadataServiceDef.getSummary());
    assertEquals("service,metadata", addMetadataServiceDef.getTags());
    assertEquals(2, addMetadataServiceDef.getClassDescriptorProperties().size());
    assertEquals("service", addMetadataServiceDef.getClassType());
  }

  @Test
  public void testClassDescriptionGetSubTypes() throws Exception {
    Properties custom = new Properties();
    AdapterRegistry myAdapterRegistry = (AdapterRegistry) AdapterRegistry.findInstance(new JunitBootstrapProperties(custom));

    String adapterRegistryTestJsonDef = myAdapterRegistry.getClassDefinition(ReformatMetadata.class.getCanonicalName());
    ClassDescriptor adapterRegistryTestDef = (ClassDescriptor) new XStreamJsonMarshaller().unmarshal(adapterRegistryTestJsonDef);

    assertTrue(adapterRegistryTestDef.getSubTypes().size() > 0);
    assertTrue(adapterRegistryTestDef.getSubTypes().contains(ReformatDateService.class.getName()));
  }

  private class MockRuntimeVersionControl implements RuntimeVersionControl {

    @Override
    public String getImplementationName() {
      return "MOCK";
    }

    @Override
    public void update() throws VcsException {
    }

    @Override
    public void checkout() throws VcsException {
    }

    @Override
    public void setBootstrapProperties(BootstrapProperties bootstrapProperties) {
    }

    @Override
    public VersionControlSystem getApi(Properties properties) throws VcsException {
      return null;
    }

  }
}
