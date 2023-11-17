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

package com.adaptris.core.interceptor;

import static com.adaptris.core.runtime.AdapterComponentMBean.ADAPTER_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.CHANNEL_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_ADAPTER_TYPE;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_METRICS_TYPE;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_WORKFLOW_TYPE;
import static com.adaptris.core.runtime.AdapterComponentMBean.WORKFLOW_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.ChildRuntimeInfoComponent;
import com.adaptris.core.runtime.WorkflowManagerMBean;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.GuidGenerator;

public abstract class StatisticsMBeanCase extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  protected static final String DEFAULT_INTERCEPTOR_NAME = "MMI";
  protected static final GuidGenerator GUID = new GuidGenerator();

  protected MBeanServer mBeanServer;
  protected List<ObjectName> registeredObjects;

  @BeforeEach
  public void setUp() throws Exception {
    mBeanServer = JmxHelper.findMBeanServer();
    registeredObjects = new ArrayList<ObjectName>();
  }

  @AfterEach
  public void tearDown() throws Exception {
    for (ObjectName bean : registeredObjects) {
      if (mBeanServer.isRegistered(bean)) {
        mBeanServer.unregisterMBean(bean);
        log.trace(bean + " unregistered");
      }
    }
  }

  @Test
  public void testRegistration() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager am = new AdapterManager(adapter);
    try {
      start(adapter);
      am.registerMBean();
    }
    finally {
      am.unregisterMBean();
      stop(adapter);
    }
  }

  @Test
  public void testStandardGetters() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    AdapterManager am = new AdapterManager(adapter);
    try {
      start(adapter);
      am.registerMBean();
      Collection<BaseComponentMBean> children = am.getAllDescendants();
      for (BaseComponentMBean bean : children) {
        assertNotNull(bean.createObjectName());
        if (bean instanceof ChildRuntimeInfoComponent) {
          assertNotNull(((ChildRuntimeInfoComponent) bean).getParentId());
          assertNotNull(((ChildRuntimeInfoComponent) bean).getParentObjectName());
          assertNotNull(((ChildRuntimeInfoComponent) bean).getParentRuntimeInfoComponent());
        }
      }
    }
    finally {
      stop(adapter);
      am.unregisterMBean();
    }
  }

  @Test
  public void testGet_NoTimeslicesAvailable() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MetricsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MetricsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      assertEquals(0, stats.getNumberOfTimeSlices());
    }
    finally {
      stop(adapter);
    }
  }

  @Test
  public void testGetTimesliceDuration() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MetricsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MetricsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      assertEquals(10, stats.getTimeSliceDurationSeconds());
    }
    finally {
      stop(adapter);
    }
  }

  protected Adapter createAdapter(String uid) throws CoreException {
    return createAdapter(uid, 1, 1);
  }

  protected Adapter createSingleChannelAdapter(String uid, WorkflowInterceptor... interceptors) throws CoreException {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(uid);
    adapter.getChannelList().add(createSingleWorkflowChannel("channel1", interceptors));
    return adapter;
  }

  protected Adapter createAdapter(String uid, int channels, int workflows) throws CoreException {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(uid);
    for (int i = 0; i < channels; i++) {
      adapter.getChannelList().add(createChannel("channel" + (i + 1), workflows));
    }
    return adapter;
  }

  protected ObjectName createAdapterObjectName(String uid) throws Exception {
    return ObjectName.getInstance(JMX_ADAPTER_TYPE + ID_PREFIX + uid);
  }

  protected ObjectName createMetricsObjectName(String uid) throws Exception {
    return ObjectName.getInstance(JMX_METRICS_TYPE + ADAPTER_PREFIX + uid + CHANNEL_PREFIX + "channel1" + WORKFLOW_PREFIX
        + "workflow1" + ID_PREFIX + DEFAULT_INTERCEPTOR_NAME);
  }

  protected ObjectName createMetricsObjectName(String uid, String metricsId) throws Exception {
    return ObjectName.getInstance(JMX_METRICS_TYPE + ADAPTER_PREFIX + uid + CHANNEL_PREFIX + "channel1" + WORKFLOW_PREFIX
        + "workflow1" + ID_PREFIX + metricsId);
  }

  protected ObjectName createWorkflowObjectName(String uid) throws Exception {
    return ObjectName.getInstance(JMX_WORKFLOW_TYPE + ADAPTER_PREFIX + uid + CHANNEL_PREFIX + "channel1" + ID_PREFIX + "workflow1");
  }

  protected Channel createChannel(String uid, int workflows) throws CoreException {
    Channel c = new Channel();
    c.setUniqueId(uid);
    for (int i = 0; i < workflows; i++) {
      c.getWorkflowList().add(createWorkflow("workflow" + (i + 1)));
    }
    return c;
  }

  protected Channel createSingleWorkflowChannel(String uid, WorkflowInterceptor... interceptors) throws CoreException {
    Channel c = new Channel();
    c.setUniqueId(uid);
    c.getWorkflowList().add(createWorkflow("workflow1", interceptors));
    return c;
  }

  protected Channel createChannel(String uid) throws CoreException {
    return createChannel(uid, 0);
  }

  protected StandardWorkflow createWorkflow(String uid) throws CoreException
  {
    WorkflowInterceptorImpl interceptor = createInterceptor();
    interceptor.setUniqueId(DEFAULT_INTERCEPTOR_NAME);
    return createWorkflow(uid, interceptor);
  }

  protected static StandardWorkflow createWorkflow(String uid, WorkflowInterceptor... interceptors)
      throws CoreException {
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId(uid);
    for (WorkflowInterceptor wi : interceptors) {
      wf.addInterceptor(wi);
    }
    return wf;
  }

  protected void register(Collection<BaseComponentMBean> mBeans) throws Exception {
    for (BaseComponentMBean bean : mBeans) {
      ObjectName name = bean.createObjectName();
      registeredObjects.add(name);
      mBeanServer.registerMBean(bean, name);
      log.trace("MBean [" + bean + "] registered");
    }
  }

  protected List<BaseComponentMBean> createJmxManagers(Adapter adapter) throws Exception {
    List<BaseComponentMBean> result = new ArrayList<BaseComponentMBean>();
    AdapterManager am = new AdapterManager(adapter);
    result.add(am);
    result.addAll(am.getAllDescendants());
    return result;
  }

  protected abstract SerializableAdaptrisMessage createMessageForInjection(String payload);

  protected abstract WorkflowInterceptorImpl createInterceptor();

}
