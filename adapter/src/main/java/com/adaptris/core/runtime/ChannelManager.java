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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanNotificationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.core.Channel;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.Workflow;

/**
 * Base implementation of {@link ChannelManagerMBean}.
 */
public class ChannelManager extends ComponentManagerImpl<Channel> implements ChannelManagerMBean, ChannelRuntimeManager {

  private transient Channel channel;
  private transient AdapterManager parent;
  private transient Set<WorkflowRuntimeManager> workflowManagers;
  private transient ObjectName myObjectName = null;

  private ChannelManager() {
    super();
    workflowManagers = new HashSet<WorkflowRuntimeManager>();
  }

  public ChannelManager(Channel c, AdapterManager owner) throws MalformedObjectNameException, CoreException {
    this(c, owner, false);
  }

  ChannelManager(Channel c, AdapterManager owner, boolean skipBackRef) throws MalformedObjectNameException, CoreException {
    this();
    channel = c;
    parent = owner;
    initMembers();
    if (!skipBackRef) {
      parent.addChild(this);
    }
  }

  private void initMembers() throws MalformedObjectNameException, CoreException {
    if (isEmpty(channel.getUniqueId())) {
      throw new CoreException("No UniqueID, this channel cannot be managed");
    }
    // Builds up a name com.adaptris:type=Channel, adapter=<adapter-id,>, id=<channel-id>
    myObjectName = ObjectName.getInstance(JMX_CHANNEL_TYPE + ADAPTER_PREFIX + parent.getUniqueId() + ID_PREFIX
        + getWrappedComponent().getUniqueId());
    for (Workflow c : channel.getWorkflowList()) {
      if (!isEmpty(c.getUniqueId())) {
        addChild(new WorkflowManager(c, this, true), true);
      }
    }
    marshalConfig();
  }

  @Override
  public ObjectName createObjectName() throws MalformedObjectNameException {
    return myObjectName;
  }

  @Override
  public long requestStartTime() {
    Date startTime = getWrappedComponent().lastStartTime();
    if (startTime != null) {
      return startTime.getTime();
    }
    return 0;
  }

  @Override
  public long requestStopTime() {
    // Should never be null, unlike lastStartTime()
    return getWrappedComponent().lastStopTime().getTime();
  }

  @Override
  public Collection<ObjectName> getChildren() throws MalformedObjectNameException {
    Set<ObjectName> result = new TreeSet<ObjectName>();
    for (WorkflowRuntimeManager wmb : workflowManagers) {
      result.add(wmb.createObjectName());
    }
    return result;
  }

  @Override
  public boolean addChild(WorkflowRuntimeManager cmb) throws CoreException {
    return addChild(cmb, false);
  }

  private boolean addChild(WorkflowRuntimeManager cmb, boolean ignoreActualWorkflow) throws CoreException {
    if (cmb == null) {
      throw new IllegalArgumentException("Null WorkflowManagerMBean");
    }
    if (!ignoreActualWorkflow) {
      getWrappedComponent().getWorkflowList().add(cmb.getWrappedComponent());
      marshalAndSendNotification();
    }
    return workflowManagers.add(cmb);
  }

  private boolean removeWorkflowManager(WorkflowRuntimeManager cmb, boolean unregisterJmx) throws CoreException {
    if (cmb == null) {
      throw new IllegalArgumentException("Null WorkflowManagerMBean");
    }
    int removed = 0;
    removed += getWrappedComponent().getWorkflowList().remove(cmb.getWrappedComponent()) ? 1 : 0;
    marshalAndSendNotification();

    getParent().childUpdated();
    if (workflowManagers.remove(cmb)) {
      if (unregisterJmx) {
        cmb.unregisterMBean();
      }
      removed++;
    }
    return removed == 2;
  }

  @Override
  public boolean removeChild(WorkflowRuntimeManager cmb) throws CoreException {
    return removeWorkflowManager(cmb, false);
  }

  @Override
  public boolean addChildren(Collection<WorkflowRuntimeManager> coll) throws CoreException {
    throw new UnsupportedOperationException();
    // int i = 0;
    // for (WorkflowManagerMBean bean : coll) {
    // i += addChild(bean) ? 1 : 0;
    // }
    // return i > 0;
  }

  @Override
  public boolean removeChildren(Collection<WorkflowRuntimeManager> coll) throws CoreException {
    int i = 0;
    for (WorkflowRuntimeManager bean : coll) {
      i += removeChild(bean) ? 1 : 0;
    }
    return i > 0;
  }

  @Override
  public Channel getWrappedComponent() {
    return channel;
  }

  @Override
  public AdapterManager getParent() {
    return parent;
  }

  /**
   * Equality is based on the underlying ObjectName.
   *
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    try {
      if (o instanceof ChannelManagerMBean) {
        ChannelManagerMBean rhs = (ChannelManagerMBean) o;
        return new EqualsBuilder().append(myObjectName, rhs.createObjectName()).isEquals();
      }
    }
    catch (MalformedObjectNameException e) {
    }
    return false;
  }

  @Override
  public String toString() {
    return myObjectName.toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 23).append(myObjectName).toHashCode();
  }

  @Override
  protected void checkTransitionTo(ComponentState futureState) throws CoreException {
    ParentStateValidator.checkTransitionTo(futureState, parent.getComponentState());
  }

  @Override
  public Collection<BaseComponentMBean> getAllDescendants() {
    Set<BaseComponentMBean> result = new HashSet<BaseComponentMBean>();
    for (WorkflowRuntimeManager c : workflowManagers) {
      result.add(c);
      result.addAll(c.getAllDescendants());
    }
    return result;
  }

  @Override
  public String getParentId() {
    return parent.getUniqueId();
  }

  @Override
  public ObjectName getParentObjectName() throws MalformedObjectNameException {
    return parent.createObjectName();
  }

  @Override
  public String createObjectHierarchyString() {
    return getParent().createObjectHierarchyString() + CHANNEL_PREFIX + getWrappedComponent().getUniqueId();
  }

  @Override
  public Collection<ObjectName> getChildRuntimeInfoComponents() throws MalformedObjectNameException {
    return new ArrayList<ObjectName>();
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
  public void registerMBean() throws CoreException {
    registerSelf();
    for (WorkflowRuntimeManager c : workflowManagers) {
      c.registerMBean();
    }
    // for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
    // cmb.registerMBean();
    // }
  }

  @Override
  public void unregisterMBean() throws CoreException {
    unregisterSelf();
    for (WorkflowRuntimeManager c : workflowManagers) {
      c.unregisterMBean();
    }
    // for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
    // cmb.unregisterMBean();
    // }
  }

  @Override
  public ObjectName addWorkflow(String xml) throws CoreException, IllegalStateException, MalformedObjectNameException {
    ensureState(ClosedState.getInstance());
    Workflow newWorkflow = (Workflow) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml);
    WorkflowManager workflowManager = new WorkflowManager(newWorkflow, this);
    newWorkflow.isEnabled(getParent().getWrappedComponent().currentLicense());
    // We don't need to "store" the XML at this point, as channelManager will eventually call
    // addChild() and that's when that happens.
    // marshalAndSendNotification();
    getParent().childUpdated();
    workflowManager.registerMBean();
    return workflowManager.createObjectName();
  }

  @Override
  public boolean removeWorkflow(String id) throws CoreException, IllegalStateException, MalformedObjectNameException {
    boolean removed = false;
    ensureState(ClosedState.getInstance());
    if (!isEmpty(id)) {
      WorkflowRuntimeManager toBeRemoved = findWorkflowManager(id);
      if (toBeRemoved != null) {
        removed = removeWorkflowManager(toBeRemoved, true);
      }
    }
    return removed;
  }

  private WorkflowRuntimeManager findWorkflowManager(String id) throws CoreException, MalformedObjectNameException {
    WorkflowRuntimeManager found = null;
    for (WorkflowRuntimeManager mgr : workflowManagers) {
      if (id.equals(mgr.getUniqueId())) {
        found = mgr;
        break;
      }
    }
    return found;
  }

  @Override
  public void childUpdated() throws CoreException {
    marshalAndSendNotification();

  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    MBeanNotificationInfo adapterLifecycle = new MBeanNotificationInfo(new String[]
    {
        NOTIF_TYPE_CHANNEL_LIFECYCLE, NOTIF_TYPE_CHANNEL_CONFIG
    }, Notification.class.getName(), "Channel Notifications");

    return new MBeanNotificationInfo[]
    {
      adapterLifecycle
    };
  }

  @Override
  protected String getNotificationType(ComponentNotificationType type) {
    String result = null;
    switch (type) {
    case LIFECYCLE: {
      result = NOTIF_TYPE_CHANNEL_LIFECYCLE;
      break;
    }
    case CONFIG: {
      result = NOTIF_TYPE_CHANNEL_CONFIG;
      break;
    }
    default:
    }
    return result;
  }
}
