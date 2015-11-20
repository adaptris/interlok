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
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_ADAPTER_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.BaseCase;
import com.adaptris.core.Channel;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.GuidGenerator;

public abstract class ComponentManagerCase extends BaseCase {

  protected static GuidGenerator guid = new GuidGenerator();
  private static final String TRANSITION_CHECK_ERROR_MSG_HEAD = "Container Component State is ";
  private static final String TRANSITION_CHECK_ERROR_MSG_TAIL = "; not suitable for member transition to ";

  protected MBeanServer mBeanServer;
  protected List<ObjectName> registeredObjects;

  public ComponentManagerCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    mBeanServer = JmxHelper.findMBeanServer();
    registeredObjects = new ArrayList<ObjectName>();
  }

  @Override
  protected void tearDown() throws Exception {
    for (ObjectName bean : registeredObjects) {
      if (mBeanServer.isRegistered(bean)) {
        mBeanServer.unregisterMBean(bean);
        log.trace(bean + " unregistered");
      }
    }
  }

  protected File deleteLater(Object marker) throws IOException {
    return TempFileUtils.createTrackedFile(getName(), null, marker);
  }

  protected Adapter createAdapter(String uid) throws CoreException {
    return createAdapter(uid, 0, 0);
  }

  protected Adapter createAdapter(String uid, int channels, int workflows) throws CoreException {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(uid);
    for (int i = 0; i < channels; i++) {
      adapter.getChannelList().add(createChannel(guid.safeUUID(), workflows));
    }
    return adapter;
  }

  protected ObjectName createAdapterObjectName(String uid) throws Exception {
    return ObjectName.getInstance(JMX_ADAPTER_TYPE + ID_PREFIX + uid);
  }

  protected Channel createChannel(String uid, int workflows) throws CoreException {
    Channel c = new Channel();
    c.setUniqueId(uid);
    for (int i = 0; i < workflows; i++) {
      c.getWorkflowList().add(createWorkflow(guid.safeUUID()));
    }
    return c;
  }

  protected Channel createChannel(String uid) throws CoreException {
    return createChannel(uid, 0);
  }

  protected StandardWorkflow createWorkflow(String uid) throws CoreException {
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId(uid);
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

  protected String createErrorMessageString(ComponentState parentState, ComponentState futureChildState) {
    return TRANSITION_CHECK_ERROR_MSG_HEAD + "[" + parentState + "]" + TRANSITION_CHECK_ERROR_MSG_TAIL + "[" + futureChildState
        + "]";

  }

  <T> T getFirstChildMBean(ParentComponent parent, Class<T> name) throws Exception {
    ArrayList<ObjectName> children = new ArrayList<ObjectName>(parent.getChildren());
    return JMX.newMBeanProxy(mBeanServer, children.get(0), name);
  }

}
