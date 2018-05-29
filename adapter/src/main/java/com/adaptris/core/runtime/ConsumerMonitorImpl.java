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

import com.adaptris.core.AdaptrisMessageConsumer;

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_CONSUMER_MONITOR_TYPE;

public abstract class ConsumerMonitorImpl<T extends AdaptrisMessageConsumer> extends ChildRuntimeInfoComponentImpl implements ConsumerMonitorMBean {

  private transient WorkflowManager parent;
  private transient T wrappedComponent;

  private ConsumerMonitorImpl(){
    super();
  }

  public ConsumerMonitorImpl(WorkflowManager owner, T consumer){
    this.parent = owner;
    this.wrappedComponent = consumer;
  }

  @Override
  protected String getType() {
    return JMX_CONSUMER_MONITOR_TYPE;
  }

  @Override
  protected String uniqueId() {
    return wrappedComponent.getUniqueId();
  }

  public T getWrappedComponent(){
    return this.wrappedComponent;
  }

  @Override
  public ParentRuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }
}
