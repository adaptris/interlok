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

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_FAILED_MESSAGE_RETRIER_TYPE;

import java.io.File;
import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.ChildRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.util.JmxHelper;

public class DefaultFailedMessageRetrierJmx implements DefaultFailedMessageRetrierJmxMBean, ChildRuntimeInfoComponent {

  private transient AdapterManager parent;
  private transient DefaultFailedMessageRetrier wrappedComponent;
  private transient ObjectName myObjectName = null;
  private transient SerializableMessageTranslator translator;

  DefaultFailedMessageRetrierJmx(AdapterManager p, DefaultFailedMessageRetrier e) throws MalformedObjectNameException {
    parent = p;
    wrappedComponent = e;
    initMembers();
    translator = new DefaultSerializableMessageTranslator();
  }

  private void initMembers() throws MalformedObjectNameException {
    // Builds up a name com.adaptris:type=LogHandler,adapter=<adapter-id>
    // There can be only one LogHandler per adapter.
    myObjectName = ObjectName.getInstance(JMX_FAILED_MESSAGE_RETRIER_TYPE + parent.createObjectHierarchyString() + ID_PREFIX
        + wrappedComponent.getClass().getSimpleName());
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

  @Override
  public RuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public boolean retryMessage(SerializableAdaptrisMessage msg) throws CoreException {
    return wrappedComponent.retryMessage(translator.translate(msg));
  }

  @Override
  public boolean retryMessage(File file) throws IOException, CoreException {
    return wrappedComponent.retryMessage(file);
  }

}
