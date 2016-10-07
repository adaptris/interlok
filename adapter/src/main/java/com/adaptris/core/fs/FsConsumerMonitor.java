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
package com.adaptris.core.fs;

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_FS_MONITOR_TYPE;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.ChildRuntimeInfoComponent;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.core.util.JmxHelper;

public class FsConsumerMonitor implements FsConsumerMonitorMBean, ChildRuntimeInfoComponent {
  private transient WorkflowManager parent;
  private transient FsConsumerImpl wrappedComponent;
  private transient ObjectName myObjectName = null;

  protected FsConsumerMonitor(WorkflowManager owner, FsConsumerImpl fs) throws MalformedObjectNameException {
    parent = owner;
    wrappedComponent = fs;
    initMembers();
  }

  private void initMembers() throws MalformedObjectNameException {
    myObjectName = ObjectName
        .getInstance(JMX_FS_MONITOR_TYPE + parent.createObjectHierarchyString() + ID_PREFIX + wrappedComponent.getUniqueId());
  }

  @Override
  public int filesRemaining() {
    int remaining = -1;
    try {
      remaining = wrappedComponent.filesRemaining();
    }
    catch (Exception e) {
    }
    return remaining;
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
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void unregisterMBean() throws CoreException {
    try {
      JmxHelper.unregister(createObjectName());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }


}
