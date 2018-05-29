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

import com.adaptris.core.runtime.ChildRuntimeInfoComponentImpl;
import com.adaptris.core.runtime.ConsumerMonitorImpl;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.WorkflowManager;

public class FsConsumerMonitor extends ConsumerMonitorImpl<FsConsumerImpl> implements FsConsumerMonitorMBean {

  public FsConsumerMonitor(WorkflowManager owner, FsConsumerImpl consumer) {
    super(owner, consumer);
  }

  @Override
  protected String getType() {
    return JMX_FS_MONITOR_TYPE;
  }

  @Override
  public int messagesRemaining() {
    int remaining = -1;
    try {
      remaining = getWrappedComponent().filesRemaining();
    }
    catch (Exception e) {
    }
    return remaining;
  }

  @Override
  @Deprecated
  public int filesRemaining() {
    return messagesRemaining();
  }
}
