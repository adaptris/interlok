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

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_INFLIGHT_TYPE;

import com.adaptris.core.runtime.ChildRuntimeInfoComponentImpl;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.WorkflowManager;

public class MessageInFlight extends ChildRuntimeInfoComponentImpl implements MessageInFlightMBean {

  private transient WorkflowManager parent;
  private transient InFlightWorkflowInterceptor wrappedComponent;

  private MessageInFlight() {
    super();
  }

  protected MessageInFlight(WorkflowManager owner, InFlightWorkflowInterceptor interceptor) {
    parent = owner;
    wrappedComponent = interceptor;
  }

  
  @Override
  protected String getType() {
    return JMX_INFLIGHT_TYPE;
  }

  @Override
  protected String uniqueId() {
    return wrappedComponent.getUniqueId();
  }


  @Override
  public boolean messagesInFlight() {
    return wrappedComponent.messagesInFlightCount() > 0;
  }

  @Override
  public int messagesPendingCount() {
    return wrappedComponent.messagesPendingCount();
  }

  @Override
  public int messagesInFlightCount() {
    return wrappedComponent.messagesInFlightCount();
  }


  @Override
  public ParentRuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

}
