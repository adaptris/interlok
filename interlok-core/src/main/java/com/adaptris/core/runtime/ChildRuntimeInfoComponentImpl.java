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

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.JmxHelper;

public abstract class ChildRuntimeInfoComponentImpl implements ChildRuntimeInfoComponent {

  private transient ObjectName myObjectName;
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  protected ChildRuntimeInfoComponentImpl() {
  }

  protected abstract String getType();

  protected abstract String uniqueId();

  private ObjectName initMyObjectName() throws MalformedObjectNameException {
    String parentHierarchy = ((HierarchicalMBean) getParentRuntimeInfoComponent()).createObjectHierarchyString();
    return ObjectName.getInstance(getType() + parentHierarchy + ID_PREFIX + uniqueId());
  }

  @Override
  public ObjectName getParentObjectName() throws MalformedObjectNameException {
    return ((BaseComponentMBean) getParentRuntimeInfoComponent()).createObjectName();
  }

  @Override
  public String getParentId() {
    return ((AdapterComponentMBean) getParentRuntimeInfoComponent()).getUniqueId();
  }

  @Override
  public ObjectName createObjectName() throws MalformedObjectNameException {
    if (myObjectName == null) {
      myObjectName = initMyObjectName();
    }
    return myObjectName;
  }

  @Override
  public void registerMBean() throws CoreException {
    try {
      ObjectName objName = createObjectName();
      log.trace("Registering {} against {}", this.getClass().getName(), objName);
      JmxHelper.register(objName, this);
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
