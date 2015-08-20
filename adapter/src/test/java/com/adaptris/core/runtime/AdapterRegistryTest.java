package com.adaptris.core.runtime;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.JndiContextFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.event.AdapterShutdownEvent;
import com.adaptris.core.management.AdapterConfigManager;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.vcs.RuntimeVersionControl;
import com.adaptris.core.management.vcs.VcsException;
import com.adaptris.core.management.vcs.VersionControlSystem;
import com.adaptris.core.stubs.JunitBootstrapProperties;
import com.adaptris.core.stubs.StaticMockEventProducer;
import com.adaptris.core.util.JmxHelper;

@SuppressWarnings("deprecation")
public class AdapterRegistryTest extends ComponentManagerCase {

  @Mock private DummyConfigurationPreProcessor mockPreProcessor;
  
  @Mock private ConfigurationPreProcessorLoader mockPreProcessorLoader;
  
  @Spy private ConfigurationPreProcessorLoader spyPreProcessorLoader;
  
  private AdapterRegistry adapterRegistry;
  private ObjectName registryObjectName;

  private transient Properties contextEnv = new Properties();

  public AdapterRegistryTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    adapterRegistry = new AdapterRegistry(new JunitBootstrapProperties(new Properties()));
    adapterRegistry.registerMBean();
    registryObjectName = adapterRegistry.createObjectName();
    contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    spyPreProcessorLoader = new ConfigurationPreProcessorFactory();
    
    MockitoAnnotations.initMocks(this);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    adapterRegistry.unregisterMBean();
  }
  
  public void testGetConfiguration() throws Exception {
    AdapterRegistry myAdapterRegistry = new AdapterRegistry(new JunitBootstrapProperties(new Properties()));
    assertEquals(0, myAdapterRegistry.getConfiguration().size());
  }

  public void testPutConfigurationUrl() throws Exception {
    AdapterRegistry myAdapterRegistry = new AdapterRegistry(new JunitBootstrapProperties(new Properties()));
    URL url = new URL("http://localhost/1234");
    myAdapterRegistry.putConfigurationURL(myAdapterRegistry.createObjectName(), url);
    assertEquals(url, myAdapterRegistry.getConfigurationURL(myAdapterRegistry.createObjectName()));
  }

  public void testPreProcessorsLoaded() throws Exception {

    Properties bsProperties = new Properties();
    bsProperties.put(AdapterConfigManager.CONFIGURATION_PRE_PROCESSORS, DummyConfigurationPreProcessor.class.getName());
    adapterRegistry = new AdapterRegistry(new JunitBootstrapProperties(bsProperties));
    adapterRegistry.setConfigurationPreProcessorLoader(spyPreProcessorLoader);
    
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    
    adapterRegistry.createAdapter(filename.toURI().toURL());
    
    verify(spyPreProcessorLoader, times(1)).load(any(BootstrapProperties.class));
  }
  
  public void testPreProcessorCalled() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    
    adapterRegistry.setConfigurationPreProcessorLoader(mockPreProcessorLoader);
    PreProcessorsList preProcessorsList = new PreProcessorsList();
    preProcessorsList.add(mockPreProcessor);
    
    when(mockPreProcessorLoader.load(any(BootstrapProperties.class))).thenReturn(preProcessorsList);
    when(mockPreProcessor.process(any(String.class))).thenReturn(FileUtils.readFileToString(filename));
    
    adapterRegistry.createAdapter(filename.toURI().toURL());
    
    // Make sure our pre-processor was called - even though our pre-processor does nothing!
    verify(mockPreProcessor, times(1)).process(any(String.class));
  }
  
  public void testMultiPreProcessorCalled() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    
    adapterRegistry.setConfigurationPreProcessorLoader(mockPreProcessorLoader);
    PreProcessorsList preProcessorsList = new PreProcessorsList();
    preProcessorsList.add(mockPreProcessor);
    preProcessorsList.add(mockPreProcessor);
    preProcessorsList.add(mockPreProcessor);
    
    when(mockPreProcessorLoader.load(any(BootstrapProperties.class))).thenReturn(preProcessorsList);
    when(mockPreProcessor.process(any(String.class))).thenReturn(FileUtils.readFileToString(filename));
    
    adapterRegistry.createAdapter(filename.toURI().toURL());
    
    // Make sure our pre-processors are called - even though our pre-processors do nothing!
    verify(mockPreProcessor, times(3)).process(any(String.class));
  }

  public void testAdapterRegistry_DifferentObjectID() throws Exception {
    AdaptrisMarshaller marshaller = DefaultMarshaller.getDefaultMarshaller();
    Properties p = new Properties();
    p.setProperty(AdapterRegistryMBean.CFG_KEY_REGISTRY_JMX_ID, getName());
    AdapterRegistry myRegistry = new AdapterRegistry(new JunitBootstrapProperties(p));
    myRegistry.registerMBean();
    ObjectName myRegistryObjectName = myRegistry.createObjectName();
    assertNotSame(myRegistryObjectName, registryObjectName);

    AdapterRegistryMBean myRegistryProxy = JMX.newMBeanProxy(JmxHelper.findMBeanServer(), myRegistryObjectName,
        AdapterRegistryMBean.class);

    adapterRegistry.createAdapter(marshaller.marshal(createAdapter(guid.safeUUID(), 1, 1)));
    adapterRegistry.createAdapter(marshaller.marshal(createAdapter(guid.safeUUID(), 1, 1)));

    assertEquals(2, adapterRegistry.getAdapters().size());
    assertEquals(0, myRegistryProxy.getAdapters().size());

    myRegistryProxy.createAdapter(marshaller.marshal(createAdapter(guid.safeUUID(), 1, 1)));
    myRegistryProxy.createAdapter(marshaller.marshal(createAdapter(guid.safeUUID(), 1, 1)));

    assertEquals(2, adapterRegistry.getAdapters().size());
    assertEquals(2, myRegistryProxy.getAdapters().size());

  }

  public void testAddAdapterMBean() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObjectName = adapterManager.createObjectName();
    adapterManager.registerMBean();
    adapterRegistry.addAdapter(adapterManager);
    assertEquals(ClosedState.getInstance(), adapterManager.getComponentState());
    assertEquals(1, adapterRegistry.getAdapters().size());
  }

  public void testAddAdapterMBean_ExistingObjectName() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObjectName = adapterManager.createObjectName();
    adapterManager.registerMBean();
    adapterRegistry.addAdapter(adapterManager);
    assertEquals(ClosedState.getInstance(), adapterManager.getComponentState());
    assertEquals(1, adapterRegistry.getAdapters().size());
    try {
      adapterRegistry.addAdapter(adapterManager);
      fail();
    }
    catch (CoreException expected) {
      assertTrue(expected.getMessage().contains("already exists in the registry, remove it first"));
    }
  }

  public void testCreateAdapter_URL() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    ObjectName objName = adapterRegistry.createAdapter(filename.toURI().toURL());
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(1, adapterRegistry.getAdapters().size());
  }

  public void testCreateAdapter_NullUrl() throws Exception {
    try {
      ObjectName objName = adapterRegistry.createAdapter((URL) null);
    }
    catch (CoreException expected) {
    }
  }

  public void testProxy_CreateAdapter_URL() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    ObjectName objName = registry.createAdapter(filename.toURI().toURL());
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(1, adapterRegistry.getAdapters().size());
    assertEquals(1, registry.getAdapters().size());
  }

  public void testCreateAdapter_String() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(1, adapterRegistry.getAdapters().size());
  }

  public void testProxy_CreateAdapter_String() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    assertNotNull(manager);
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
    assertEquals(1, registry.getAdapters().size());
  }


  public void testValidateConfig_ValidXML() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    adapterRegistry.validateConfig(xml);
  }

  public void testValidateConfig_InvalidXML() throws Exception {
    String xml = "ABCDEFG";
    try {
      adapterRegistry.validateConfig(xml);
      fail();
    } catch (CoreException expected) {
    }
  }

  public void testPersistAdapter_MBean_to_URL() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertNotNull(objName);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    adapterRegistry.persistAdapter(manager, filename.toURI().toURL());
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  public void testProxy_PersistAdapter_Bean_to_URL() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    assertNotNull(objName);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    registry.persistAdapter(manager, filename.toURI().toURL());
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  public void testPersistAdapter_ObjectName_To_File() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    adapterRegistry.persistAdapter(objName, filename.toURI().toURL());
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  public void testProxy_PersistAdapter_ObjectName_To_URL() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    registry.persistAdapter(objName, filename.toURI().toURL());
    Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(filename);
    assertRoundtripEquality(adapter, marshalledAdapter);
  }


  public void testDestroyAdapter_NotRegistered() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ObjectName objName = adapterManager.createObjectName();
      assertTrue(mBeanServer.isRegistered(objName));
      assertEquals(0, adapterRegistry.getAdapters().size());
      adapterRegistry.destroyAdapter(adapterManager);
      assertFalse(mBeanServer.isRegistered(objName));
      assertEquals(ClosedState.getInstance(), adapterManager.getComponentState());
      assertEquals(0, adapterRegistry.getAdapters().size());
    }
    finally {
      adapterManager.unregisterMBean();
    }
  }

  public void testDestroyAdapter_MBean() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    adapterRegistry.destroyAdapter(manager);
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(0, adapterRegistry.getAdapters().size());
  }

  public void testProxy_DestroyAdapter_MBean() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    adapterRegistry.destroyAdapter(manager);
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(0, registry.getAdapters().size());
  }

  public void testProxy_DestroyAdapter_MBean_SharedConnection_JNDI() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    InitialContext context = new InitialContext(contextEnv);
    context.lookup("adapter:comp/env/" + getName());
    adapterRegistry.destroyAdapter(manager);
    try {
      context.lookup("adapter:comp/env/" + getName());
    }
    catch (NamingException expected) {

    }
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(0, registry.getAdapters().size());
  }

  public void testDestroyAdapter_ObjectName() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(1, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    adapterRegistry.destroyAdapter(objName);
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(0, adapterRegistry.getAdapters().size());
  }

  public void testProxy_DestroyAdapter_ObjectName() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(1, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    adapterRegistry.destroyAdapter(objName);
    assertFalse(mBeanServer.isRegistered(objName));
    assertEquals(0, adapterRegistry.getAdapters().size());
  }

  public void testStart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(1, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    AdapterRegistry.start(adapterRegistry.getAdapters());
    assertEquals(StartedState.getInstance(), manager.getComponentState());
  }

  public void testStop() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(1, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    AdapterRegistry.stop(adapterRegistry.getAdapters());
    assertEquals(StoppedState.getInstance(), manager.getComponentState());
  }

  public void testClose() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(1, adapterRegistry.getAdapters().size());
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, objName, AdapterManagerMBean.class);
    manager.requestStart();
    AdapterRegistry.close(adapterRegistry.getAdapters());
    assertEquals(ClosedState.getInstance(), manager.getComponentState());
  }

  public void testSendShutdownEvent() throws Exception {
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
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(1, adapterRegistry.getAdapters().size());
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

  public void testSendShutdownEvent_AdapterAlreadyClosed() throws Exception {
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
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertEquals(1, adapterRegistry.getAdapters().size());
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

  public void testGetConfigurationURL() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URL expectedURL = filename.toURI().toURL();
    ObjectName objName = adapterRegistry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, adapterRegistry.getConfigurationURL(objName));
  }

  public void testGetConfigurationURL_NoURL() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = adapterRegistry.createAdapter(xml);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertNull(adapterRegistry.getConfigurationURL(objName));
  }

  public void testRemoveConfigurationURL() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URL expectedURL = filename.toURI().toURL();
    ObjectName objName = adapterRegistry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, adapterRegistry.getConfigurationURL(objName));
    assertTrue(adapterRegistry.removeConfigurationURL(objName));
    assertNull(adapterRegistry.getConfigurationURL(objName));
    assertFalse(adapterRegistry.removeConfigurationURL(objName));
  }

  public void testDestroy_With_GetConfigurationURL() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URL expectedURL = filename.toURI().toURL();
    ObjectName objName = adapterRegistry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, adapterRegistry.getConfigurationURL(objName));
    adapterRegistry.destroyAdapter(objName);
    assertEquals(expectedURL, adapterRegistry.getConfigurationURL(objName));
  }

  public void testProxy_GetConfigurationURL() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URL expectedURL = filename.toURI().toURL();
    ObjectName objName = registry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, registry.getConfigurationURL(objName));
  }

  public void testProxy_GetConfigurationURL_NoURL() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    ObjectName objName = registry.createAdapter(xml);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertNull(registry.getConfigurationURL(objName));
  }

  public void testProxy_RemoveConfigurationURL() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URL expectedURL = filename.toURI().toURL();
    ObjectName objName = registry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, registry.getConfigurationURL(objName));
    assertTrue(registry.removeConfigurationURL(objName));
    assertNull(registry.getConfigurationURL(objName));
    assertNull(adapterRegistry.getConfigurationURL(objName));
    assertFalse(registry.removeConfigurationURL(objName));
  }

  public void testProxy_Destroy_With_GetConfigurationURL() throws Exception {
    AdapterRegistryMBean registry = JMX.newMBeanProxy(mBeanServer, registryObjectName, AdapterRegistryMBean.class);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    URL expectedURL = filename.toURI().toURL();
    ObjectName objName = registry.createAdapter(expectedURL);
    assertNotNull(objName);
    assertTrue(mBeanServer.isRegistered(objName));
    assertEquals(expectedURL, registry.getConfigurationURL(objName));
    registry.destroyAdapter(objName);
    assertEquals(expectedURL, registry.getConfigurationURL(objName));
  }

  public void testReloadFromVersionControl_NoVCS() throws Exception {
    AdapterRegistry myAdapterRegistry = new AdapterRegistry(new JunitBootstrapProperties(new Properties()));
    try {
      myAdapterRegistry.reloadFromVersionControl();
      fail();
    }
    catch (CoreException expected) {
      assertEquals("No Runtime Version Control", expected.getMessage());
    }
  }

  public void testReloadFromVersionControl_WithVCS() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    File filename = deleteLater(adapter);
    DefaultMarshaller.getDefaultMarshaller().marshal(adapter, filename);
    Properties p = new Properties();
    p.put("adapterConfigUrl.1", filename.toURI().toURL().toString());
    AdapterRegistry myAdapterRegistry = new AdapterRegistry(new JunitBootstrapProperties(p));
    ObjectName myObjectName = myAdapterRegistry.createAdapter(filename.toURI().toURL());
    assertEquals(1, myAdapterRegistry.getAdapters().size());

    myAdapterRegistry.overrideRuntimeVCS(new MockRuntimeVersionControl());
    // This should destroy the adapter just created; and create a new one...
    myAdapterRegistry.reloadFromVersionControl();
    assertEquals(1, myAdapterRegistry.getAdapters().size());
  }

  public void testGetVersionControl() throws Exception {
    AdapterRegistry myAdapterRegistry = new AdapterRegistry(new JunitBootstrapProperties(new Properties()));
    assertNull(myAdapterRegistry.getVersionControl());
    myAdapterRegistry.overrideRuntimeVCS(new MockRuntimeVersionControl());
    assertEquals("MOCK", myAdapterRegistry.getVersionControl());
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
