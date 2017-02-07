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

package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.fs.FsProducer;
import com.adaptris.core.ftp.FtpConnection;
import com.adaptris.core.http.jetty.EmbeddedConnection;
import com.adaptris.core.jdbc.AdvancedJdbcPooledConnection;
import com.adaptris.core.jdbc.JdbcPooledConnection;
import com.adaptris.core.jdbc.PooledConnectionProperties;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConsumer;
import com.adaptris.core.jms.JmsProducer;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.lifecycle.FilteredSharedComponentStart;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.stubs.MockConfirmService;
import com.adaptris.core.stubs.MockConnection;
import com.adaptris.core.stubs.MockService;
import com.adaptris.core.transaction.DummyTransactionManager;
import com.adaptris.core.transaction.SharedTransactionManager;
import com.adaptris.core.transaction.TransactionManager;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

/**
 * Base test for testing XSTream configuration vis-a-vis shared components.
 */
public class SharedComponentListTest extends ExampleConfigCase {
  private static final String GUID_PATTERN = "(?m)^\\s*<unique-id>[0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12}<\\/unique-id>$";

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "SharedComponentConfig.baseDir";

  private static final String EXAMPLE_XML_NOTES = "<!--\n\n" + "This is an example of using shared connections and shared services. "
      + "\nEach connection/service that is shared should have an unique id associated with it; this can be "
      + "\nreferred to in configuration by using a 'shared-connection' connection or 'shared-service' service implementation."
      + "\n\nNote that this is different to XStream referencing by ID; "
      + "\nwhile it is possible to share arbitrary components such as producers in that way, "
      + "\nbehaviour may be undefined as those components may not be" + "\nthreadsafe or reentrant" + "\n\n-->\n";

  private enum ConnectionBuilder {
    FtpConnectionBuilder {
      AdaptrisConnection build() {
        FtpConnection c = new FtpConnection("goofy_edison");
        c.setLookupName("adapter:comp/env/optional-lookup-name");
        c.setCacheConnection(true);
        return c;
      }
    },
    PooledJdbcConnectionBuilder {
      AdaptrisConnection build() {
        JdbcPooledConnection connection = new JdbcPooledConnection();
        connection.setUniqueId("vigilant_hypatia");
        connection.setConnectUrl("jdbc:mysql://localhost:3306/insert-database");
        return connection;
      }
    },
    AdvancedPooledJdbcConnectionBuilder {
      AdaptrisConnection build() {
        AdvancedJdbcPooledConnection connection = new AdvancedJdbcPooledConnection();
        connection.setUniqueId("amused_goldberg");
        connection.setConnectUrl("jdbc:mysql://localhost:3306/query-database");
        KeyValuePairSet poolProps = new KeyValuePairSet();
        poolProps.add(new KeyValuePair(PooledConnectionProperties.maxPoolSize.name(), "20"));
        poolProps.add(new KeyValuePair(PooledConnectionProperties.minPoolSize.name(), "5"));
        connection.setConnectionPoolProperties(poolProps);
        return connection;
      }
    };

    abstract AdaptrisConnection build();

  }
  private AdaptrisMarshaller myMarshaller;

  public SharedComponentListTest(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // We force ourselves to use the standard marshaller as we know that ExampleConfigCase doesn't actually
    // use the standard marshaller for config generation.
    myMarshaller = DefaultMarshaller.getDefaultMarshaller();
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);
    Adapter w = (Adapter) object;
    String marshalled = myMarshaller.marshal(w).replaceAll(GUID_PATTERN, "");
    result = result + marshalled;
    return result;
  }

  private static String filterGuid(String uid) {
    if (isBlank(uid) || uid.matches(GUID_PATTERN)) {
      return "";
    }
    return "(" + uid + ")";
  }

  public void testObjectReferences() throws Exception {
    Adapter srcAdapter = createAdapter();
    String xml = myMarshaller.marshal(srcAdapter);
    Adapter roundtrip = (Adapter) myMarshaller.unmarshal(xml);
    assertRoundtripEquality(srcAdapter, roundtrip);
  }

  @Override
  protected String createBaseFileName(Object object) {
    return object.getClass().getName();
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object) + EXAMPLE_XML_NOTES;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter adapter = null;
    try {
      adapter = createAdapter();
      for (ConnectionBuilder b : ConnectionBuilder.values()) {
        adapter.getSharedComponents().addConnection(b.build());
      }
      TransactionManager transactionManager = new DummyTransactionManager("nifty_mcclintock",
          "adapter:comp/env/myTransactionManager");
      adapter.getSharedComponents().setTransactionManager(transactionManager);
      Channel c = new Channel();
      c.setProduceConnection(new SharedConnection("goofy_edison"));
      c.setConsumeConnection(new EmbeddedConnection());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return adapter;
  }

  public void testDebug() throws Exception {
    SharedComponentList list = new SharedComponentList();
    assertFalse(list.isDebug());
    assertNull(list.getDebug());
    list.setDebug(Boolean.TRUE);
    assertTrue(list.isDebug());
    assertEquals(Boolean.TRUE, list.getDebug());
    list.setDebug(null);
    assertFalse(list.isDebug());
    assertNull(list.getDebug());
  }

  public void testAddConnection() throws Exception {
    SharedComponentList list = new SharedComponentList();
    try {
      list.addConnection(new MockConnection());
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(0, list.getConnections().size());
    try {
      list.addConnection(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(0, list.getConnections().size());
    // Should have no effect as you're just adding to a clone.
    list.getConnections().add(new MockConnection());
    assertEquals(0, list.getConnections().size());
    list.addConnection(new MockConnection(getName()));

    assertFalse(list.addConnection(new MockConnection(getName())));
    assertEquals(1, list.getConnections().size());
  }
  
  public void testAddService() throws Exception {
    SharedComponentList list = new SharedComponentList();

    assertEquals(0, list.getServices().size());
    try {
      list.addServiceCollection(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(0, list.getServices().size());
    // Should have no effect as you're just adding to a clone.
    list.getServices().add(new MockService());
    assertEquals(0, list.getServices().size());
    MockService mockConfirmService = new MockService();
    mockConfirmService.setUniqueId("Something");
    list.addServiceCollection(mockConfirmService);

    assertFalse(list.addServiceCollection(mockConfirmService));
    assertEquals(1, list.getServices().size());
  }

  public void testAddConnections() throws Exception {
    SharedComponentList list = new SharedComponentList();
    try {
      list.addConnections(Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection()}));
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(0, list.getConnections().size());
    try {
      list.addConnections(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(0, list.getConnections().size());

    // This is valid, we don't check for duplicates until later.
    Collection<AdaptrisConnection> rejected =
        list.addConnections(Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName())}));
    assertEquals(1, list.getConnections().size());
    assertEquals(1, rejected.size());
  }
  
  public void testAddServices() throws Exception {
    SharedComponentList list = new SharedComponentList();
    try {
      list.addServiceCollections(null);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(0, list.getConnections().size());

    // This is valid, we don't check for duplicates until later.
    MockService service1 = new MockService();
    MockService service2 = new MockService();
    service2.setUniqueId(service1.getUniqueId());
    
    Collection<Service> rejected =
        list.addServiceCollections(Arrays.asList(new ServiceImp[] {service1, service2}));
    assertEquals(1, list.getServices().size());
    assertEquals(1, rejected.size());
  }

  public void testSetConnections() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> bad = Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection()});
    List<AdaptrisConnection> alsoBad =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName())});
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    try {
      list.setConnections(bad);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(0, list.getConnections().size());
    try {
      list.setConnections(alsoBad);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(0, list.getConnections().size());
    list.setConnections(good);
    assertEquals(2, list.getConnections().size());
  }

  public void testContainsConnection() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(good);
    assertEquals(2, list.getConnections().size());
    assertTrue(list.containsConnection(getName()));
  }

  public void testLifecycle_Init() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(good);
    try {
      LifecycleHelper.init(list);
      assertEquals(2, list.getConnections().size());
      for (AdaptrisConnection c : list.getConnections()) {
        assertEquals(InitialisedState.getInstance(), c.retrieveComponentState());
      }
    } finally {
      stop(list);
    }
  }

  public void testLifecycle_Start() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(good);
    try {
      LifecycleHelper.init(list);
      LifecycleHelper.start(list);
      assertEquals(2, list.getConnections().size());
      for (AdaptrisConnection c : list.getConnections()) {
        assertEquals(StartedState.getInstance(), c.retrieveComponentState());
      }
    } finally {
      stop(list);
    }
  }

  public void testLifecycle_Stop() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(good);
    try {
      LifecycleHelper.init(list);
      LifecycleHelper.start(list);
      LifecycleHelper.stop(list);
      assertEquals(2, list.getConnections().size());
      for (AdaptrisConnection c : list.getConnections()) {
        assertEquals(StoppedState.getInstance(), c.retrieveComponentState());
      }
    } finally {
      stop(list);
    }
  }

  public void testLifecycle_Close() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(good);
    try {
      LifecycleHelper.init(list);
      LifecycleHelper.start(list);
      LifecycleHelper.stop(list);
      LifecycleHelper.close(list);
      assertEquals(2, list.getConnections().size());
      for (AdaptrisConnection c : list.getConnections()) {
        assertEquals(ClosedState.getInstance(), c.retrieveComponentState());
      }
    } finally {
      stop(list);
    }
  }


  // FilteredStart was removing connections
  // from the underlying list.
  public void testInterlok_1096() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(new ArrayList<>(good));
    FilteredSharedComponentStart starter = new FilteredSharedComponentStart();
    starter.addExclude(getName());
    starter.addExclude(getName() + "_1");
    list.setLifecycleStrategy(starter);
    try {
      LifecycleHelper.init(list);
      LifecycleHelper.start(list);
      assertEquals(2, list.getConnections().size());
      for (AdaptrisConnection c : good) {
        assertEquals(ClosedState.getInstance(), c.retrieveComponentState());
      }
    } finally {
      stop(list);
    }
  }


  public void testRemoveConnection() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(good);
    assertEquals(2, list.getConnections().size());
    Collection<AdaptrisConnection> removed = list.removeConnection(getName());
    assertEquals(1, removed.size());
    assertFalse(list.containsConnection(getName()));
    assertEquals(1, list.getConnections().size());
  }

  public void testRemoveConnection_unbindsJNDI() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    InitialContext initialContext = new InitialContext(env);

    try {
      start(adapter);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertEquals(getName(), lookedup.getUniqueId());
      adapter.getSharedComponents().removeConnection(getName());
      try {
        initialContext.lookup("adapter:comp/env/" + getName());
        fail();
      } catch (NamingException expected) {
      }
    } finally {
      stop(adapter);
    }
  }

  public void testBindJNDI() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    InitialContext initialContext = new InitialContext(env);

    try {
      start(adapter);
      adapter.getSharedComponents().addConnection(new NullConnection(getName()));
      adapter.getSharedComponents().bindJNDI(getName());
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertEquals(getName(), lookedup.getUniqueId());
      adapter.getSharedComponents().bindJNDI("ShouldGetIgnored");
    } finally {
      stop(adapter);
    }
  }

  public void testBindJNDITransactionManager() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    InitialContext initialContext = new InitialContext(env);

    try {
      start(adapter);
      adapter.getSharedComponents().setTransactionManager(new DummyTransactionManager(getName(), null));
      adapter.getSharedComponents().bindJNDI(getName());
      TransactionManager lookedup = (TransactionManager) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertEquals(getName(), lookedup.getUniqueId());
      adapter.getSharedComponents().bindJNDI("ShouldGetIgnored");
    } finally {
      stop(adapter);
    }
  }

  public void testSharedConnection_StandardLookup() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    try {
      start(adapter);
      SharedConnection conn = new SharedConnection(getName());
      conn.retrieveConnection(NullConnection.class);
    } finally {
      stop(adapter);
    }
  }

  public void testSharedTransactionManager_StandardLookup() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    adapter.getSharedComponents().setTransactionManager(new DummyTransactionManager(getName(), getName()));
    try {
      start(adapter);
      SharedTransactionManager conn = new SharedTransactionManager(getName());
      conn.init();
      assertNotNull(conn.proxiedTransactionManager());
    } finally {
      stop(adapter);
    }
  }

  public void testSharedTransactionManager_CompEnvLookupName() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    adapter.getSharedComponents().setTransactionManager(new DummyTransactionManager(getName(), null));
    try {
      start(adapter);
      SharedTransactionManager conn = new SharedTransactionManager("comp/env/" + getName());
      conn.init();
      assertNotNull(conn.proxiedTransactionManager());
    } finally {
      stop(adapter);
    }
  }

  public void testSharedConnection_Lookup_CompEnvLookupName() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    try {
      start(adapter);
      SharedConnection conn = new SharedConnection("comp/env/" + getName());
      conn.retrieveConnection(NullConnection.class);
    } finally {
      stop(adapter);
    }
  }

  public void testSharedConnection_Lookup_FallbackToPlainName() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    NullConnection nc = new NullConnection(getName());
    nc.setLookupName(getName());
    adapter.getSharedComponents().addConnection(nc);
    try {
      start(adapter);
      SharedConnection conn = new SharedConnection(getName());
      conn.retrieveConnection(NullConnection.class);
    } finally {
      stop(adapter);
    }
  }

  public void testAddConnection_BindsToJndiWhenStarted() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    InitialContext initialContext = new InitialContext(env);

    try {
      start(adapter);
      stop(adapter);
      adapter.getSharedComponents().addConnection(new NullConnection(getName()));
      start(adapter);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertNotNull(lookedup);
      assertEquals(getName(), lookedup.getUniqueId());
    } finally {
      stop(adapter);
    }
  }

  public void testClose_UnbindsFromJNDI() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    InitialContext initialContext = new InitialContext(env);

    try {
      adapter.getSharedComponents().addConnection(new NullConnection(getName()));
      adapter.getSharedComponents().addConnection(new NullConnection(getName() + "_2"));
      start(adapter);
      NullConnection lookedup = (NullConnection) initialContext.lookup("adapter:comp/env/" + getName());
      assertEquals(getName(), lookedup.getUniqueId());
      stop(adapter);
      try {
        initialContext.lookup("adapter:comp/env/" + getName());
        fail();
      } catch (NamingException expected) {

      }
      // Now a start request should rebind to JNDI.
      start(adapter);
      initialContext.lookup("adapter:comp/env/" + getName());
      initialContext.lookup("adapter:comp/env/" + getName() + "_2");
    } finally {
      stop(adapter);
    }
  }

  public void testGetConnectionIds() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(good);
    assertEquals(2, list.getConnections().size());
    assertEquals(2, list.getConnectionIds().size());
    assertTrue(list.getConnectionIds().contains(getName()));
  }

  public void testGetConnections() throws Exception {
    SharedComponentList list = new SharedComponentList();
    List<AdaptrisConnection> good =
        Arrays.asList(new AdaptrisConnection[] {new MockConnection(getName()), new MockConnection(getName() + "_1")});
    list.setConnections(good);
    List<AdaptrisConnection> copy = list.getConnections();
    copy.add(new NullConnection(getName()));
    copy.add(new NullConnection(getName()));
    copy.add(new NullConnection());
    copy.add(new NullConnection());
    // That should have no effect, and it should still be valid.
    assertEquals(2, list.getConnections().size());
    assertEquals(2, list.getConnectionIds().size());

    try {
      list.setConnections(copy);
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertEquals(2, list.getConnections().size());

  }


  public void testConnectionState() throws Exception {
    Adapter adapter = AdapterTest.createAdapter(getName());
    MockConnection connection = new MockConnection(getName());
    adapter.getSharedComponents().addConnection(connection);
    StandaloneProducer producer = new StandaloneProducer(new SharedConnection(getName()), new NullMessageProducer());
    StandardWorkflow workflow = (StandardWorkflow) adapter.getChannelList().getChannel(0).getWorkflowList().get(0);
    workflow.getServiceCollection().add(producer);
    start(adapter);
    assertEquals(StartedState.getInstance(), connection.retrieveComponentState());
    LifecycleHelper.stop(adapter);
    assertEquals(StoppedState.getInstance(), connection.retrieveComponentState());
    LifecycleHelper.close(adapter);
    assertEquals(ClosedState.getInstance(), connection.retrieveComponentState());
  }

  public void testConnectionState_SharedConnectionState() throws Exception {
    Adapter adapter = AdapterTest.createAdapter(getName());
    MockConnection connection = new MockConnection();
    connection.setUniqueId(getName());
    adapter.getSharedComponents().addConnection(connection);
    StandaloneProducer producer = new StandaloneProducer(new SharedConnection(getName()), new NullMessageProducer());
    StandardWorkflow workflow = (StandardWorkflow) adapter.getChannelList().getChannel(0).getWorkflowList().get(0);
    workflow.getServiceCollection().add(producer);
    start(adapter);
    assertEquals(StartedState.getInstance(), connection.retrieveComponentState());
    stop(producer);
    assertNotSame(ClosedState.getInstance(), connection.retrieveComponentState());
  }

  public void testIssue6573() throws Exception {
    testConnectionState_SharedConnectionState();
  }



  private Adapter createAdapter() throws CoreException, PasswordException {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("upbeat_liskov");
    JmsConnection jmsConnection = createPtpConnection("jms-connection");

    adapter.getSharedComponents().addConnection(jmsConnection);
    
    ServiceList serviceList = new ServiceList();
    serviceList.setUniqueId("shared-service-list");
    serviceList.add(new LogMessageService("log-message-service"));
    
    adapter.getSharedComponents().addServiceCollection(serviceList);

    StandardWorkflow wf1 = new StandardWorkflow();
    wf1.setUniqueId("reverent-edison");
    wf1.setConsumer(new FsConsumer(new ConfiguredConsumeDestination("in-directory")));
    wf1.setProducer(new FsProducer(new ConfiguredProduceDestination("out-directory")));
    wf1.getServiceCollection().add(new SharedService("shared-service-list"));
    
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId("pedantic_brown");
    wf.setConsumer(new JmsConsumer(new ConfiguredConsumeDestination("jms:queue:SampleQueue1")));
    wf.setProducer(new NullMessageProducer());
    wf.getServiceCollection().add(new StandaloneProducer(new SharedConnection("jms-connection"),
        new JmsProducer(new ConfiguredProduceDestination("jms:topic:MyTopicName"))));
    Channel channel = new Channel();
    channel.setUniqueId("quirky_shannon");
    channel.setConsumeConnection(new SharedConnection("jms-connection"));
    channel.getWorkflowList().add(wf);
    channel.getWorkflowList().add(wf1);
    adapter.getChannelList().add(channel);
    return adapter;
  }

  private JmsConnection createPtpConnection(String uniqueId) throws PasswordException {
    JmsConnection c = new JmsConnection();
    StandardJndiImplementation jndi = new StandardJndiImplementation();
    jndi.setJndiName("Connection_Factory_To_Lookup");
    KeyValuePairSet kvps = jndi.getJndiParams();
    kvps.addKeyValuePair(new KeyValuePair(Context.SECURITY_PRINCIPAL, "Administrator"));
    kvps.addKeyValuePair(new KeyValuePair(Context.SECURITY_CREDENTIALS, "Administrator"));
    kvps.addKeyValuePair(new KeyValuePair("com.sonicsw.jndi.mfcontext.domain", "Domain1"));
    kvps.addKeyValuePair(new KeyValuePair(Context.INITIAL_CONTEXT_FACTORY, "com.sonicsw.jndi.mfcontext.MFContextFactory"));
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair(Context.PROVIDER_URL, "tcp://localhost:2506"));
    c.setVendorImplementation(jndi);
    if (!isEmpty(uniqueId)) {
      c.setUniqueId(uniqueId);
    }
    return c;
  }



}
