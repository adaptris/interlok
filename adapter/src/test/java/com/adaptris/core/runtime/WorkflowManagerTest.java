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

import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_MSG_CLOSED;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_MSG_INITIALISED;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_MSG_STARTED;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_MSG_STOPPED;
import static com.adaptris.core.runtime.AdapterComponentMBean.NOTIF_TYPE_WORKFLOW_LIFECYCLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.Workflow;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.http.jetty.JettyPoolingWorkflowInterceptor;
import com.adaptris.core.http.jetty.MessageConsumer;
import com.adaptris.core.interceptor.InFlightWorkflowInterceptor;
import com.adaptris.core.interceptor.MessageMetricsInterceptor;
import com.adaptris.core.interceptor.ThrottlingInterceptor;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubSerializableMessage;
import com.adaptris.interlok.management.MessageProcessor;
import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.GuidGenerator;

public class WorkflowManagerTest extends ComponentManagerCase {
  private static final String PAYLOAD = "Quick zephyrs blow, vexing daft Jim";
  private static final String PAYLOAD_ENCODING = "UTF-8";
  private static final String METADATA_KEY = "my-key";
  private static final String METADATA_VALUE = "myvalue";
  
  private static final long TIMEOUT_MILLIS = 60000;

  public WorkflowManagerTest(String name) {
    super(name);
  }


  public void testJettyInterceptor_AutoAdded() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    PoolingWorkflow workflow = new PoolingWorkflow("w1");
    workflow.setConsumer(new MessageConsumer());
    new WorkflowManager(workflow, channelManager);
    assertEquals(3, workflow.getInterceptors().size());
    assertEquals(MessageMetricsInterceptor.class, workflow.getInterceptors().get(0).getClass());
    assertEquals(InFlightWorkflowInterceptor.class, workflow.getInterceptors().get(1).getClass());
    assertEquals(JettyPoolingWorkflowInterceptor.class, workflow.getInterceptors().get(2).getClass());
  }

  public void testJettyInterceptor_AlreadyHasInterceptor() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    PoolingWorkflow workflow = new PoolingWorkflow("w1");
    workflow.addInterceptor(new JettyPoolingWorkflowInterceptor());
    workflow.setConsumer(new MessageConsumer());
    new WorkflowManager(workflow, channelManager);

    assertEquals(3, workflow.getInterceptors().size());

    // Look the order should now be swapped.
    assertEquals(JettyPoolingWorkflowInterceptor.class, workflow.getInterceptors().get(0).getClass());
    assertEquals(MessageMetricsInterceptor.class, workflow.getInterceptors().get(1).getClass());
    assertEquals(InFlightWorkflowInterceptor.class, workflow.getInterceptors().get(2).getClass());
  }

  public void testJettyInterceptor_NotAddedTo_StandardWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    workflow.setConsumer(new MessageConsumer());
    new WorkflowManager(workflow, channelManager);
    assertEquals(2, workflow.getInterceptors().size());
    assertEquals(MessageMetricsInterceptor.class, workflow.getInterceptors().get(0).getClass());
    assertEquals(InFlightWorkflowInterceptor.class, workflow.getInterceptors().get(1).getClass());
  }

  public void testMessageCounter_Enabled() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    new WorkflowManager(workflow, channelManager);
    assertEquals(2, workflow.getInterceptors().size());
    assertEquals(MessageMetricsInterceptor.class, workflow.getInterceptors().get(0).getClass());
    assertEquals(InFlightWorkflowInterceptor.class, workflow.getInterceptors().get(1).getClass());
  }

  public void testMessageCounter_Enabled_AlreadyHasMessageCounter() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    workflow.getInterceptors().add(new MessageMetricsInterceptor(getName(), null));
    new WorkflowManager(workflow, channelManager);
    assertEquals(2, workflow.getInterceptors().size());
    assertEquals(MessageMetricsInterceptor.class, workflow.getInterceptors().get(0).getClass());
    assertEquals(getName(), workflow.getInterceptors().get(0).getUniqueId());
    assertEquals(InFlightWorkflowInterceptor.class, workflow.getInterceptors().get(1).getClass());
  }

  public void testMessageCounter_Disabled() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    workflow.setDisableDefaultMessageCount(true);
    new WorkflowManager(workflow, channelManager);
    assertEquals(0, workflow.getInterceptors().size());
  }

  public void testConstructor_WithInterceptor_RuntimeInfoFactoryRegistered() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    workflow.setDisableDefaultMessageCount(true);
    workflow.getInterceptors().add(new MessageMetricsInterceptor());
    WorkflowManager workflowManager = new WorkflowManager(workflow, channelManager);
    assertEquals(1, workflowManager.getChildRuntimeInfoComponents().size());

  }

  public void testConstructor_WithInterceptor_NoRuntimeInfoFactoryRegistration() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    workflow.setDisableDefaultMessageCount(true);
    workflow.getInterceptors().add(new ThrottlingInterceptor());
    WorkflowManager workflowManager = new WorkflowManager(workflow, channelManager);
    assertEquals(0, workflowManager.getChildRuntimeInfoComponents().size());
  }

  public void testAddChildRuntimeComponent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    workflow.setDisableDefaultMessageCount(true);
    WorkflowManager workflowManager = new WorkflowManager(workflow, channelManager);
    WorkflowChild child = new WorkflowChild(workflowManager);
    new WorkflowChild(workflowManager);
    assertTrue(workflowManager.addChildJmxComponent(child));
    assertFalse(workflowManager.addChildJmxComponent(child));
    try {
      workflowManager.addChildJmxComponent(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(1, workflowManager.getChildRuntimeInfoComponents().size());
  }

  public void testRemoveChildRuntimeComponent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    workflow.setDisableDefaultMessageCount(true);
    WorkflowManager workflowManager = new WorkflowManager(workflow, channelManager);
    WorkflowChild child = new WorkflowChild(workflowManager);
    WorkflowChild child2 = new WorkflowChild(workflowManager);
    assertTrue(workflowManager.addChildJmxComponent(child));
    try {
      workflowManager.removeChildJmxComponent(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertTrue(workflowManager.removeChildJmxComponent(child));
    assertFalse(workflowManager.removeChildJmxComponent(child2));
    assertEquals(0, workflowManager.getChildRuntimeInfoComponents().size());

  }

  public void testEqualityHashCode() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    AdapterManager am1 = new AdapterManager(createAdapter(adapterName));
    AdapterManager am2 = new AdapterManager(createAdapter(adapterName));
    AdapterManager fam = new AdapterManager(createAdapter(adapterName));

    ChannelManager cm1 = new ChannelManager(createChannel("c1"), am1);
    ChannelManager cm2 = new ChannelManager(createChannel("c1"), am2);
    ChannelManager fcm = new ChannelManager(createChannel("c1"), fam);

    Workflow workflow = createWorkflow("w1");
    WorkflowManager wm1 = new WorkflowManager(workflow, cm1);
    WorkflowManager wm2 = new WorkflowManager(workflow, cm2);
    WorkflowManager fm = new FailingWorkflowManager(workflow, fcm);
    WorkflowManager wm3 = new WorkflowManager(createWorkflow("w2"), cm1);
    assertEquals(wm1, wm1);
    assertEquals(wm1, wm2);
    assertNotSame(wm1, wm3);
    assertFalse(wm1.equals(fm));
    assertFalse(wm1.equals(new Object()));
    assertFalse(wm1.equals(null));
    assertEquals(wm1.hashCode(), wm1.hashCode());
  }

  public void testGetConfiguration() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager workflowManager = new WorkflowManager(workflow, channelManager);
    Workflow marshalledWorkflow = (Workflow) new XStreamMarshaller().unmarshal(workflowManager.getConfiguration());
    assertRoundtripEquality(workflow, marshalledWorkflow);
    Channel marshalledChannel = (Channel) new XStreamMarshaller().unmarshal(channelManager.getConfiguration());
    assertRoundtripEquality(channel, marshalledChannel);

    Adapter marshalledAdapter = (Adapter) new XStreamMarshaller().unmarshal(adapterManager.getConfiguration());
    assertRoundtripEquality(adapter, marshalledAdapter);
  }

  public void testGetParent() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    AdapterManager adapterManager2 = new AdapterManager(createAdapter(adapterName));
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    ChannelManager channelManager_2 = new ChannelManager(createChannel("c1"), adapterManager2);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager workflowManager = new WorkflowManager(workflow, channelManager);
    assertEquals(channelManager, workflowManager.getParent());
    assertTrue(channelManager == workflowManager.getParent());
    assertEquals(channelManager_2, workflowManager.getParent());
    assertFalse(channelManager_2 == workflowManager.getParent());
  }

  public void testGetParentId() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    new AdapterManager(createAdapter(adapterName));
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager workflowManager = new WorkflowManager(workflow, channelManager);
    assertEquals(channelManager, workflowManager.getParent());
    assertTrue(channelManager == workflowManager.getParent());
    assertEquals("c1", workflowManager.getParentId());
  }

  public void testGetParentObjectName() throws Exception {

    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    new AdapterManager(createAdapter(adapterName));
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager workflowManager = new WorkflowManager(workflow, channelManager);
    assertEquals(channelManager, workflowManager.getParent());
    assertTrue(channelManager == workflowManager.getParent());
    assertEquals(channelManager.createObjectName(), workflowManager.getParentObjectName());
  }

  public void testProxyEquality() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      assertEquals(realWorkflowManager, workflowManagerProxy);
      assertFalse(realWorkflowManager == workflowManagerProxy);
    }
    finally {
    }
  }

  public void testGetState() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      assertEquals(ClosedState.getInstance(), workflowManagerProxy.getComponentState());
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testClose() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      adapterManager.requestStart();
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
      assertEquals(ClosedState.getInstance(), workflowManagerProxy.getComponentState());
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testStop() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      adapterManager.requestStart();
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
      workflowManagerProxy.requestStop(TIMEOUT_MILLIS);
      assertEquals(StoppedState.getInstance(), workflowManagerProxy.getComponentState());
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testStart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      adapterManager.requestStart();
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
      workflowManagerProxy.requestStop(TIMEOUT_MILLIS);
      assertEquals(StoppedState.getInstance(), workflowManagerProxy.getComponentState());
      workflowManagerProxy.requestStart(TIMEOUT_MILLIS);
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testInitialise() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      adapterManager.requestStart();
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
      assertEquals(ClosedState.getInstance(), workflowManagerProxy.getComponentState());
      workflowManagerProxy.requestInit(TIMEOUT_MILLIS);
      assertEquals(InitialisedState.getInstance(), workflowManagerProxy.getComponentState());
    }
    finally {
      adapter.requestClose();
    }

  }

  public void testRestart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      adapterManager.requestStart();
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
      workflowManagerProxy.requestStop(TIMEOUT_MILLIS);
      assertEquals(StoppedState.getInstance(), workflowManagerProxy.getComponentState());
      workflowManagerProxy.requestRestart(TIMEOUT_MILLIS);
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
    }
    finally {
      adapter.requestClose();
    }

  }

  public void testLastStartTime() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      assertEquals(0, workflowManagerProxy.requestStartTime());
      adapterManager.requestStart();
      assertTrue(workflowManagerProxy.requestStartTime() > 0);
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testLastStopTime() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      long t1 = workflowManagerProxy.requestStopTime();
      assertTrue(t1 > 0);
      adapterManager.requestStart();
      adapterManager.requestStop();
      assertTrue(t1 <= workflowManagerProxy.requestStopTime());
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelClosed_InitWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      channelManagerProxy.requestClose(TIMEOUT_MILLIS);
      try {
        workflowManagerProxy.requestInit(TIMEOUT_MILLIS);
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(ClosedState.getInstance(), InitialisedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelClosed_StartWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManagerProxy.requestClose(TIMEOUT_MILLIS);
      try {
        workflowManagerProxy.requestStart(TIMEOUT_MILLIS);
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(ClosedState.getInstance(), StartedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelClosed_StopWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManagerProxy.requestClose(TIMEOUT_MILLIS);
      try {
        workflowManagerProxy.requestStop(TIMEOUT_MILLIS);
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(ClosedState.getInstance(), StoppedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelClosed_CloseWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      ChannelManagerMBean channelManagerProxy = JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManagerProxy.requestClose(TIMEOUT_MILLIS);
      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelInitialised_InitWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManager.requestClose();
      channelManager.requestInit();
      workflowManagerProxy.requestInit(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelInitialised_StartWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManager.requestClose();
      channelManager.requestInit();
      try {
        workflowManagerProxy.requestStart(TIMEOUT_MILLIS);
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(InitialisedState.getInstance(), StartedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelInitialised_StopWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManager.requestClose();
      channelManager.requestInit();
      try {
        workflowManagerProxy.requestStop(TIMEOUT_MILLIS);
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(InitialisedState.getInstance(), StoppedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelInitialised_CloseWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManager.requestClose();
      channelManager.requestInit();
      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelStarted_InitWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
      workflowManagerProxy.requestInit(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelStarted_StartWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      workflowManagerProxy.requestStart(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelStarted_StopWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      workflowManagerProxy.requestStop(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }

  }

  public void testChannelStarted_CloseWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testChannelStopped_InitWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManager.requestStop();
      try {
        workflowManagerProxy.requestInit(TIMEOUT_MILLIS);
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(StoppedState.getInstance(), InitialisedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }

  }

  public void testChannelStopped_StartWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManager.requestStop();
      try {
        workflowManagerProxy.requestStart(TIMEOUT_MILLIS);
        fail();
      }
      catch (CoreException e) {
        assertEquals(createErrorMessageString(StoppedState.getInstance(), StartedState.getInstance()), e.getMessage());
      }
    }
    finally {
      adapter.requestClose();
    }

  }

  public void testChannelStopped_StopWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManager.requestStop();
      workflowManagerProxy.requestStop(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }

  }

  public void testChannelStopped_CloseWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    ObjectName channelObj = channelManager.createObjectName();

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      JMX.newMBeanProxy(mBeanServer, channelObj, ChannelManagerMBean.class);
      adapterManager.requestStart();
      channelManager.requestStop();
      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testInjectMessage() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    MockMessageProducer mockProducer = new MockMessageProducer();
    workflow.setProducer(mockProducer);
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    channelManager.createObjectName();

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());

    String msgUniqueId = new GuidGenerator().getUUID();
    SerializableAdaptrisMessage msg = createSAM(msgUniqueId);
    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      adapterManager.requestStart();
      workflowManagerProxy.processAsync(msg);
      assertEquals(1, mockProducer.getMessages().size());
      AdaptrisMessage procMsg = mockProducer.getMessages().get(0);
      assertEquals(msgUniqueId, procMsg.getUniqueId());
      assertEquals(PAYLOAD, procMsg.getContent());
      assertEquals(PAYLOAD_ENCODING, procMsg.getContentEncoding());
      assertTrue(procMsg.headersContainsKey(METADATA_KEY));
      assertEquals(METADATA_VALUE, procMsg.getMetadataValue(METADATA_KEY));
    }
    finally {
      adapter.requestClose();
    }
  }

  public void testInjectWithReply() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    workflow.getServiceCollection().add(new AddMetadataService(Arrays.asList(new MetadataElement(getName(), getName()))));

    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    channelManager.createObjectName();

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());

    String msgUniqueId = new GuidGenerator().getUUID();
    SerializableAdaptrisMessage msg = createSAM(msgUniqueId);

    try {
      register(mBeans);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      adapterManager.requestStart();
      SerializableAdaptrisMessage reply = (SerializableAdaptrisMessage) workflowManagerProxy.process(msg);

      assertEquals(msgUniqueId, reply.getUniqueId());
      assertEquals(PAYLOAD, reply.getContent());
      assertEquals(PAYLOAD_ENCODING, reply.getContentEncoding());
      assertTrue(reply.containsKey(METADATA_KEY));
      assertEquals(METADATA_VALUE, reply.getMetadataValue(METADATA_KEY));
      assertTrue(reply.containsKey(getName()));
      assertEquals(getName(), reply.getMetadataValue(getName()));
    } finally {
      adapter.requestClose();
    }
  }


  public void testProcessAsync() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    MockMessageProducer mockProducer = new MockMessageProducer();
    workflow.setProducer(mockProducer);
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    channelManager.createObjectName();

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());

    String msgUniqueId = new GuidGenerator().getUUID();
    SerializableMessage msg = createSM(msgUniqueId);

    try {
      register(mBeans);
      MessageProcessor workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, MessageProcessor.class);
      adapterManager.requestStart();
      workflowManagerProxy.processAsync(msg);
      assertEquals(1, mockProducer.getMessages().size());
      AdaptrisMessage procMsg = mockProducer.getMessages().get(0);
      assertEquals(msgUniqueId, procMsg.getUniqueId());
      assertEquals(PAYLOAD, procMsg.getContent());
      assertEquals(PAYLOAD_ENCODING, procMsg.getContentEncoding());
      assertTrue(procMsg.headersContainsKey(METADATA_KEY));
      assertEquals(METADATA_VALUE, procMsg.getMetadataValue(METADATA_KEY));
    } finally {
      adapter.requestClose();
    }
  }

  public void testProcess() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    StandardWorkflow workflow = createWorkflow("w1");
    workflow.getServiceCollection().add(new AddMetadataService(Arrays.asList(new MetadataElement(getName(), getName()))));

    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    channelManager.createObjectName();

    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());

    String msgUniqueId = new GuidGenerator().getUUID();
    SerializableMessage msg = createSM(msgUniqueId);

    try {
      register(mBeans);
      MessageProcessor workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, MessageProcessor.class);
      adapterManager.requestStart();
      SerializableMessage reply = workflowManagerProxy.process(msg);

      assertEquals(msgUniqueId, reply.getUniqueId());
      assertEquals(PAYLOAD, reply.getContent());
      assertEquals(PAYLOAD_ENCODING, reply.getContentEncoding());
      Map<String, String> headers = reply.getMessageHeaders();
      assertTrue(headers.containsKey(METADATA_KEY));
      assertEquals(METADATA_VALUE, headers.get(METADATA_KEY));
      assertTrue(headers.containsKey(getName()));
      assertEquals(getName(), headers.get(getName()));
      assertEquals("", reply.getNextServiceId());
    } finally {
      adapter.requestClose();
    }
  }


  public void testMBean_NotificationOnInit() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();    
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(workflowObj, listener, null, null);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);

      
      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
      workflowManagerProxy.requestInit(TIMEOUT_MILLIS);
      assertEquals(InitialisedState.getInstance(), workflowManagerProxy.getComponentState());
      listener.waitForMessages(2);
      assertEquals(2, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(1);
      assertEquals(NOTIF_TYPE_WORKFLOW_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_INITIALISED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(workflowObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  public void testMBean_NotificationOnStart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(workflowObj, listener, null, null);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);

      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
      workflowManagerProxy.requestStart(TIMEOUT_MILLIS);
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
      listener.waitForMessages(2);
      assertEquals(2, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(1);
      assertEquals(NOTIF_TYPE_WORKFLOW_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_STARTED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(workflowObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  public void testMBean_NotificationOnStop() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(workflowObj, listener, null, null);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);

      workflowManagerProxy.requestStop(TIMEOUT_MILLIS);
      assertEquals(StoppedState.getInstance(), workflowManagerProxy.getComponentState());
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_WORKFLOW_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_STOPPED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(workflowObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  public void testMBean_NotificationOnClose() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(workflowObj, listener, null, null);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);

      workflowManagerProxy.requestClose(TIMEOUT_MILLIS);
      assertEquals(ClosedState.getInstance(), workflowManagerProxy.getComponentState());
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification n = listener.getNotifications().get(0);
      assertEquals(NOTIF_TYPE_WORKFLOW_LIFECYCLE, n.getType());
      assertEquals(NOTIF_MSG_CLOSED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(workflowObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }
  
  public void testMBean_NotificationOnRestart() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    AdapterManager adapterManager = new AdapterManager(adapter);
    Channel channel = createChannel("c1");
    ChannelManager channelManager = new ChannelManager(channel, adapterManager);
    Workflow workflow = createWorkflow("w1");
    WorkflowManager realWorkflowManager = new WorkflowManager(workflow, channelManager);
    adapterManager.createObjectName();
    ObjectName workflowObj = realWorkflowManager.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();

      mBeanServer.addNotificationListener(workflowObj, listener, null, null);
      WorkflowManagerMBean workflowManagerProxy = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);

      workflowManagerProxy.requestRestart(TIMEOUT_MILLIS);
      assertEquals(StartedState.getInstance(), workflowManagerProxy.getComponentState());
      // restart generated 3 notificates 1-close 2-start 3-restart
      // Timing issue on Jenkins, doesn't happen locally.
      // listener.waitForMessages(3);
      // assertEquals(3, listener.getNotifications().size());
      // Notification n = listener.getNotifications().get(2);
      // assertEquals(NOTIF_TYPE_WORKFLOW_LIFECYCLE, n.getType());
      // assertEquals(NOTIF_MSG_RESTARTED, n.getMessage());
    }
    finally {
      mBeanServer.removeNotificationListener(workflowObj, listener);
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  private SerializableAdaptrisMessage createSAM(String msgUniqueId) {
    SerializableAdaptrisMessage  msg = new SerializableAdaptrisMessage();
    msg.setUniqueId(msgUniqueId);
    msg.setContent(PAYLOAD);
    msg.setContentEncoding(PAYLOAD_ENCODING);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    return msg;
  }

  private SerializableMessage createSM(String msgUniqueId) {
    SerializableMessage msg = new StubSerializableMessage();
    msg.setUniqueId(msgUniqueId);
    msg.setContent(PAYLOAD);
    msg.setContentEncoding(PAYLOAD_ENCODING);
    msg.addMessageHeader(METADATA_KEY, METADATA_VALUE);
    return msg;
  }

  private class FailingWorkflowManager extends WorkflowManager {
    public FailingWorkflowManager(Workflow w, ChannelManager owner) throws MalformedObjectNameException,
        CoreException,
        InstanceAlreadyExistsException {
      super(w, owner);
    }

    @Override
    public ObjectName createObjectName() throws MalformedObjectNameException {
      throw new MalformedObjectNameException();
    }
  }

  private class WorkflowChild extends StubBaseComponentMBean implements ChildRuntimeInfoComponent {
    private WorkflowManager myParent;
    private String uid = UUID.randomUUID().toString();

    WorkflowChild(WorkflowManager wm) {
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
