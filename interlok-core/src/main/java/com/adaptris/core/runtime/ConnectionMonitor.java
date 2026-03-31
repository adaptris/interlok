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

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_CONNECTION_TYPE;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.ComponentState;

public class ConnectionMonitor extends ChildRuntimeInfoComponentImpl implements ConnectionMonitorMBean {

  private transient ParentRuntimeInfoComponent parent;
  private transient AdaptrisConnection wrappedComponent;
  private transient String objectNameId;

  private ConnectionMonitor() {
    super();
  }

  public ConnectionMonitor(ParentRuntimeInfoComponent owner, AdaptrisConnection component) {
    this();
    parent = owner;
    wrappedComponent = component;
    objectNameId = wrappedComponent.getUniqueId();
  }

  @Override
  protected String getType() {
    return JMX_CONNECTION_TYPE;
  }

  @Override
  protected String uniqueId() {
    return objectNameId;
  }

  @Override
  public ParentRuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public ComponentState getComponentState() {
    return wrappedComponent.retrieveComponentState();
  }

  @Override
  public String getUniqueId() {
    return wrappedComponent.getUniqueId();
  }

  String connectionId() {
    return wrappedComponent.getUniqueId();
  }

  void appendObjectNameSuffix(String suffix) {
    objectNameId = wrappedComponent.getUniqueId() + suffix;
  }
}
