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
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.naming.Context;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.Channel;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.FailedMessageRetrier;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.JndiContextFactory;
import com.adaptris.core.ProcessingExceptionHandler;
import com.adaptris.core.Service;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.management.VersionReport;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.ManagedThreadFactory;

/**
 * Base implementation of {@link AdapterManagerMBean}.
 */
public class AdapterManager extends ComponentManagerImpl<Adapter> implements AdapterManagerMBean, AdapterRuntimeManager {
  private transient Properties contextEnv = new Properties();
  private transient Adapter adapter;
  private transient Set<ChannelRuntimeManager> channelManagers;
  private transient Set<ChildRuntimeInfoComponent> childRuntimeInfoComponents;

  private transient ObjectName myObjectName = null;
  private String adapterBuildVersion;
  private List<String> adapterModules = new ArrayList<String>();

  private AdapterManager() {
    super();
    channelManagers = new HashSet<ChannelRuntimeManager>();
    childRuntimeInfoComponents = new HashSet<ChildRuntimeInfoComponent>();
    contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
  }

  public AdapterManager(Adapter owner) throws MalformedObjectNameException, CoreException {
    this();
    adapter = owner;
    initRefs();
    initVersionInfo();
  }

  private void initRefs() throws MalformedObjectNameException, CoreException {
    myObjectName = ObjectName.getInstance(JMX_ADAPTER_TYPE + ID_PREFIX + getWrappedComponent().getUniqueId());
    addChildJmxComponent(new AdapterComponentChecker(this));
    for (Channel c : adapter.getChannelList()) {
      if (c.hasUniqueId()) {
        addChild(new ChannelManager(c, this, true), true);
      }
    }
    registerChildRuntime(adapter.getMessageErrorDigester());
    registerChildRuntime(adapter.getFailedMessageRetrier());
    registerChildRuntime(adapter.getMessageErrorHandler());
    registerChildRuntime(adapter.logHandler());
    marshalConfig();
  }

  private void initVersionInfo() {
    VersionReport v = VersionReport.getInstance();
    adapterBuildVersion = v.getAdapterBuildVersion();
    for (String s : v.getReport()) {
      adapterModules.add(s);
    }
  }

  private void registerChildRuntime(AdaptrisComponent c) throws CoreException {
    ChildRuntimeInfoComponent comp = (ChildRuntimeInfoComponent) RuntimeInfoComponentFactory.create(this, c);
    if (comp != null) {
      addChildJmxComponent(comp);
    }
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
    for (ChannelRuntimeManager cmb : channelManagers) {
      result.add(cmb.createObjectName());
    }
    return result;
  }

  @Override
  public boolean addChild(ChannelRuntimeManager cmb) throws CoreException {
    return addChild(cmb, false);
  }

  private boolean addChild(ChannelRuntimeManager cmb, boolean ignoreActualChannel) throws CoreException {
    if (cmb == null) {
      throw new IllegalArgumentException("Null ChannelManagerBean");
    }
    if (!ignoreActualChannel) {
      getWrappedComponent().getChannelList().add(cmb.getWrappedComponent());
      marshalAndSendNotification();

    }
    return channelManagers.add(cmb);

  }

  private boolean removeChannelManager(ChannelRuntimeManager cmb, boolean unregisterJmx) throws CoreException {
    if (cmb == null) {
      throw new IllegalArgumentException("Null ChannelManagerBean");
    }
    int removed = 0;
    Channel c = cmb.getWrappedComponent();
    closeQuietly(c);
    removed += getWrappedComponent().getChannelList().remove(c) ? 1 : 0;
    if (removed == 1) marshalAndSendNotification();
    if (channelManagers.remove(cmb)) {
      if (unregisterJmx) {
        cmb.unregisterMBean();
      }
      removed++;
    }
    return removed == 2;
  }

  @Override
  public boolean removeChild(ChannelRuntimeManager cmb) throws CoreException {
    return removeChannelManager(cmb, false);
  }

  @Override
  public boolean addChildren(Collection<ChannelRuntimeManager> coll) throws CoreException {
    throw new UnsupportedOperationException();
    // int i = 0;
    // for (ChannelManager bean : coll) {
    // i += addChild(bean) ? 1 : 0;
    // }
    // return i > 0;
  }

  @Override
  public boolean removeChildren(Collection<ChannelRuntimeManager> coll) throws CoreException {
    int i = 0;
    for (ChannelRuntimeManager bean : coll) {
      i += removeChild(bean) ? 1 : 0;
    }
    return i > 0;
  }

  @Override
  public Adapter getWrappedComponent() {
    return adapter;
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
      if (o instanceof AdapterManagerMBean) {
        AdapterManagerMBean rhs = (AdapterManagerMBean) o;
        return new EqualsBuilder().append(myObjectName, rhs.createObjectName()).isEquals();
      }
    }
    catch (MalformedObjectNameException ignored) {

    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 17).append(myObjectName).toHashCode();
  }

  @Override
  public String toString() {
    return myObjectName.toString();
  }

  @Override
  protected void checkTransitionTo(ComponentState futureState) throws CoreException {
    // Do nothing, we don't have a parent to check.
  }

  @Override
  public Collection<BaseComponentMBean> getAllDescendants() {
    Set<BaseComponentMBean> result = new HashSet<BaseComponentMBean>();
    for (ChannelRuntimeManager c : channelManagers) {
      result.add(c);
      result.addAll(c.getAllDescendants());
    }
    for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
      result.add(cmb);
    }
    return result;
  }

  @Override
  public Collection<ObjectName> getChildRuntimeInfoComponents() throws MalformedObjectNameException {
    Set<ObjectName> result = new TreeSet<ObjectName>();
    for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
      result.add(cmb.createObjectName());
    }
    return result;
  }

  @Override
  public boolean addChildJmxComponent(ChildRuntimeInfoComponent comp) {
    if (comp == null) {
      throw new IllegalArgumentException("Null ChildRuntimeInfoComponent");
    }
    return childRuntimeInfoComponents.add(comp);
  }

  @Override
  public boolean removeChildJmxComponent(ChildRuntimeInfoComponent comp) {
    if (comp == null) {
      throw new IllegalArgumentException("Null ChildRuntimeInfoComponent");
    }
    return childRuntimeInfoComponents.remove(comp);
  }

  @Override
  public String createObjectHierarchyString() {
    return ADAPTER_PREFIX + getWrappedComponent().getUniqueId();
  }

  @Override
  public boolean addSharedConnection(String xmlString) throws CoreException, IllegalArgumentException {
    ensureState(ClosedState.getInstance());
    AdaptrisConnection comp = (AdaptrisConnection) DefaultMarshaller.getDefaultMarshaller().unmarshal(xmlString);
    boolean result = getWrappedComponent().getSharedComponents().addConnection(comp);
    if (result) {
      marshalAndSendNotification();
    }
    return result;
  }

  @Override
  public boolean addAndBindSharedConnection(String xmlString) throws CoreException, IllegalStateException {
    ensureState(StartedState.getInstance(), InitialisedState.getInstance(), StoppedState.getInstance());
    AdaptrisConnection comp = (AdaptrisConnection) DefaultMarshaller.getDefaultMarshaller().unmarshal(xmlString);
    boolean result = getWrappedComponent().getSharedComponents().addConnection(comp);
    if (result) {
      getWrappedComponent().getSharedComponents().bindJNDI(comp.getUniqueId());
      marshalAndSendNotification();
    }
    return result;
  }

  @Override
  public boolean removeSharedConnection(String connectionId) throws CoreException, IllegalStateException {
    ensureState(ClosedState.getInstance());
    Collection<AdaptrisConnection> c = getWrappedComponent().getSharedComponents().removeConnection(connectionId);
    boolean result = c.size() > 0;
    if (result) {
      marshalAndSendNotification();
    }
    return result;
  }

  @Override
  public boolean containsSharedConnection(String connectionId) throws CoreException {
    return getWrappedComponent().getSharedComponents().containsConnection(connectionId);
  }

  @Override
  public Collection<String> getSharedConnectionIds() throws CoreException {
    return getWrappedComponent().getSharedComponents().getConnectionIds();
  }

  @Override
  public boolean addSharedService(String xmlString) throws CoreException, IllegalStateException, IllegalArgumentException {
    ensureState(ClosedState.getInstance());
    Service comp = (Service) DefaultMarshaller.getDefaultMarshaller().unmarshal(xmlString);
    boolean result = getWrappedComponent().getSharedComponents().addService(comp);
    if (result) {
      marshalAndSendNotification();
    }
    return result;
  }

  @Override
  public boolean addAndBindSharedService(String xmlString) throws CoreException, IllegalStateException, IllegalArgumentException {
    ensureState(StartedState.getInstance(), InitialisedState.getInstance(), StoppedState.getInstance());
    Service comp = (Service) DefaultMarshaller.getDefaultMarshaller().unmarshal(xmlString);
    boolean result = getWrappedComponent().getSharedComponents().addService(comp);
    if (result) {
      getWrappedComponent().getSharedComponents().bindJNDI(comp.getUniqueId());
      marshalAndSendNotification();
    }
    return result;
  }

  @Override
  public boolean removeSharedService(String serviceId) throws CoreException, IllegalStateException {
    ensureState(ClosedState.getInstance());
    Collection<Service> c = getWrappedComponent().getSharedComponents().removeService(serviceId);
    boolean result = c.size() > 0;
    if (result) {
      marshalAndSendNotification();
    }
    return result;
  }

  @Override
  public boolean containsSharedService(String serviceId) throws CoreException {
    return getWrappedComponent().getSharedComponents().containsService(serviceId);
  }

  @Override
  public Collection<String> getSharedServiceIds() throws CoreException {
    return getWrappedComponent().getSharedComponents().getServiceIds();
  }

  @Override
  public boolean removeSharedComponent(String id) throws CoreException, IllegalStateException {
    return removeSharedConnection(id) || removeSharedService(id);
  }

  @Override
  public ObjectName addChannel(String xml) throws CoreException, MalformedObjectNameException {
    // ensureState(ClosedState.getInstance());
    Channel newChannel = (Channel) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml);
    ChannelManager channelManager = new ChannelManager(newChannel, this);
    // We don't need to "store" the XML at this point, as channelManager will eventually call
    // addChild() and that's when that happens.
    channelManager.registerMBean();
    return channelManager.createObjectName();
  }

  private ChannelRuntimeManager findChannelManager(String id) {
    ChannelRuntimeManager found = null;
    MBeanServer server = JmxHelper.findMBeanServer();
    for (ChannelRuntimeManager mgr : channelManagers) {
      if (id.equals(mgr.getUniqueId())) {
        found = mgr;
        break;
      }
    }
    return found;
  }

  @Override
  public boolean removeChannel(String id) throws CoreException, MalformedObjectNameException {
    boolean removed = false;
    // ensureState(ClosedState.getInstance());
    if (!isEmpty(id)) {
      ChannelRuntimeManager toBeRemoved = findChannelManager(id);
      if (toBeRemoved != null) {
        removed = removeChannelManager(toBeRemoved, true);
      }
    }
    return removed;
  }

  @Override
  public void setMessageErrorHandler(String xml) throws CoreException {
    ensureState(ClosedState.getInstance());
    ProcessingExceptionHandler obj = (ProcessingExceptionHandler) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml);
    obj.prepare();
    getWrappedComponent().setMessageErrorHandler(obj);
    registerChildRuntime(obj);
    marshalAndSendNotification();

  }

  @Override
  public void setFailedMessageRetrier(String xml) throws CoreException {
    ensureState(ClosedState.getInstance());
    FailedMessageRetrier obj = (FailedMessageRetrier) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml);
    obj.prepare();
    getWrappedComponent().setFailedMessageRetrier(obj);
    registerChildRuntime(obj);
    marshalAndSendNotification();

  }

  @Override
  public void registerMBean() throws CoreException {
    registerSelf();
    for (ChannelRuntimeManager c : channelManagers) {
      c.registerMBean();
    }
    for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
      cmb.registerMBean();
    }
  }

  @Override
  public void unregisterMBean() throws CoreException {
    unregisterSelf();
    for (ChannelRuntimeManager c : channelManagers) {
      c.unregisterMBean();
    }
    for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
      cmb.unregisterMBean();
    }
  }

  @Override
  public void childUpdated() throws CoreException {
    marshalAndSendNotification();

  }

  @Override
  public String getAdapterBuildVersion() {
    return adapterBuildVersion;
  }

  @Override
  public List<String> getModuleVersions() {
    return new ArrayList<String>(adapterModules);
  }

  @Override
  public void sendLifecycleEvent(AdapterLifecycleEvent event) throws CoreException {
    event.setAdapterUniqueId(adapter.getUniqueId());
    if (StartedState.getInstance().equals(adapter.getEventHandler().retrieveComponentState())) {
      adapter.getEventHandler().send(event);
    }
    else {
      log.info("Event Handler stopped; ignoring request to send {}", event.getClass().getSimpleName());
    }
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    MBeanNotificationInfo adapterLifecycle = new MBeanNotificationInfo(new String[]
    {
        NOTIF_TYPE_ADAPTER_LIFECYCLE, NOTIF_TYPE_ADAPTER_CONFIG
    }, Notification.class.getName(), "Adapter Notifications");

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
      result = NOTIF_TYPE_ADAPTER_LIFECYCLE;
      break;
    }
    case CONFIG: {
      result = NOTIF_TYPE_ADAPTER_CONFIG;
      break;
    }
    default:
    }
    return result;
  }

  @Override
  public void forceClose() throws CoreException {
    sendNotification(createLifecycleNotification(NOTIF_MSG_FORCE_CLOSE, myObjectName));
    log.trace("Force Close on {}", myObjectName);
    // TODO : This is probably quite bad, but once we move to having our own class loader each adapter should have
    // their own static instance of MTF so we aren't interrupting all threads globally...
    ManagedThreadFactory.interruptManagedThreads();
    super.requestClose();
  }
}
