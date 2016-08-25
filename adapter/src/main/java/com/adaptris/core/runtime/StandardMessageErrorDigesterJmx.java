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
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_MSG_ERR_DIGESTER_TYPE;

import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.JmxHelper;

/**
 * Exposes all the records handled by {@link StandardMessageErrorDigester} and exposes them via JMX.
 *
 *
 * @author lchan
 *
 */
public class StandardMessageErrorDigesterJmx implements StandardMessageErrorDigesterJmxMBean, ChildRuntimeInfoComponent {

  private transient AdapterManager parent;
  private transient StandardMessageErrorDigester wrappedComponent;
  private transient ObjectName myObjectName = null;

  private StandardMessageErrorDigesterJmx() {

  }

  StandardMessageErrorDigesterJmx(AdapterManager owner, StandardMessageErrorDigester component) throws MalformedObjectNameException {
    this();
    parent = owner;
    wrappedComponent = component;
    initMembers();
  }

  private void initMembers() throws MalformedObjectNameException {
    // Builds up a name com.adaptris:type=MessageErrorDigest,adapter=<adapter-id,>,id=<this-id>
    myObjectName = ObjectName.getInstance(JMX_MSG_ERR_DIGESTER_TYPE + parent.createObjectHierarchyString() + ID_PREFIX
        + wrappedComponent.getUniqueId());
  }

  @Override
  public ObjectName createObjectName() throws MalformedObjectNameException {
    return myObjectName;
  }

  @Override
  public MessageErrorDigest getDigest() {
    return wrappedComponent.getDigest();
  }

  @Override
  public MessageErrorDigest getDigestSubset(int fromIndex) {
    return getDigestSubset(fromIndex, wrappedComponent.getDigest().size());
  }

  @Override
  public MessageErrorDigest getDigestSubset(int fromIndex, int toIndex) {
    List<MessageDigestErrorEntry> newList = wrappedComponent.getDigest().subList(fromIndex, toIndex);
    return new MessageErrorDigest(newList.size(), newList);
  }

  @Override
  public int getTotalErrorCount() {
    return wrappedComponent.getTotalErrorCount();
  }

  @Override
  public RuntimeInfoComponent getParentRuntimeInfoComponent() {
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

  @Override
  public boolean remove(MessageDigestErrorEntry entry) {
    return wrappedComponent.remove(entry);
  }

  @Override
  public boolean remove(String uniqueId) {
    return wrappedComponent.remove(uniqueId);
  }

  @Override
  public boolean remove(MessageDigestErrorEntry entry, boolean attemptFileDelete) {
    return wrappedComponent.remove(entry, attemptFileDelete);
  }

  @Override
  public boolean remove(String uniqueId, boolean attemptFileDelete) {
    return wrappedComponent.remove(uniqueId, attemptFileDelete);
  }
}
