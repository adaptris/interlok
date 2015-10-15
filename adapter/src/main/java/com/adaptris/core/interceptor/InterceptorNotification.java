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

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_NOTIFIER_TYPE;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanNotificationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.ChildRuntimeInfoComponent;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.ManagedThreadFactory;

public class InterceptorNotification extends NotificationBroadcasterSupport implements
    InterceptorNotificationMBean, ChildRuntimeInfoComponent {

  private transient WorkflowManager parent;
  private transient NotifyingInterceptor wrappedComponent;
  private transient ObjectName myObjectName = null;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  private transient AtomicInteger sequenceNumber = new AtomicInteger();

  protected InterceptorNotification(WorkflowManager owner, NotifyingInterceptor interceptor)
      throws MalformedObjectNameException {
    super(Executors.newCachedThreadPool(new ManagedThreadFactory()));
    parent = owner;
    wrappedComponent = interceptor;
    initMembers();
  }

  private void initMembers() throws MalformedObjectNameException {
    // Builds up a name
    // com.adaptris:type=Metrics,adapter=<adapter-id,>,channel=<channel-id>,workflow=<workflow-id>,id=myid
    myObjectName =
        ObjectName.getInstance(JMX_NOTIFIER_TYPE + parent.createObjectHierarchyString() + ID_PREFIX
            + wrappedComponent.getUniqueId());
    wrappedComponent.registerNotificationEmitter(this);
  }

  @Override
  public ParentRuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public ObjectName getParentObjectName() throws MalformedObjectNameException {
    return parent.createObjectName();
  }

  @Override
  public String getParentId() {
    return parent.getUniqueId();
  }

  @Override
  public ObjectName createObjectName() throws MalformedObjectNameException {
    return myObjectName;
  }

  @Override
  public void registerMBean() throws CoreException {
    try {
      JmxHelper.register(createObjectName(), this);
    } catch (Exception e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void unregisterMBean() throws CoreException {
    try {
      JmxHelper.unregister(createObjectName());
    } catch (Exception e) {
      throw new CoreException(e);
    }
  }


  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    MBeanNotificationInfo myNotification =
        new MBeanNotificationInfo(new String[] {NOTIF_TYPE_INTERCEPTOR,},
            Notification.class.getName(), "Interceptor Notifications");
    return new MBeanNotificationInfo[] {myNotification};
  }

  @Override
  public void sendNotification(String message, Properties userData) {
    try {
      Notification n =
          new Notification(NOTIF_TYPE_INTERCEPTOR, createObjectName(),
              sequenceNumber.getAndIncrement(), message);
      n.setUserData(userData);
      super.sendNotification(n);
    } catch (MalformedObjectNameException ignored) {
      // We're registered now, createObjectName shouldn't be throwing anymore Exceptions.
    }
  }

}
