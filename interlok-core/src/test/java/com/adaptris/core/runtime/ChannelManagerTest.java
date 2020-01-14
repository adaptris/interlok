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
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_MSG_STOPPED;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_TYPE_CHANNEL_CONFIG;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_TYPE_CHANNEL_LIFECYCLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.Channel;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.RetryMessageErrorHandler;
import com.adaptris.core.RetryMessageErrorHandlerMonitorMBean;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.Workflow;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.SerializableMessage;

@SuppressWarnings("deprecation")
public class ChannelManagerTest extends ComponentManagerCase {

  public ChannelManagerTest() {
  }

  @Test
  public void testEqualityHashCode() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    AdapterManager am1 = new AdapterManager(createAdapter(adapterName));
    AdapterManager am2 = new AdapterManager(createAdapter(adapterName));
    AdapterManager fam = new AdapterManager(createAdapter(adapterName));
    Channel channel = createChannel("c1");
    Channel channel2 = createChannel("c1");
    ChannelManager cm1 = new ChannelManager(channel, am1);
    ChannelManager cm2 = new ChannelManager(channel2, am2);
    ChannelManager fcm = new FailingChannelManager(channel, fam);
    ChannelManager cm3 = new ChannelManager(createChannel("c3"), am1);
    assertEquals(cm1, cm1);
    assertEquals(cm1, cm2);
    assertNotSame(cm1, cm3);
    assertFalse(cm1.equals(fcm));
    assertFalse(cm1.equals(new Object()));
    assertFalse(cm1.equals(null));
    assertEquals(cm1.hashCode(), cm2.hashCode());
    assertNotSame(cm1.hashCode(), cm3.hashCode());
  }

  @Test
  public void testGetConfiguration() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 0, 0);
    AdapterManager am1 = new AdapterManager(adapter);
    Channel channel = createChannel("c1", 1);
    ChannelManager cm1 = new ChannelManager(channel, am1);
    Channel marshalledCopy = (Channel) new XStreamMarshaller().unmarshal(cm1.getConfiguration());
    assertRoundtripEquality(channel, marshalledCopy);
  }

  @Test
  public void testGetParent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Adapter adapter2 = createAdapter(adapterName);
    Channel channel = createChannel(getName(), 0);
    try {
      AdapterManager adapterManager = new AdapterManager(adapter);
      AdapterManager adapterManager_2 = new AdapterManager(adapter2);
      ChannelManager channelManager = new ChannelManager(channel, adapterManager);
      assertEquals(adapterManager, channelManager.getParent());
      assertTrue(adapterManager == channelManager.getParent());
      assertEquals(adapterManager_2, channelManager.getParent());
      assertFalse(adapterManager_2 == channelManager.getParent());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testGetParentObjectName() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Adapter adapter2 = createAdapter(adapterName);
    Channel channel = createChannel(getName(), 0);
    try {
      AdapterManager adapterManager = new AdapterManager(adapter);
      ChannelManager channelManager = new ChannelManager(channel, adapterManager);
      assertEquals(adapterManager, channelManager.getParent());
      assertTrue(adapterManager == channelManager.getParent());
      assertEquals(adapterManager.createObjectName(), channelManager.getParentObjectName());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testGetParentId() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    Adapter adapter2 = createAdapter(adapterName);
    Channel channel = createChannel(getName(), 0);
    try {
      AdapterManager adapterManager = new AdapterManager(adapter);
      ChannelManager channelManager = new ChannelManager(channel, adapterManager);
      assertEquals(adapterManager, channelManager.getParent());
      assertTrue(adapterManager == channelManager.getParent());
      assertEquals(adapterName, channelManager.getParentId());
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testProxyEquality() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    try {
      adapterManager.registerMBean();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertEquals(child1, channelManagerProxy);
      assertFalse(child1 == channelManagerProxy);
    }
    finally {
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testGetState() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testClose() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestClose();
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
      assertEquals(ClosedState.getInstance(), child1.getComponentState());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testStop() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManagerProxy.requestStop();
      assertEquals(StoppedState.getInstance(), channelManagerProxy.getComponentState());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testStart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();


    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestStop();
      assertEquals(StoppedState.getInstance(), channelManagerProxy.getComponentState());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testInitialise() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestClose();
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
      channelManagerProxy.requestInit();
      assertEquals(InitialisedState.getInstance(), channelManagerProxy.getComponentState());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testRestart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestStop();
      assertEquals(StoppedState.getInstance(), channelManagerProxy.getComponentState());
      channelManagerProxy.requestRestart();
      assertEquals(StartedState.getInstance(), channelManagerProxy.getComponentState());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testLastStartTime() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    try {
      adapterManager.registerMBean();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertEquals(0, channelManagerProxy.requestStartTime());
      adapterManager.requestStart();
      assertEquals(StartedState.getInstance(), channelManagerProxy.getComponentState());
      assertTrue(channelManagerProxy.requestStartTime() > 0);
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testLastStopTime() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      long t1 = channelManagerProxy.requestStopTime();
      assertTrue(t1 > 0);
      adapterManager.requestStart();
      adapterManager.requestStop();
      assertTrue(t1 <= channelManagerProxy.requestStopTime());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testGetChildren() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1", 2);
    // Add a WF with no ID... which should mean we have a workflow that is unmanaged.
    c1.getWorkflowList().add(new StandardWorkflow());
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    try {
      assertEquals(2, child1.getChildren().size());
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
    Channel channel = createChannel(getName());
    Workflow workflow1 = createWorkflow(getName() + "_1");
    Workflow workflow2 = createWorkflow(getName() + "_2");

    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    WorkflowManager child1 = new WorkflowManager(workflow1, channelManager);
    WorkflowManager child2 = new WorkflowManager(workflow2, channelManager);

    assertEquals(2, channel.getWorkflowList().size());
    assertEquals(2, channelManager.getChildren().size());

    try {
      channelManager.addChild(child1);
      fail();
    }
    catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().startsWith("duplicate Workflow ID"));
    }
    assertEquals(2, channelManager.getChildren().size());
    try {
      channelManager.addChild(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(2, channelManager.getChildren().size());
  }

  @Test
  public void testRemoveChild() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel(getName());
    Workflow workflow1 = createWorkflow(getName() + "_1");
    Workflow workflow2 = createWorkflow(getName() + "_2");

    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    WorkflowManager child1 = new WorkflowManager(workflow1, channelManager);
    WorkflowManager child2 = new WorkflowManager(workflow2, channelManager);

    assertEquals(2, channelManager.getChildren().size());
    assertTrue(channelManager.removeChild(child1));
    assertFalse(channelManager.getChildren().contains(child1.createObjectName()));
    assertEquals(1, channelManager.getChildren().size());
    try {
      channelManager.removeChild(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(1, channelManager.getChildren().size());
  }

  @Test
  public void testAddChildren() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel(getName());
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    DisconnectedWorkflowManager child1 = new DisconnectedWorkflowManager();
    DisconnectedWorkflowManager child2 = new DisconnectedWorkflowManager();
    List<WorkflowRuntimeManager> workflows = new ArrayList<WorkflowRuntimeManager>(Arrays.asList(new DisconnectedWorkflowManager[]
    {
        child1, child2
    }));
    try {
      channelManager.addChildren(workflows);
      fail();
    }
    catch (UnsupportedOperationException expected) {

    }

    //
    // String adapterName = this.getClass().getSimpleName() + "." + getName();
    // Adapter adapter = createAdapter(adapterName, 1, 2);
    // AdapterManager adapterManager = new AdapterManager(adapter);
    // Channel channel = adapter.getChannelList().get(0);
    // ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    // WorkflowManager child1 = new WorkflowManager(channel.getWorkflowList().get(0), channelManager);
    // WorkflowManager child2 = new WorkflowManager(channel.getWorkflowList().get(1), channelManager);
    // List<WorkflowManagerMBean> workflows = new ArrayList<WorkflowManagerMBean>(Arrays.asList(new WorkflowManager[]
    // {
    // child1, child2
    // }));
    // assertEquals(2, channelManager.getChildren().size());
    // assertFalse(channelManager.addChildren(workflows));
    // assertEquals(2, channelManager.getChildren().size());
  }

  @Test
  public void testRemoveChildren() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel(getName());
    Workflow workflow1 = createWorkflow(getName() + "_1");
    Workflow workflow2 = createWorkflow(getName() + "_2");

    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    WorkflowManager child1 = new WorkflowManager(workflow1, channelManager);
    WorkflowManager child2 = new WorkflowManager(workflow2, channelManager);
    List<WorkflowRuntimeManager> workflows = new ArrayList<WorkflowRuntimeManager>(Arrays.asList(new WorkflowManager[]
    {
        child1, child2
    }));
    assertEquals(2, channelManager.getChildren().size());
    assertEquals(2, channel.getWorkflowList().size());
    assertTrue(channelManager.removeChildren(workflows));
    assertFalse(channelManager.removeChildren(workflows));
    assertEquals(0, channelManager.getChildren().size());
    assertEquals(0, channel.getWorkflowList().size());
  }

  @Test
  public void testChannelManager_HasRetryMonitor() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    c1.setMessageErrorHandler(new RetryMessageErrorHandler(getName()));

    ChannelManager cm1 = new ChannelManager(c1, adapterManager);
    ObjectName channelObj = cm1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>(Arrays.asList(new BaseComponentMBean[]
    {
        adapterManager, cm1
    }));
    try {
      register(mBeans);
      ObjectName handlerObjectName = ObjectName
          .getInstance(JMX_RETRY_MONITOR_TYPE + cm1.createObjectHierarchyString() + ID_PREFIX + getName());

      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertTrue(channelManagerProxy.getChildRuntimeInfoComponents().contains(handlerObjectName));
      RetryMessageErrorHandlerMonitorMBean monitor = JMX.newMBeanProxy(mBeanServer, handlerObjectName,
          RetryMessageErrorHandlerMonitorMBean.class);
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterClosed_InitChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
      try {
        channelManagerProxy.requestInit();
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(adapterManager.getComponentState(), InitialisedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterClosed_StartChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
      try {
        channelManagerProxy.requestStart();
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(adapterManager.getComponentState(), StartedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterClosed_StopChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
      try {
        channelManagerProxy.requestStop();
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(adapterManager.getComponentState(), StoppedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterClosed_CloseChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>(Arrays.asList(new BaseComponentMBean[]
    {
        adapterManager, child1, child2
    }));
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
      channelManagerProxy.requestClose();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterInitialised_InitChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>(Arrays.asList(new BaseComponentMBean[]
    {
        adapterManager, child1, child2
    }));
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestInit();
      channelManagerProxy.requestInit();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterInitialised_StartChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>(Arrays.asList(new BaseComponentMBean[]
    {
        adapterManager, child1, child2
    }));
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestInit();
      try {
        channelManagerProxy.requestStart();
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(adapterManager.getComponentState(), StartedState.getInstance()), e.getMessage());

      }
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterInitialised_StopChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>(Arrays.asList(new BaseComponentMBean[]
    {
        adapterManager, child1, child2
    }));
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestInit();
      try {
        channelManagerProxy.requestStop();
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(adapterManager.getComponentState(), StoppedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterInitialised_CloseChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestInit();
      channelManagerProxy.requestClose();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterStarted_InitChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManagerProxy.requestInit();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterStarted_StartChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManagerProxy.requestStart();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterStarted_StopChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>(Arrays.asList(new BaseComponentMBean[]
    {
        adapterManager, child1, child2
    }));
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManagerProxy.requestStop();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterStarted_CloseChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManagerProxy.requestClose();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAddChildRuntimeComponent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel c1 = createChannel(getName() + "_1");
    ChannelManager channelManager = new ChannelManager(c1, adapterManager);
    assertTrue(channelManager.addChildJmxComponent(new ChannelChild(channelManager)));
    assertEquals(1, channelManager.getChildRuntimeInfoComponents().size());
    assertEquals(1, channelManager.getAllDescendants().size());
  }

  @Test
  public void testRemoveChildRuntimeComponent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel c1 = createChannel(getName() + "_1");
    ChannelManager channelManager = new ChannelManager(c1, adapterManager);
    ChannelChild child = new ChannelChild(channelManager);
    assertTrue(channelManager.addChildJmxComponent(child));
    assertTrue(channelManager.addChildJmxComponent(new ChannelChild(channelManager)));
    assertTrue(channelManager.removeChildJmxComponent(child));
    assertFalse(channelManager.removeChildJmxComponent(child));
    assertEquals(1, channelManager.getChildRuntimeInfoComponents().size());
    assertEquals(1, channelManager.getAllDescendants().size());
  }

  @Test
  public void testAdapterStopped_InitChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>(Arrays.asList(new BaseComponentMBean[]
    {
        adapterManager, child1, child2
    }));
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      adapterManager.requestStop();
      try {
        channelManagerProxy.requestInit();
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(adapterManager.getComponentState(), InitialisedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterStopped_StartChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>(Arrays.asList(new BaseComponentMBean[]
    {
        adapterManager, child1, child2
    }));
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      adapterManager.requestStop();
      try {
        channelManagerProxy.requestStart();
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(adapterManager.getComponentState(), StartedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterStopped_StopChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      adapterManager.requestStop();
      channelManagerProxy.requestStop();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testAdapterStopped_CloseChannel() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    Channel c2 = createChannel(getName() + "_2");
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    ChannelManager child2 = new ChannelManager(c2, adapterManager);
    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      adapterManager.requestStop();
      channelManagerProxy.requestClose();
    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testMBean_AddWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = createChannel(getName() + "_1");
    ObjectName adapterObj = adapterManager.createObjectName();
    ChannelManager channelManager = new ChannelManager(newChannel, adapterManager);
    ObjectName channelObj = channelManager.createObjectName();
    StandardWorkflow newWorkflow = createWorkflow(getName() + "_1");

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      adapterManager.registerMBean();

      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      ObjectName workflowObj = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow));
      assertNotNull(workflowObj);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      assertEquals(ClosedState.getInstance(), workflowManagerProxy.getComponentState());
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      Channel marshalledChannel = (Channel) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          channelManagerProxy.getConfiguration());

      assertEquals(1, marshalledAdapter.getChannelList().size());
      assertEquals(1, marshalledAdapter.getChannelList().get(0).getWorkflowList().size());
      assertEquals(1, marshalledChannel.getWorkflowList().size());
    }
    finally {
    }
  }

  @Test
  public void testMBean_AddWorkflow_InvalidState() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = createChannel(getName() + "_1");
    ObjectName adapterObj = adapterManager.createObjectName();
    ChannelManager channelManager = new ChannelManager(newChannel, adapterManager);
    ObjectName channelObj = channelManager.createObjectName();
    StandardWorkflow newWorkflow = createWorkflow(getName() + "_1");

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      try {
        ObjectName workflowObj = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow));
        fail();
      }
      catch (IllegalStateException e) {

      }
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_AddWorkflow_NoUniqueId() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = createChannel(getName() + "_1");
    ObjectName adapterObj = adapterManager.createObjectName();
    ChannelManager channelManager = new ChannelManager(newChannel, adapterManager);
    ObjectName channelObj = channelManager.createObjectName();
    StandardWorkflow newWorkflow = new StandardWorkflow();

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      adapterManager.registerMBean();
      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      try {
        ObjectName workflowObj = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow));
        fail();
      }
      catch (CoreException e) {

      }
    }
    finally {
      adapterManager.requestClose();
    }
  }

  @Test
  public void testMBean_RemoveWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = createChannel(getName() + "_1");
    ObjectName adapterObj = adapterManager.createObjectName();
    ChannelManager channelManager = new ChannelManager(newChannel, adapterManager);
    ObjectName channelObj = channelManager.createObjectName();
    StandardWorkflow newWorkflow1 = createWorkflow(getName() + "_1");
    StandardWorkflow newWorkflow2 = createWorkflow(getName() + "_2");

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      adapterManager.registerMBean();

      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      ObjectName workflowObj1 = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow1));
      ObjectName workflowObj2 = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow2));
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj1, WorkflowManagerMBean.class);
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      Channel marshalledChannel = (Channel) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          channelManagerProxy.getConfiguration());
      assertEquals(1, marshalledAdapter.getChannelList().size());
      assertEquals(2, marshalledAdapter.getChannelList().get(0).getWorkflowList().size());
      assertEquals(2, marshalledChannel.getWorkflowList().size());

      assertTrue(channelManagerProxy.removeWorkflow(newWorkflow1.getUniqueId()));

      marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(adapterManagerProxy.getConfiguration());
      marshalledChannel = (Channel) DefaultMarshaller.getDefaultMarshaller().unmarshal(channelManagerProxy.getConfiguration());
      assertEquals(1, marshalledAdapter.getChannelList().size());
      assertEquals(1, marshalledAdapter.getChannelList().get(0).getWorkflowList().size());
      assertEquals(1, marshalledChannel.getWorkflowList().size());

    }
    finally {
    }

  }

  @Test
  public void testMBean_RemoveWorkflow_AddWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = createChannel(getName() + "_1");
    ObjectName adapterObj = adapterManager.createObjectName();
    ChannelManager channelManager = new ChannelManager(newChannel, adapterManager);
    ObjectName channelObj = channelManager.createObjectName();
    StandardWorkflow newWorkflow1 = createWorkflow(getName() + "_1");
    StandardWorkflow newWorkflow2 = createWorkflow(getName() + "_2");

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      adapterManager.registerMBean();

      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);

      ObjectName workflowObj1 = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow1));
      ObjectName workflowObj2 = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow2));

      assertEquals(2, channelManagerProxy.getChildren().size());

      assertTrue(channelManagerProxy.removeWorkflow(newWorkflow1.getUniqueId()));
      assertFalse(JmxHelper.findMBeanServer().isRegistered(workflowObj1));

      assertNotNull(channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow1)));
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj1, WorkflowManagerMBean.class);
    }
    finally {
    }

  }

  @Test
  public void testMBean_RemoveWorkflow_NotFound() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel newChannel = createChannel(getName() + "_1");
    ObjectName adapterObj = adapterManager.createObjectName();
    ChannelManager channelManager = new ChannelManager(newChannel, adapterManager);
    ObjectName channelObj = channelManager.createObjectName();
    StandardWorkflow newWorkflow1 = createWorkflow(getName() + "_1");
    StandardWorkflow newWorkflow2 = createWorkflow(getName() + "_2");

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      adapterManager.registerMBean();

      AdapterManagerMBean adapterManagerProxy = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      ObjectName workflowObj1 = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow1));
      ObjectName workflowObj2 = channelManagerProxy.addWorkflow(DefaultMarshaller.getDefaultMarshaller().marshal(newWorkflow2));
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj1, WorkflowManagerMBean.class);
      Adapter marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          adapterManagerProxy.getConfiguration());
      Channel marshalledChannel = (Channel) DefaultMarshaller.getDefaultMarshaller().unmarshal(
          channelManagerProxy.getConfiguration());
      assertEquals(1, marshalledAdapter.getChannelList().size());
      assertEquals(2, marshalledAdapter.getChannelList().get(0).getWorkflowList().size());
      assertEquals(2, marshalledChannel.getWorkflowList().size());

      assertFalse(channelManagerProxy.removeWorkflow(null));
      assertFalse(channelManagerProxy.removeWorkflow(getName()));

      marshalledAdapter = (Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(adapterManagerProxy.getConfiguration());
      marshalledChannel = (Channel) DefaultMarshaller.getDefaultMarshaller().unmarshal(channelManagerProxy.getConfiguration());
      assertEquals(1, marshalledAdapter.getChannelList().size());
      assertEquals(2, marshalledAdapter.getChannelList().get(0).getWorkflowList().size());
      assertEquals(2, marshalledChannel.getWorkflowList().size());

    }
    finally {
    }

  }

  @Test
  public void testMBean_NotificationOnInit() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    c1.setAutoStart(false);
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    SimpleNotificationListener listener = new SimpleNotificationListener();

    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(channelObj, listener, null, null);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestInit();
      assertEquals(InitialisedState.getInstance(), channelManagerProxy.getComponentState());
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_CHANNEL_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_INITIALISED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(channelObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testMBean_NotificationOnStart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    c1.setAutoStart(false);
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    SimpleNotificationListener listener = new SimpleNotificationListener();

    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(channelObj, listener, null, null);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestStart();
      assertEquals(StartedState.getInstance(), channelManagerProxy.getComponentState());
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_CHANNEL_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_STARTED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(channelObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testMBean_NotificationOnStop() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    c1.setAutoStart(false);
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    SimpleNotificationListener listener = new SimpleNotificationListener();

    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(channelObj, listener, null, null);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestStart(2000);
      channelManagerProxy.requestStop(2000);
      assertEquals(StoppedState.getInstance(), channelManagerProxy.getComponentState());
      listener.waitForMessages(2);
      assertEquals(2, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(1);
      assertEquals(NOTIF_TYPE_CHANNEL_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_STOPPED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(channelObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testMBean_NotificationOnClose() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    c1.setAutoStart(false);
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    SimpleNotificationListener listener = new SimpleNotificationListener();

    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(channelObj, listener, null, null);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestStart();
      channelManagerProxy.requestClose();
      assertEquals(ClosedState.getInstance(), channelManagerProxy.getComponentState());
      listener.waitForMessages(2);
      // Timing issues under gradle
      // assertEquals(2, listener.getNotifications().size());
      // Notification n = listener.getNotifications().get(1);
      // assertEquals(NOTIF_TYPE_CHANNEL_LIFECYCLE, n.getType());
      // assertEquals(NOTIF_MSG_CLOSED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(channelObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testMBean_NotificationOnRestart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);

    Channel c1 = createChannel(getName() + "_1");
    c1.setAutoStart(false);
    ChannelManager child1 = new ChannelManager(c1, adapterManager);
    SimpleNotificationListener listener = new SimpleNotificationListener();

    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(channelObj, listener, null, null);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestStart();
      channelManagerProxy.requestRestart();
      assertEquals(StartedState.getInstance(), channelManagerProxy.getComponentState());
      // This generates 4 notifications 1-start, 2-close 3-start 4-restart
      listener.waitForMessages(4);
      // Apparent timing issue on Jenkins.
      // assertEquals(4, listener.getNotifications().size());
      // Notification n = listener.getNotifications().get(3);
      // assertEquals(NOTIF_TYPE_CHANNEL_LIFECYCLE, n.getType());
      // assertEquals(NOTIF_MSG_RESTARTED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(channelObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testMBean_NotificationOnAddWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    String workflowXml = DefaultMarshaller.getDefaultMarshaller().marshal(new PoolingWorkflow(getName() + "_wf1"));
    ChannelManager child1 = new ChannelManager(createChannel(getName() + "_1"), adapterManager);
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_CHANNEL_CONFIG);

    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();

    try {
      adapterManager.registerMBean();

      mBeanServer.addNotificationListener(channelObj, listener, null, null);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);

      channelManagerProxy.addWorkflow(workflowXml);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_CHANNEL_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(channelManagerProxy.getConfiguration(), n.getUserData());
    }
    finally {
      mBeanServer.removeNotificationListener(channelObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Test
  public void testMBean_NotificationOnRemoveWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    
    String workflowXml = DefaultMarshaller.getDefaultMarshaller().marshal(new PoolingWorkflow(getName() + "_wf1"));
    Channel channel = createChannel(getName() + "_1");
    StandardWorkflow workflow = createWorkflow(getName() + "_1");
    channel.getWorkflowList().add(workflow);
    
    ChannelManager child1 = new ChannelManager(channel, adapterManager);
    SimpleNotificationListener listener = new SimpleNotificationListener();
    NotificationFilterSupport filter = new NotificationFilterSupport();
    filter.enableType(NOTIF_TYPE_CHANNEL_CONFIG);

    ObjectName adapterObj = adapterManager.createObjectName();
    ObjectName channelObj = child1.createObjectName();
    
    try {
      adapterManager.registerMBean();

      mBeanServer.addNotificationListener(channelObj, listener, null, null);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);

      channelManagerProxy.removeWorkflow(workflow.getUniqueId());
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_CHANNEL_CONFIG, n.getType());
      assertEquals(NOTIF_MSG_CONFIG_UPDATED, n.getMessage());
      assertEquals(channelManagerProxy.getConfiguration(), n.getUserData());
    }
    finally {
      mBeanServer.removeNotificationListener(channelObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();

    }
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  private class FailingChannelManager extends ChannelManager {
    public FailingChannelManager(Channel c, AdapterManager owner) throws MalformedObjectNameException, CoreException {
      super(c, owner);
    }

    @Override
    public ObjectName createObjectName() throws MalformedObjectNameException {
      throw new MalformedObjectNameException();
    }
  }

  private class DisconnectedWorkflowManager extends ComponentManagerImpl<Workflow> implements WorkflowManagerMBean,
      WorkflowRuntimeManager {

    @Override
    public long requestStartTime() {
      return 0;
    }

    @Override
    public long requestStopTime() {
      return 0;
    }

    @Override
    public String getConfiguration() throws CoreException {
      return null;
    }

    @Override
    public Workflow getWrappedComponent() {
      return null;
    }

    @Override
    public ObjectName createObjectName() throws MalformedObjectNameException {
      return null;
    }

    @Override
    public ChannelManager getParent() {
      return null;
    }

    @Override
    public boolean injectMessage(SerializableAdaptrisMessage serialisedMessage) throws CoreException {
      return false;
    }

    @Override
    public SerializableAdaptrisMessage injectMessageWithReply(SerializableAdaptrisMessage serialisedMessage) throws CoreException {
      return null;
    }

    @Override
    protected void checkTransitionTo(ComponentState futureState) throws CoreException {
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
    public Collection<BaseComponentMBean> getAllDescendants() {
      return null;
    }

    @Override
    public void registerMBean() throws CoreException {
      registerSelf();
    }

    @Override
    public void unregisterMBean() throws CoreException {
      unregisterSelf();
    }

    @Override
    protected String getNotificationType(ComponentNotificationType type) {
      return null;
    }

    @Override
    public void processAsync(SerializableMessage paramSerializableMessage) throws InterlokException {}

    @Override
    public SerializableMessage process(SerializableMessage paramSerializableMessage) throws InterlokException {
      return null;
    }

  }

  private class ChannelChild extends StubBaseComponentMBean implements ChildRuntimeInfoComponent {
    private ChannelManager myParent;
    private String uid = UUID.randomUUID().toString();

    ChannelChild(ChannelManager wm) {
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
}
