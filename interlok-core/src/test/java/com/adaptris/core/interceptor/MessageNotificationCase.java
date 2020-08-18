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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.junit.After;
import org.junit.Before;
import com.adaptris.core.Adapter;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public abstract class MessageNotificationCase extends BaseCase {
  protected MBeanServer mBeanServer;
  protected List<ObjectName> registeredObjects;


  @Before
  public void setUp() throws Exception {
    mBeanServer = JmxHelper.findMBeanServer();
    registeredObjects = new ArrayList<ObjectName>();
  }

  @After
  public void tearDown() throws Exception {
    for (ObjectName bean : registeredObjects) {
      if (mBeanServer.isRegistered(bean)) {
        mBeanServer.unregisterMBean(bean);
        log.trace(bean + " unregistered");
      }
    }
  }


  protected BaseComponentMBean getFirstImpl(List<BaseComponentMBean> mBeans, Class<?> c) {
    BaseComponentMBean result = null;
    for (BaseComponentMBean m : mBeans) {
      if (c.isAssignableFrom(m.getClass())) {
        result = m;
        break;
      }
    }
    return result;
  }

  protected StandardWorkflow createWorkflow(String uid, WorkflowInterceptor... interceptors)
      throws CoreException {
    StandardWorkflow wf = new StandardWorkflow();
    wf.setUniqueId(uid);
    for (WorkflowInterceptor wi : interceptors) {
      wf.addInterceptor(wi);
    }
    return wf;
  }

  protected Adapter createAdapter(String uid, Workflow... workflows) throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(uid);
    adapter.getChannelList().add(createChannel(uid + "_Channel", workflows));
    return adapter;
  }

  protected Channel createChannel(String uid, Workflow... workflows) {
    Channel c = new Channel();
    c.setUniqueId(uid);
    for (Workflow w : workflows) {
      c.getWorkflowList().add(w);
    }
    return c;
  }

  protected void register(Collection<BaseComponentMBean> mBeans) throws Exception {
    for (BaseComponentMBean bean : mBeans) {
      ObjectName name = bean.createObjectName();
      registeredObjects.add(name);
      mBeanServer.registerMBean(bean, name);
    }
  }

  protected List<BaseComponentMBean> createJmxManagers(Adapter adapter) throws Exception {
    List<BaseComponentMBean> result = new ArrayList<BaseComponentMBean>();
    AdapterManager am = new AdapterManager(adapter);
    result.add(am);
    result.addAll(am.getAllDescendants());
    return result;
  }
}
