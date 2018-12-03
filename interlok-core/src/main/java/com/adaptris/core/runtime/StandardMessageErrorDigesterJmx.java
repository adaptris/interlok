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

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_MSG_ERR_DIGESTER_TYPE;

import java.util.List;

/**
 * Exposes all the records handled by {@link StandardMessageErrorDigester} and exposes them via JMX.
 *
 *
 * @author lchan
 *
 */
public class StandardMessageErrorDigesterJmx extends ChildRuntimeInfoComponentImpl implements StandardMessageErrorDigesterJmxMBean {

  private transient AdapterManager parent;
  private transient StandardMessageErrorDigester wrappedComponent;

  private StandardMessageErrorDigesterJmx() {
    super();
  }

  StandardMessageErrorDigesterJmx(AdapterManager owner, StandardMessageErrorDigester component) {
    this();
    parent = owner;
    wrappedComponent = component;
  }

  @Override
  protected String getType() {
    return JMX_MSG_ERR_DIGESTER_TYPE;
  }

  @Override
  protected String uniqueId() {
    return wrappedComponent.getUniqueId();
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
