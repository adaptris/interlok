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

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_FS_MONITOR_TYPE;

import javax.management.MalformedObjectNameException;

import com.adaptris.core.runtime.ChildRuntimeInfoComponentImpl;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.WorkflowManager;

public class FsConsumerMonitor extends ChildRuntimeInfoComponentImpl implements FsConsumerMonitorMBean {
  private transient WorkflowManager parent;
  private transient FsConsumerImpl wrappedComponent;

  private FsConsumerMonitor() {
    super();
  }

  FsConsumerMonitor(WorkflowManager owner, FsConsumerImpl fs) throws MalformedObjectNameException {
    parent = owner;
    wrappedComponent = fs;
  }

  @Override
  protected String getType() {
    return JMX_FS_MONITOR_TYPE;
  }

  @Override
  protected String uniqueId() {
    return wrappedComponent.getUniqueId();
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

}
