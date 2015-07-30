package com.adaptris.core.interceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.BaseCase;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.core.util.JmxHelper;

public abstract class MessageNotificationCase extends BaseCase {
  protected MBeanServer mBeanServer;
  protected List<ObjectName> registeredObjects;

  public MessageNotificationCase(String name) {
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
    adapter.registerLicense(new LicenseStub());
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
