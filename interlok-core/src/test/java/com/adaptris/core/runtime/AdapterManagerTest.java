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
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_RETRY_MONITOR_TYPE;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_MSG_CONFIG_UPDATED;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_MSG_INITIALISED;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_MSG_STARTED;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_TYPE_ADAPTER_CONFIG;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_TYPE_ADAPTER_LIFECYCLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.Channel;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultFailedMessageRetrier;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.JndiContextFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullService;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.RetryMessageErrorHandler;
import com.adaptris.core.RetryMessageErrorHandlerMonitorMBean;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.StandardProcessingExceptionHandler;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.Workflow;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.interceptor.MessageMetricsInterceptor;
import com.adaptris.core.stubs.MockConnection;
import com.adaptris.core.stubs.MockFailingConnection;
import com.adaptris.core.stubs.MockServiceWithConnection;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public class AdapterManagerTest extends ComponentManagerCase {

  private Properties env = new Properties();
  private InitialContext initialContext = null;

  public AdapterManagerTest() {
  }

  @Before
  public void beforeMyTests() throws Exception {
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    initialContext = new InitialContext(env);
  }

  @Test
  public void testWrappedComponentClassName() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager am1 = new AdapterManager(adapter);
    assertEquals(Adapter.class.getCanonicalName(), am1.getWrappedComponentClassname());
  }

  @Test
  public void testEqualityHashCode() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager am1 = new AdapterManager(adapter);
    AdapterManager am2 = new AdapterManager(adapter);
    AdapterManager fm = new FailingAdapterManager(adapter);
    AdapterManager am3 = new AdapterManager(createAdapter("somethingElse", 2, 2));
    assertEquals(am1, am1);
    assertEquals(am1, am2);
    assertNotSame(am1, am3);
    assertFalse(am1.equals(fm));
    assertTrue(fm.equals(am1));
    assertFalse(am1.equals(new Object()));
    assertFalse(am1.equals(null));
    assertEquals(am1.hashCode(), am2.hashCode());
    assertNotSame(am1.hashCode(), am3.hashCode());
  }

  @Test
  public void testGetConfiguration() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager am1 = new AdapterManager(adapter);
    Adapter marshalledAdapter = (Adapter) new XStreamMarshaller().unmarshal(am1.getConfiguration());
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  @Test
  public void testRegisterMBean() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Channel channel = createChannel("c1");
    Workflow workflow = createWorkflow("w1");
    workflow.getInterceptors().add(new MessageMetricsInterceptor());
    channel.getWorkflowList().add(workflow);
    adapter.getChannelList().add(channel);
    AdapterManager adapterManager = new AdapterManager(adapter);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      adapterManager.registerMBean();
      assertTrue(JmxHelper.findMBeanServer().isRegistered(adapterManager.createObjectName()));
      for (BaseComponentMBean baseMBean : adapterManager.getAllDescendants()) {
        assertTrue(JmxHelper.findMBeanServer().isRegistered(baseMBean.createObjectName()));
      }

      adapterManager.unregisterMBean();
      assertFalse(JmxHelper.findMBeanServer().isRegistered(adapterManager.createObjectName()));
      for (BaseComponentMBean baseMBean : adapterManager.getAllDescendants()) {
        assertFalse(JmxHelper.findMBeanServer().isRegistered(baseMBean.createObjectName()));
      }
      register(mBeans);
      assertTrue(JmxHelper.findMBeanServer().isRegistered(adapterManager.createObjectName()));
      for (BaseComponentMBean baseMBean : adapterManager.getAllDescendants()) {
        assertTrue(JmxHelper.findMBeanServer().isRegistered(baseMBean.createObjectName()));
      }
    }
    finally {

    }
  }

  @Test
  public void testProxyEquality() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    AdapterManager realManager = (AdapterManager) mBeans.get(0);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean managerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertEquals(realManager, managerProxy);
      assertFalse(realManager == managerProxy);
    }
    finally {
    }
  }

  @Test
  public void testGetState() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertEquals(ClosedState.getInstance(), manager.getComponentState());
    }
    finally {
    }
  }

  @Test
  public void testInitialise() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      manager.requestInit();
      assertEquals(InitialisedState.getInstance(), manager.getComponentState());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testInitialise_WithTimeout() throws Exception {
    TimeInterval standardTimeout = new TimeInterval(2L, TimeUnit.SECONDS);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockConnection conn =
        new MockConnection(getName(), new TimeInterval(250L, TimeUnit.MILLISECONDS).toMilliseconds());
    adapter.getSharedComponents().addConnection(conn);
    AdapterManager adapterManager = new AdapterManager(adapter);

    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      try {
        manager.requestInit(-1);
        fail();
      }
      catch (IllegalArgumentException expected) {

      }
      manager.requestInit(standardTimeout.toMilliseconds());
      assertEquals(InitialisedState.getInstance(), manager.getComponentState());
      adapterManager.requestClose();
      try {
        manager.requestInit(new TimeInterval(100L, TimeUnit.MILLISECONDS).toMilliseconds());
        fail();
      }
      catch (TimeoutException expected) {

      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testInitialise_WithTimeout_InitFailure() throws Exception {
    TimeInterval standardTimeout = new TimeInterval(2L, TimeUnit.SECONDS);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockFailingConnection conn = new MockFailingConnection(getName(), "Init");
    conn.setConnectionAttempts(3);
    conn.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    adapter.getSharedComponents().addConnection(conn);
    AdapterManager adapterManager = new AdapterManager(adapter);

    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      try {
        manager.requestInit(standardTimeout.toMilliseconds());
        fail();
      }
      catch (CoreException expected) {

      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testStart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      manager.requestStart();
      assertEquals(StartedState.getInstance(), manager.getComponentState());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testStart_WithTimeout() throws Exception {
    TimeInterval standardTimeout = new TimeInterval(3L, TimeUnit.SECONDS);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockConnection conn =
        new MockConnection(getName(), new TimeInterval(100L, TimeUnit.MILLISECONDS).toMilliseconds());
    adapter.getSharedComponents().addConnection(conn);
    AdapterManager adapterManager = new AdapterManager(adapter);

    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      
      manager.requestStart(standardTimeout.toMilliseconds());
      assertEquals(StartedState.getInstance(), manager.getComponentState());
      adapterManager.requestClose();
      try {
        manager.requestStart(new TimeInterval(100L, TimeUnit.MILLISECONDS).toMilliseconds());
        fail();
      }
      catch (TimeoutException expected) {

      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testStart_WithTimeout_StartFailure() throws Exception {
    TimeInterval standardTimeout = new TimeInterval(3L, TimeUnit.SECONDS);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockFailingConnection conn = new MockFailingConnection(getName(), "Start");
    conn.setConnectionAttempts(3);
    conn.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    adapter.getSharedComponents().addConnection(conn);
    AdapterManager adapterManager = new AdapterManager(adapter);

    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      try {
        manager.requestStart(standardTimeout.toMilliseconds());
        fail();
      }
      catch (CoreException expected) {
      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testStop() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      manager.requestStart();
      manager.requestStop();
      assertEquals(StoppedState.getInstance(), manager.getComponentState());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testStop_WithTimeout() throws Exception {
    TimeInterval standardTimeout = new TimeInterval(2L, TimeUnit.SECONDS);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockConnection conn =
        new MockConnection(getName(), new TimeInterval(250L, TimeUnit.MILLISECONDS).toMilliseconds());
    adapter.getSharedComponents().addConnection(conn);
    AdapterManager adapterManager = new AdapterManager(adapter);

    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManager.requestStart();
      manager.requestStop(standardTimeout.toMilliseconds());
      assertEquals(StoppedState.getInstance(), manager.getComponentState());
      adapterManager.requestStart();
      assertEquals(StartedState.getInstance(), manager.getComponentState());
      try {
        manager.requestStop(new TimeInterval(100L, TimeUnit.MILLISECONDS).toMilliseconds());
        fail();
      }
      catch (TimeoutException expected) {

      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testForceClose_ErrorOnInit_RequestInit() throws Exception {
    final TimeInterval waitTime = new TimeInterval(5L, TimeUnit.SECONDS);

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockFailingConnection conn = new MockFailingConnection(getName(), "Init");
    conn.setConnectionAttempts(-1);
    conn.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));

    adapter.getSharedComponents().addConnection(conn);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      final AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      // Create a cyclic barrier to wait for init operation to finish
      final CyclicBarrier gate = new CyclicBarrier(2);
      MyExceptionHandler exceptionHandler = new MyExceptionHandler();
      Thread initThread = new ManagedThreadFactory().newThread(new Runnable() {
        @Override
        public void run() {
          try {
            manager.requestInit();
          }
          catch (CoreException e) {
            throw new RuntimeException(e);
          }
          // This code is likely to never trigger, because of the throw above...
          try {
            gate.await(waitTime.toMilliseconds(), TimeUnit.MILLISECONDS);
          }
          catch (Exception gateException) {
          }
        }
      });
      initThread.setUncaughtExceptionHandler(exceptionHandler);
      initThread.start();
      try {
        gate.await(waitTime.toMilliseconds(), TimeUnit.MILLISECONDS);
        fail("Adapter init success, when not expected");
      }
      catch (Exception gateException) {
        // Expected now force close it, because it took too long.
        manager.forceClose();
      }
      assertEquals(ClosedState.getInstance(), manager.getComponentState());
      assertEquals(0, conn.getInitCount());
      assertEquals(0, conn.getCloseCount());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testForceClose_ErrorOnInit_RequestStart() throws Exception {
    final TimeInterval waitTime = new TimeInterval(5L, TimeUnit.SECONDS);

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockFailingConnection conn = new MockFailingConnection(getName(), "Init");
    conn.setConnectionAttempts(-1);
    conn.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));

    adapter.getSharedComponents().addConnection(conn);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      final AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      // Create a cyclic barrier to wait for init/start operation to finish
      final CyclicBarrier gate = new CyclicBarrier(3);
      MyExceptionHandler exceptionHandler = new MyExceptionHandler();
      ManagedThreadFactory threadFactory = new ManagedThreadFactory();
      Thread initThread = threadFactory.newThread(new Runnable() {
        @Override
        public void run() {
          try {
            manager.requestInit();
          }
          catch (CoreException e) {
            throw new RuntimeException(e);
          }
          // This code is likely to never trigger, because of the throw above...
          try {
            gate.await(waitTime.toMilliseconds(), TimeUnit.MILLISECONDS);
          }
          catch (Exception gateException) {
          }
        }
      });
      initThread.setUncaughtExceptionHandler(exceptionHandler);
      initThread.start();
      Thread startThread = threadFactory.newThread(new Runnable() {
        @Override
        public void run() {
          try {
            manager.requestStart();
          }
          catch (CoreException e) {
            throw new RuntimeException(e);
          }
          // This code is likely to never trigger, because of the throw above...
          try {
            gate.await(waitTime.toMilliseconds(), TimeUnit.MILLISECONDS);
          }
          catch (Exception gateException) {
          }
        }
      });
      startThread.setUncaughtExceptionHandler(exceptionHandler);
      startThread.start();
      try {
        gate.await(waitTime.toMilliseconds(), TimeUnit.MILLISECONDS);
        fail("Adapter init success, when not expected");
      }
      catch (Exception gateException) {
        // Expected now force close it, because it took too long.
        manager.forceClose();
      }
      assertEquals(ClosedState.getInstance(), manager.getComponentState());
      assertEquals(0, conn.getInitCount());
      assertEquals(0, conn.getCloseCount());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testForceClose_ErrorOnStart_RequestStart() throws Exception {
    final TimeInterval waitTime = new TimeInterval(5L, TimeUnit.SECONDS);

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockFailingConnection conn = new MockFailingConnection(getName(), "Start");
    conn.setConnectionAttempts(-1);
    conn.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));

    adapter.getSharedComponents().addConnection(conn);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      final AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      // Create a cyclic barrier to wait for init operation to finish
      final CyclicBarrier gate = new CyclicBarrier(2);
      MyExceptionHandler exceptionHandler = new MyExceptionHandler();
      Thread startThread = new ManagedThreadFactory().newThread(new Runnable() {
        @Override
        public void run() {
          try {
            manager.requestStart();
          }
          catch (CoreException e) {
            throw new RuntimeException(e);
          }
          // This code is likely to never trigger, because of the throw above...
          try {
            gate.await(waitTime.toMilliseconds(), TimeUnit.MILLISECONDS);
          }
          catch (Exception gateException) {
          }
        }
      });
      startThread.setUncaughtExceptionHandler(exceptionHandler);
      startThread.start();
      try {
        gate.await(waitTime.toMilliseconds(), TimeUnit.MILLISECONDS);
        fail("Adapter Start success, when not expected");
      }
      catch (Exception gateException) {
        // Expected now force close it, because it took too long.
        manager.forceClose();
      }
      assertEquals(ClosedState.getInstance(), manager.getComponentState());
      assertEquals(1, conn.getInitCount());
      assertEquals(0, conn.getStartCount());
      assertEquals(1, conn.getCloseCount());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testClose() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      manager.requestStart();
      manager.requestClose();
      assertEquals(ClosedState.getInstance(), manager.getComponentState());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testClose_WithTimeout() throws Exception {
    TimeInterval standardTimeout = new TimeInterval(2L, TimeUnit.SECONDS);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockConnection conn =
        new MockConnection(getName(), new TimeInterval(100L, TimeUnit.MILLISECONDS).toMilliseconds());
    adapter.getSharedComponents().addConnection(conn);
    AdapterManager adapterManager = new AdapterManager(adapter);

    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManager.requestStart();
      manager.requestClose(standardTimeout.toMilliseconds());
      assertEquals(ClosedState.getInstance(), manager.getComponentState());
      adapterManager.requestStart();
      assertEquals(StartedState.getInstance(), manager.getComponentState());
      try {
        manager.requestClose(new TimeInterval(100L, TimeUnit.MILLISECONDS).toMilliseconds());
        fail();
      }
      catch (TimeoutException expected) {

      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testRestart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      manager.requestRestart();
      assertEquals(StartedState.getInstance(), manager.getComponentState());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testRestart_WithTimeout() throws Exception {
    TimeInterval standardTimeout = new TimeInterval(3L, TimeUnit.SECONDS);
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    MockConnection conn =
        new MockConnection(getName(), new TimeInterval(100L, TimeUnit.MILLISECONDS).toMilliseconds());
    adapter.getSharedComponents().addConnection(conn);
    AdapterManager adapterManager = new AdapterManager(adapter);

    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManager.requestStart();
      log.trace(getName() + ": Requesting Restart with Timeout=" + standardTimeout.toMilliseconds());
      manager.requestRestart(standardTimeout.toMilliseconds());
      log.trace(getName() + ": Restarted");
      assertEquals(StartedState.getInstance(), manager.getComponentState());
      try {
        log.trace(getName() + ": Requesting Restart with TimeInterval = 100");
        manager.requestRestart(new TimeInterval(100L, TimeUnit.MILLISECONDS).toMilliseconds());
        fail();
      }
      catch (TimeoutException expected) {
        log.trace(getName() + ": TimeoutException (as expected)");
      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testLastStopTime() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      long t1 = manager.requestStopTime();
      assertTrue(t1 > 0);
      manager.requestStart();
      manager.requestStop();
      assertTrue(t1 <= manager.requestStopTime());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testLastStartTime() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertEquals(0, manager.requestStartTime());
      manager.requestStart();
      assertTrue(manager.requestStartTime() > 0);
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testGetChildren() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    // Add a Channel with no ID... which should mean we have a channel that is un-managed.
    adapter.getChannelList().add(new Channel());
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      assertEquals(2, adapterManager.getChildren().size());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAddChild() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);

    assertEquals(2, adapterManager.getChildren().size());
    try {
      adapterManager.addChild(child1);
    }
    catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().startsWith("duplicate Channel ID"));
    }
    assertEquals(2, adapterManager.getChildren().size());
    try {
      adapterManager.addChild(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(2, adapterManager.getChildren().size());
    assertEquals(2, adapter.getChannelList().size());
    Adapter marshalledAdapter = (Adapter) new XStreamMarshaller().unmarshal(adapterManager.getConfiguration());
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  @Test
  public void testRemoveChild() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 0, 0);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);

    assertEquals(2, adapterManager.getChildren().size());

    assertTrue(adapterManager.removeChild(child1));
    assertEquals(1, adapterManager.getChildren().size());
    assertFalse(adapterManager.getChildren().contains(child1.createObjectName()));
    assertEquals(1, adapter.getChannelList().size());

    try {
      adapterManager.removeChild(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testAddChildren() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 0, 0);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    StubChannelManager child1 = new StubChannelManager();
    StubChannelManager child2 = new StubChannelManager();
    List<ChannelRuntimeManager> childCollection = new ArrayList<ChannelRuntimeManager>(Arrays.asList(new StubChannelManager[]
    {
        child1, child2
    }));
    try {
      adapterManager.addChildren(childCollection);
      fail();
    }
    catch (UnsupportedOperationException expected) {

    }
    // String adapterName = this.getClass().getSimpleName() + "." + getName();
    // Adapter adapter = createAdapter(adapterName, 2, 2);
    // AdapterManager adapterManager = new AdapterManager(adapter);
    //
    // ChannelManager child1 = new ChannelManager(adapter.getChannelList().get(0), adapterManager);
    // ChannelManager child2 = new ChannelManager(adapter.getChannelList().get(1), adapterManager);
    // List<ChannelManagerMBean> childCollection = new ArrayList<ChannelManagerMBean>(Arrays.asList(new ChannelManager[]
    // {
    // child1, child2
    // }));
    // assertEquals(2, adapterManager.getChildren().size());
    // assertFalse(adapterManager.addChildren(childCollection));
    // assertEquals(2, adapterManager.getChildren().size());
  }

  @Test
  public void testRemoveChildren() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    AdapterManager altAdapterManager = new AdapterManager(createAdapter(adapterName));
    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    Channel c3 = createChannel(getName() + "_3");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ChannelManager child3 = new ChannelManager(c3, altAdapterManager);
    List<ChannelRuntimeManager> childCollection = new ArrayList<ChannelRuntimeManager>(Arrays.asList(new ChannelManager[]
    {
        child1, child2, child3
    }));
    assertEquals(2, adapterManager.getChildren().size());
    assertTrue(adapterManager.removeChildren(childCollection));
    assertFalse(adapterManager.removeChildren(childCollection));
    assertEquals(0, adapterManager.getChildren().size());
    assertEquals(0, adapter.getChannelList().size());
  }

  @Test
  public void testAddChildRuntimeComponent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.setMessageErrorDigester(new NullMessageErrorDigester());
    AdapterManager adapterManager = new AdapterManager(adapter);
    AdapterChild child = new AdapterChild(adapterManager);
    AdapterChild child2 = new AdapterChild(adapterManager);
    assertTrue(adapterManager.addChildJmxComponent(child));
    assertFalse(adapterManager.addChildJmxComponent(child));
    try {
      adapterManager.addChildJmxComponent(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    // the child that was added, and the component Checker.
    assertEquals(2, adapterManager.getChildRuntimeInfoComponents().size());
  }

  @Test
  public void testRemoveChildRuntimeComponent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.setMessageErrorDigester(new NullMessageErrorDigester());
    AdapterManager adapterManager = new AdapterManager(adapter);
    AdapterChild child = new AdapterChild(adapterManager);
    AdapterChild child2 = new AdapterChild(adapterManager);
    assertTrue(adapterManager.addChildJmxComponent(child));
    try {
      adapterManager.removeChildJmxComponent(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertTrue(adapterManager.removeChildJmxComponent(child));
    assertFalse(adapterManager.removeChildJmxComponent(child2));
    // Just the componentChecker left.
    assertEquals(1, adapterManager.getChildRuntimeInfoComponents().size());
  }

  @Test
  public void testMBean_ContainsSharedConnection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertTrue(amp.containsSharedConnection(getName()));
    }
    finally {
    }
  }

  @Test
  public void testMBean_GetConnectionIds() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      Collection<String> result = amp.getSharedConnectionIds();
      assertEquals(1, result.size());
      assertTrue(result.contains(getName()));
    }
    finally {
    }
  }

  @Test
  public void testMBean_AddSharedConnection() throws Exception {    
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      amp.addSharedConnection(m.marshal(new NullConnection(getName())));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(1, marshalledAdapter.getSharedComponents().getConnections().size());
      assertEquals(getName(), marshalledAdapter.getSharedComponents().getConnections().get(0).getUniqueId());
    }
    finally {
    }
  }

  @Test
  public void testMBean_AddSharedConnection_AddChannel_StartAdapter() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    Channel newChannel = createChannel(getName(), 1);
    newChannel.setConsumeConnection(new SharedConnection(getName()));
    newChannel.setProduceConnection(new SharedConnection(getName()));
    StandardWorkflow wf = (StandardWorkflow) newChannel.getWorkflowList().getWorkflows().get(0);
    MockServiceWithConnection service = new MockServiceWithConnection(new SharedConnection(getName()));
    wf.getServiceCollection().add(service);

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      amp.addSharedConnection(m.marshal(new NullConnection(getName())));
      ObjectName channelObj = amp.addChannel(m.marshal(newChannel));
      ChannelManagerMBean cmb = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);

      adapterManager.requestStart();

      assertEquals(StartedState.getInstance(), cmb.getComponentState());
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_AddSharedConnection_NoUniqueID() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      try {
        amp.addSharedConnection(m.marshal(new NullConnection()));
        fail();
      }
      catch (IllegalArgumentException e) {

      }
    }
    finally {
    }
  }

  @Test
  public void testMBean_AddAndBindSharedConnection_Duplicate() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      adapterManager.requestStart();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertTrue(amp.addAndBindSharedConnection(m.marshal(new NullConnection(getName()))));
      assertFalse(amp.addAndBindSharedConnection(m.marshal(new NullConnection(getName()))));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(1, marshalledAdapter.getSharedComponents().getConnections().size());
      assertEquals(getName(), marshalledAdapter.getSharedComponents().getConnections().get(0).getUniqueId());
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_AddSharedConnection_Duplicate() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertTrue(amp.addSharedConnection(m.marshal(new NullConnection(getName()))));
      assertFalse(amp.addSharedConnection(m.marshal(new NullConnection(getName()))));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(1, marshalledAdapter.getSharedComponents().getConnections().size());
      assertEquals(getName(), marshalledAdapter.getSharedComponents().getConnections().get(0).getUniqueId());
    }
    finally {
    }
  }

  @Test
  public void testMBean_SharedConnection_GarbageCollectedReferences() throws Exception {
    // Tests the behaviour with WeakHashMap inside AdaptrisConnectionImp
    // We use the AdapterManager to test this, because we marshal the channel to XML to add it
    // to the channel so we aren't actually holding a reference to it for the test.
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    Channel newChannel = createChannel(getName(), 1);
    newChannel.setConsumeConnection(new SharedConnection(getName()));
    newChannel.setProduceConnection(new SharedConnection(getName()));
    StandardWorkflow wf = (StandardWorkflow) newChannel.getWorkflowList().getWorkflows().get(0);
    MockServiceWithConnection service = new MockServiceWithConnection(new SharedConnection(getName()));
    wf.getServiceCollection().add(service);

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);

      // Add the shared connection and the channel.
      adapterManager.addSharedConnection(m.marshal(new NullConnection(getName())));
      adapterManager.addChannel(m.marshal(newChannel));

      adapterManager.requestStart();
      adapterManager.requestClose();
      NullConnection beforeRemove = (NullConnection) adapterManager.getWrappedComponent().getSharedComponents().getConnections()
          .get(0);
      assertEquals(1, beforeRemove.retrieveMessageConsumers().size());
      assertEquals(1, beforeRemove.retrieveMessageProducers().size());
      // one for the channel, and one for the service.
      assertEquals(2, beforeRemove.retrieveExceptionListeners().size());

      adapterManager.removeChannel(getName());

      // Suggest a garbage collection which *should* remove the references for the connection.
      System.gc();
      Thread.sleep(1000);

      adapterManager.requestStart();
      NullConnection afterRemove = (NullConnection) adapterManager.getWrappedComponent().getSharedComponents().getConnections()
          .get(0);
      assertEquals(0, afterRemove.retrieveMessageConsumers().size());
      assertEquals(0, afterRemove.retrieveMessageProducers().size());
      assertEquals(0, afterRemove.retrieveExceptionListeners().size());
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_HasRetryMessageErrorHandler() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.setMessageErrorHandler(new RetryMessageErrorHandler(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      adapterManager.requestStart();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ObjectName handlerObjectName = ObjectName
          .getInstance(
              JMX_RETRY_MONITOR_TYPE + adapterManager.createObjectHierarchyString() + ID_PREFIX + getName());
      assertTrue(amp.getChildRuntimeInfoComponents().contains(handlerObjectName));
      RetryMessageErrorHandlerMonitorMBean monitor = JMX.newMBeanProxy(mBeanServer, handlerObjectName,
          RetryMessageErrorHandlerMonitorMBean.class);
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_AddAndBindSharedConnection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      adapterManager.requestStart();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      amp.addAndBindSharedConnection(m.marshal(new NullConnection(getName())));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(1, marshalledAdapter.getSharedComponents().getConnections().size());
      assertEquals(getName(), marshalledAdapter.getSharedComponents().getConnections().get(0).getUniqueId());
      assertTrue(amp.getSharedConnectionIds().contains(getName()));
      assertTrue(amp.containsSharedConnection(getName()));
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_AddAndBindSharedConnection_AddChannel_StartChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    Channel newChannel = createChannel(getName(), 1);
    StandardWorkflow wf = (StandardWorkflow) newChannel.getWorkflowList().getWorkflows().get(0);
    MockServiceWithConnection service = new MockServiceWithConnection(new SharedConnection(getName()));
    wf.getServiceCollection().add(service);

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      adapterManager.requestStart();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      amp.addAndBindSharedConnection(m.marshal(new NullConnection(getName())));
      ObjectName channelObj = amp.addChannel(m.marshal(newChannel));
      ChannelManagerMBean cmb = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);

      assertEquals(ClosedState.getInstance(), cmb.getComponentState());
      // This should start, referencing the shared connection in JNDI.
      cmb.requestStart();
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_Notification_AddAndBindSharedConnection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_ADAPTER_CONFIG);

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      adapterManager.requestStart();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      mBeanServer.addNotificationListener(adapterObj, listener, filter, null);
      amp.addAndBindSharedConnection(m.marshal(new NullConnection(getName())));
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(amp.getConfiguration(), n.getUserData());
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_AddAndBindSharedConnection_IllegalState() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      try {
        amp.addAndBindSharedConnection(m.marshal(new NullConnection(getName())));
        fail();
      }
      catch (IllegalStateException expected) {

      }
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_NotificationOnAddSharedConnection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_ADAPTER_CONFIG);

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      mBeanServer.addNotificationListener(adapterObj, listener, filter, null);
      amp.addSharedConnection(m.marshal(new NullConnection(getName())));
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(amp.getConfiguration(), n.getUserData());
    }
    finally {
    }
  }

  @Test
  public void testMBean_RemoveSharedConnection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      amp.removeSharedConnection(getName());
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(0, marshalledAdapter.getSharedComponents().getConnections().size());
    }
    finally {
    }
  }

  @Test
  public void testMBean_RemoveSharedConnection_NotFound() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertFalse(amp.removeSharedConnection(getName() + "_1"));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(1, marshalledAdapter.getSharedComponents().getConnections().size());
      assertEquals(getName(), marshalledAdapter.getSharedComponents().getConnections().get(0).getUniqueId());
    }
    finally {
    }
  }

  @Test
  public void testMBean_NotificationOnRemoveSharedConnection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_ADAPTER_CONFIG);

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      mBeanServer.addNotificationListener(adapterObj, listener, filter, null);
      assertTrue(amp.removeSharedConnection(getName()));
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(amp.getConfiguration(), n.getUserData());
    }
    finally {
    }
  }

  @Test
  public void testMBean_AddChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = createChannel(getName() + "_1");
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ObjectName channelObj = adapterManagerProxy.addChannel(DefaultMarshaller.getDefaultMarshaller().marshal(newChannel));
      assertNotNull(channelObj);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      assertEquals(1, marshalledAdapter.getChannelList().size());
    }
    finally {
    }
  }

  @Test
  public void testMBean_AddChannel_WhileStarted() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = createChannel(getName() + "_1");
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      adapterManager.requestStart();
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ObjectName cmObj = adapterManagerProxy.addChannel(DefaultMarshaller.getDefaultMarshaller().marshal(newChannel));
      ChannelManagerMBean newCM = JMX.newMBeanProxy(mBeanServer, cmObj, ChannelManagerMBean.class);
      assertEquals(ClosedState.getInstance(), newCM.getComponentState());
      newCM.requestStart();
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_AddChannel_NoUniqueID() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = new Channel();
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      try {
        adapterManagerProxy.addChannel(DefaultMarshaller.getDefaultMarshaller().marshal(newChannel));
        fail();
      }
      catch (CoreException expected) {

      }
    }
    finally {
    }
  }

  @Test
  public void testMBean_SetMessageErrorHandler() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler();
    meh.setUniqueId(getName());
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManagerProxy.setMessageErrorHandler(DefaultMarshaller.getDefaultMarshaller().marshal(meh));
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      assertEquals(StandardProcessingExceptionHandler.class, marshalledAdapter.getMessageErrorHandler().getClass());
      assertEquals(meh.getUniqueId(),
          ((StandardProcessingExceptionHandler) marshalledAdapter.getMessageErrorHandler()).getUniqueId());
    }
    finally {
    }
  }

  @Test
  public void testMBean_SetFailedMessageRetrier() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    DefaultFailedMessageRetrier meh = new DefaultFailedMessageRetrier();
    meh.setUniqueId(getName());
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManagerProxy.setFailedMessageRetrier(DefaultMarshaller.getDefaultMarshaller().marshal(meh));
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      assertEquals(DefaultFailedMessageRetrier.class, marshalledAdapter.getFailedMessageRetrier().getClass());
      assertEquals(meh.getUniqueId(), ((DefaultFailedMessageRetrier) marshalledAdapter.getFailedMessageRetrier()).getUniqueId());
    }
    finally {
    }
  }

  @Test
  public void testMBean_removeChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel1 = createChannel(getName() + "_1");
    Channel newChannel2 = createChannel(getName() + "_2");
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ObjectName channelObj1 = adapterManagerProxy.addChannel(DefaultMarshaller.getDefaultMarshaller().marshal(newChannel1));
      ObjectName channelObj2 = adapterManagerProxy.addChannel(DefaultMarshaller.getDefaultMarshaller().marshal(newChannel2));
      assertTrue(adapterManagerProxy.removeChannel(newChannel1.getUniqueId()));
      assertFalse(JmxHelper.findMBeanServer().isRegistered(channelObj1));
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      // Should still be 1, there's 2 channels.
      assertEquals(1, marshalledAdapter.getChannelList().size());

    }
    finally {
    }
  }

  @Test
  public void testMBean_removeChannel_WhileStarted() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel1 = createChannel(getName() + "_1");
    Channel newChannel2 = createChannel(getName() + "_2");
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ObjectName channelObj1 = adapterManagerProxy.addChannel(DefaultMarshaller.getDefaultMarshaller().marshal(newChannel1));
      ObjectName channelObj2 = adapterManagerProxy.addChannel(DefaultMarshaller.getDefaultMarshaller().marshal(newChannel2));
      adapterManager.requestStart();
      assertTrue(adapterManagerProxy.removeChannel(newChannel1.getUniqueId()));
      assertFalse(JmxHelper.findMBeanServer().isRegistered(channelObj1));
      assertTrue(JmxHelper.findMBeanServer().isRegistered(channelObj2));
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      // Should still be 1, there's 2 channels.
      assertEquals(1, marshalledAdapter.getChannelList().size());

      ChannelManagerMBean cmb = JMX.newMBeanProxy(mBeanServer, channelObj2, ChannelManagerMBean.class);
      assertEquals(StartedState.getInstance(), cmb.getComponentState());
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_removeChannelAddChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Channel newChannel1 = createChannel(getName() + "_1");
    adapter.getChannelList().add(newChannel1);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertTrue(adapterManagerProxy.removeChannel(newChannel1.getUniqueId()));
      assertEquals(0, adapterManagerProxy.getChildren().size());

      ObjectName channelObj1 = adapterManagerProxy.addChannel(DefaultMarshaller.getDefaultMarshaller().marshal(newChannel1));
      assertTrue(JmxHelper.findMBeanServer().isRegistered(channelObj1));
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      assertEquals(1, marshalledAdapter.getChannelList().size());

    }
    finally {
    }
  }

  @Test
  public void testMBean_removeChannel_NotFound() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertFalse(adapterManagerProxy.removeChannel(null));
      assertFalse(adapterManagerProxy.removeChannel(getName()));
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      assertEquals(0, marshalledAdapter.getChannelList().size());
    }
    finally {
    }

  }

  @Test
  public void testStart_WithErrorDigester() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    adapter.setMessageErrorDigester(new StandardMessageErrorDigester(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      manager.requestStart();
      assertEquals(StartedState.getInstance(), manager.getComponentState());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_getAdapterBuildVersion() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Channel newChannel1 = createChannel(getName() + "_1");
    adapter.getChannelList().add(newChannel1);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertNotNull(adapterManagerProxy.getAdapterBuildVersion());
    }
    finally {
    }
  }

  @Test
  public void testMBean_getAdapterModuleVersion() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Channel newChannel1 = createChannel(getName() + "_1");
    adapter.getChannelList().add(newChannel1);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertNotNull(adapterManagerProxy.getModuleVersions());
      // Should be 3 in the versions
      // The version for adp-core-apt.jar and adp-core.jar and interlok-common.
      assertEquals(3, adapterManagerProxy.getModuleVersions().size());
    }
    finally {
    }
  }

  @Test
  public void testMBean_getArtifactIdentifiers() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Channel newChannel1 = createChannel(getName() + "_1");
    adapter.getChannelList().add(newChannel1);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertNotNull(adapterManagerProxy.getArtifactIdentifiers());
      // Should be 3 in the versions
      // The version for adp-core-apt.jar and adp-core.jar and interlok-common.
      assertEquals(3, adapterManagerProxy.getArtifactIdentifiers().size());
    } finally {
    }
  }

  @Test
  public void testMBean_NotificationOnInit() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    SimpleNotificationListener listener = new SimpleNotificationListener();
    ObjectName adapterObj = createAdapterObjectName(adapterName);
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      mBeanServer.addNotificationListener(adapterObj, listener, null, null);

      manager.requestInit();
      assertEquals(InitialisedState.getInstance(), manager.getComponentState());
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_INITIALISED, n.getMessage());
      assertEquals(InitialisedState.getInstance(), n.getUserData());
    }
    finally {
      mBeanServer.removeNotificationListener(adapterObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_NotificationOnStart() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    SimpleNotificationListener listener = new SimpleNotificationListener();
    ObjectName adapterObj = createAdapterObjectName(adapterName);
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      mBeanServer.addNotificationListener(adapterObj, listener, null, null);

      manager.requestStart();
      assertEquals(StartedState.getInstance(), manager.getComponentState());
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_STARTED, n.getMessage());
      assertEquals(StartedState.getInstance(), n.getUserData());
    }
    finally {
      mBeanServer.removeNotificationListener(adapterObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_NotificationOnStop() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    SimpleNotificationListener listener = new SimpleNotificationListener();
    ObjectName adapterObj = createAdapterObjectName(adapterName);
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      mBeanServer.addNotificationListener(adapterObj, listener, null, null);

      manager.requestStart();
      manager.requestStop();
      assertEquals(StoppedState.getInstance(), manager.getComponentState());
      listener.waitForMessages(2);
      // Timing issues under github actions / jenkins / vm
      // assertEquals(2, listener.getNotifications().size());
      //
      // // Get the last notification by sorting it.
      // Notification n = listener.notificationsSortedBySeqNo().get(1);
      // assertEquals(NOTIF_TYPE_ADAPTER_LIFECYCLE, n.getType());
      // assertEquals(NOTIF_MSG_STOPPED, n.getMessage());
      // assertEquals(StoppedState.getInstance(), n.getUserData());
    }
    finally {
      mBeanServer.removeNotificationListener(adapterObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_NotificationOnClose() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    SimpleNotificationListener listener = new SimpleNotificationListener();
    ObjectName adapterObj = createAdapterObjectName(adapterName);
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      mBeanServer.addNotificationListener(adapterObj, listener, null, null);

      manager.requestStart();
      manager.requestClose();
      assertEquals(ClosedState.getInstance(), manager.getComponentState());
      listener.waitForMessages(2);
      // Timing issues under gradle
      // assertEquals(2, listener.getNotifications().size());
      // Notification n = listener.notificationsSortedBySeqNo().get(1);
      // assertEquals(NOTIF_TYPE_ADAPTER_LIFECYCLE, n.getType());
      // assertEquals(NOTIF_MSG_CLOSED, n.getMessage());
      // assertEquals(ClosedState.getInstance(), n.getUserData());
    }
    finally {
      mBeanServer.removeNotificationListener(adapterObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_NotificationOnRestart() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    AdapterManager adapterManager = new AdapterManager(adapter);
    SimpleNotificationListener listener = new SimpleNotificationListener();
    ObjectName adapterObj = createAdapterObjectName(adapterName);
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);

      mBeanServer.addNotificationListener(adapterObj, listener, null, null);

      manager.requestStart();
      manager.requestRestart();
      assertEquals(StartedState.getInstance(), manager.getComponentState());
      // This will actually send 3 notifications 1 start, 1 close, 1 start, and a final "restart"
      listener.waitForMessages(4);
      // Timing issue waiting for events, event 4 is just an Adapter-started event on jenkins but
      // isn't when
      // tested locally
      // assertEquals(4, listener.getNotifications().size());
      // Notification n = listener.getNotifications().get(3);
      // assertEquals(NOTIF_TYPE_ADAPTER_LIFECYCLE, n.getType());
      // assertEquals(NOTIF_MSG_RESTARTED, n.getMessage());
      // assertEquals(StartedState.getInstance(), n.getUserData());
    }
    finally {
      mBeanServer.removeNotificationListener(adapterObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_NotificationOnAddChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 1, 1);
    String newChannelXml = DefaultMarshaller.getDefaultMarshaller().marshal(createChannel(getName() + "_1"));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_ADAPTER_CONFIG);
    try {
      adapterManager.registerMBean();
      mBeanServer.addNotificationListener(adapterObj, listener, filter, null);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManagerProxy.addChannel(newChannelXml);
      listener.waitForMessages(1);
      // for (Notification n : listener.getNotifications()) {
      // System.err.printf("Notifcation Details-----\n");
      // System.err.printf("Source    : %s\n", n.getSource());
      // System.err.printf("Timestamp : %tT\n", new Date(n.getTimeStamp()));
      // System.err.printf("Type      : %s\n", n.getType());
      // System.err.printf("Message   : %s\n", n.getMessage());
      // }
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(adapterManagerProxy.getConfiguration(), n.getUserData());
    }
    finally {
    }
  }

  @Test
  public void testMBean_NotificationOnAddWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Channel newChannel = createChannel(getName() + "_1");
    String workflowXml = DefaultMarshaller.getDefaultMarshaller().marshal(new PoolingWorkflow(getName() + "_wf1"));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    ChannelManager channelManager = new ChannelManager(newChannel, adapterManager);
    ObjectName channelManagerObj = channelManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_ADAPTER_CONFIG);
    try {
      adapterManager.registerMBean();
      mBeanServer.addNotificationListener(adapterObj, listener, filter, null);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      channelManager.addWorkflow(workflowXml);

      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(adapterManagerProxy.getConfiguration(), n.getUserData());
    }
    finally {
    }
  }

  @Test
  public void testMBean_NotificationOnRemoveChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Channel c = createChannel(getName() + "_1");
    adapter.getChannelList().add(c);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_ADAPTER_CONFIG);
    try {
      adapterManager.registerMBean();
      mBeanServer.addNotificationListener(adapterObj, listener, filter, null);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManagerProxy.removeChannel(c.getUniqueId());
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(adapterManagerProxy.getConfiguration(), n.getUserData());
    }
    finally {
    }
  }

  @Test
  public void testMBean_NotificationOnSetFailedMessageRetrier() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 1, 1);
    String newCompXml = DefaultMarshaller.getDefaultMarshaller().marshal(new DefaultFailedMessageRetrier());
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_ADAPTER_CONFIG);
    try {
      adapterManager.registerMBean();
      mBeanServer.addNotificationListener(adapterObj, listener, filter, null);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManagerProxy.setFailedMessageRetrier(newCompXml);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(adapterManagerProxy.getConfiguration(), n.getUserData());
    }
    finally {
    }
  }

  @Test
  public void testMBean_NotificationOnSetMessageErrorHandler() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 1, 1);
    String newCompXml = DefaultMarshaller.getDefaultMarshaller().marshal(new StandardProcessingExceptionHandler());
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_ADAPTER_CONFIG);
    try {
      adapterManager.registerMBean();
      mBeanServer.addNotificationListener(adapterObj, listener, filter, null);
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      adapterManagerProxy.setMessageErrorHandler(newCompXml);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_ADAPTER_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(adapterManagerProxy.getConfiguration(), n.getUserData());
    }
    finally {
    }
  }

  @Test
  public void testMBean_AddSharedService() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      amp.addSharedService(m.marshal(new NullService(getName())));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(1, marshalledAdapter.getSharedComponents().getServices().size());
      assertEquals(getName(), marshalledAdapter.getSharedComponents().getServices().get(0).getUniqueId());
      assertTrue(marshalledAdapter.getSharedComponents().getServiceIds().contains(getName()));
    }
    finally {
    }
  }

  @Test
  public void testMBean_AddAndBindSharedService() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      adapterManager.requestStart();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      amp.addAndBindSharedService(m.marshal(new NullService(getName())));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(1, marshalledAdapter.getSharedComponents().getServices().size());
      assertEquals(getName(), marshalledAdapter.getSharedComponents().getServices().get(0).getUniqueId());
      assertTrue(amp.getSharedServiceIds().contains(getName()));
      assertTrue(amp.containsSharedService(getName()));
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_RemoveSharedService() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.getSharedComponents().addService(new NullService(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertTrue(amp.removeSharedService(getName()));
      assertFalse(amp.removeSharedService("HelloWorld"));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(0, marshalledAdapter.getSharedComponents().getServices().size());
    }
    finally {
    }
  }

  @Test
  public void testMBean_RemoveSharedComponent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.getSharedComponents().addService(new NullService(NullService.class.getCanonicalName()));
    adapter.getSharedComponents().addConnection(new NullConnection(NullConnection.class.getCanonicalName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName adapterObj = adapterManager.createObjectName();

    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean amp = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertTrue(amp.removeSharedComponent(NullService.class.getCanonicalName()));
      assertFalse(amp.removeSharedComponent("HelloWorld"));
      Adapter marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(0, marshalledAdapter.getSharedComponents().getServices().size());
      assertTrue(amp.removeSharedComponent(NullConnection.class.getCanonicalName()));
      marshalledAdapter = (Adapter) m.unmarshal(amp.getConfiguration());
      assertEquals(0, marshalledAdapter.getSharedComponents().getConnections().size());
    }
    finally {
    }
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  private class FailingAdapterManager extends AdapterManager {

    public FailingAdapterManager(Adapter owner) throws MalformedObjectNameException, CoreException {
      super(owner);
    }

    @Override
    public ObjectName createObjectName() throws MalformedObjectNameException {
      throw new MalformedObjectNameException();
    }
  }

  private class StubChannelManager extends StubBaseComponentMBean implements ChannelManagerMBean, ChannelRuntimeManager {

    @Override
    public void requestInit() throws CoreException {
    }

    @Override
    public void requestStart() throws CoreException {
    }

    @Override
    public void requestStop() throws CoreException {
    }

    @Override
    public void requestClose() throws CoreException {
    }

    @Override
    public void requestRestart() throws CoreException {
    }

    @Override
    public long requestStartTime() {
      return 0;
    }

    @Override
    public long requestStopTime() {
      return 0;
    }

    @Override
    public ComponentState getComponentState() {
      return ClosedState.getInstance();
    }

    @Override
    public String getUniqueId() {
      return null;
    }

    @Override
    public String getConfiguration() throws CoreException {
      return null;
    }

    @Override
    public Channel getWrappedComponent() {
      return null;
    }

    @Override
    public ObjectName createObjectName() throws MalformedObjectNameException {
      return null;
    }

    @Override
    public Collection<ObjectName> getChildren() throws MalformedObjectNameException {
      return null;
    }

    @Override
    public Collection<BaseComponentMBean> getAllDescendants() {
      return null;
    }

    @Override
    public boolean addChild(WorkflowRuntimeManager wmb) throws CoreException {
      return false;
    }

    @Override
    public boolean removeChild(WorkflowRuntimeManager wmb) throws CoreException {
      return false;
    }

    @Override
    public boolean addChildren(Collection<WorkflowRuntimeManager> coll) throws CoreException, UnsupportedOperationException {
      return false;
    }

    @Override
    public boolean removeChildren(Collection<WorkflowRuntimeManager> coll) throws CoreException {
      return false;
    }

    @Override
    public AdapterManager getParent() {
      return null;
    }

    @Override
    public String getParentId() {
      return null;
    }

    @Override
    public ObjectName getParentObjectName() throws MalformedObjectNameException {
      return null;
    }

    @Override
    public String createObjectHierarchyString() {
      return null;
    }

    @Override
    public Collection<ObjectName> getChildRuntimeInfoComponents() throws MalformedObjectNameException {
      return null;
    }

    @Override
    public boolean addChildJmxComponent(ChildRuntimeInfoComponent comp) {
      return false;
    }

    @Override
    public boolean removeChildJmxComponent(ChildRuntimeInfoComponent comp) {
      return false;
    }

    @Override
    public ObjectName addWorkflow(String xmlString) throws CoreException, IllegalStateException, MalformedObjectNameException {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeWorkflow(String id) throws CoreException, IllegalStateException, MalformedObjectNameException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void childUpdated() throws CoreException {
    }

    @Override
    public void requestInit(long timeout) throws CoreException, TimeoutException {
    }

    @Override
    public void requestStart(long timeout) throws CoreException, TimeoutException {
    }

    @Override
    public void requestStop(long timeout) throws CoreException, TimeoutException {
    }

    @Override
    public void requestClose(long timeout) throws CoreException, TimeoutException {
    }

    @Override
    public void requestRestart(long timeout) throws CoreException, TimeoutException {
    }

    @Override
    public String getWrappedComponentClassname() {
      return Channel.class.getCanonicalName();
    }

  }

  private class AdapterChild extends StubBaseComponentMBean implements ChildRuntimeInfoComponent {
    private AdapterManager myParent;
    private String uid = UUID.randomUUID().toString();

    AdapterChild(AdapterManager wm) {
      myParent = wm;
    }

    @Override
    public ObjectName getParentObjectName() throws MalformedObjectNameException {
      return myParent.createObjectName();
    }

    @Override
    public String getParentId() {
      return myParent.getUniqueId();
    }

    @Override
    public ObjectName createObjectName() throws MalformedObjectNameException {
      return ObjectName.getInstance("Dummy:type=" + uid);
    }

    @Override
    public RuntimeInfoComponent getParentRuntimeInfoComponent() {
      return myParent;
    }
  }

  private class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private List<Throwable> exceptionList = Collections.synchronizedList(new ArrayList<Throwable>());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
      exceptionList.add(e);
    }

    public Collection<Throwable> getExceptions() {
      return new ArrayList(exceptionList);
    }
  }
}
