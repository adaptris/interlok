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
package com.adaptris.core;

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_RETRY_MONITOR_TYPE;

import java.util.Collection;

import com.adaptris.core.runtime.ChildRuntimeInfoComponentImpl;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;

public class RetryMessageErrorHandlerMonitor extends ChildRuntimeInfoComponentImpl implements RetryMessageErrorHandlerMonitorMBean {
  private transient ParentRuntimeInfoComponent parent;
  private transient RetryMessageErrorHandlerImp wrappedComponent;

  private RetryMessageErrorHandlerMonitor() {
    super();
  }

  RetryMessageErrorHandlerMonitor(ParentRuntimeInfoComponent owner, RetryMessageErrorHandlerImp retrier) {
    this();
    parent = owner;
    wrappedComponent = retrier;
  }

  @Override
  protected String getType() {
    return JMX_RETRY_MONITOR_TYPE;
  }

  @Override
  protected String uniqueId() {
    return wrappedComponent.getUniqueId();
  }

  @Override
  public ParentRuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public void failAllMessages(boolean failFuture) {
    wrappedComponent.failAllMessages();
    wrappedComponent.failFutureMessages(failFuture);
  }

  @Override
  public Collection<String> waitingForRetry() {
    return wrappedComponent.waitingForRetry();
  }

  @Override
  public void failMessage(String s) {
    wrappedComponent.failMessage(s);
  }

}
