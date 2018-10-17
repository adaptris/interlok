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

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_FAILED_MESSAGE_RETRIER_TYPE;

import java.io.File;
import java.io.IOException;

import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.ChildRuntimeInfoComponentImpl;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.interlok.types.SerializableMessage;

public class DefaultFailedMessageRetrierJmx extends ChildRuntimeInfoComponentImpl
    implements DefaultFailedMessageRetrierJmxMBean {

  private transient AdapterManager parent;
  private transient DefaultFailedMessageRetrier wrappedComponent;
  private transient SerializableMessageTranslator translator;

  private DefaultFailedMessageRetrierJmx() {
    super();
  }

  DefaultFailedMessageRetrierJmx(AdapterManager p, DefaultFailedMessageRetrier e) {
    parent = p;
    wrappedComponent = e;
    translator = new DefaultSerializableMessageTranslator();
  }

  @Override
  protected String getType() {
    return JMX_FAILED_MESSAGE_RETRIER_TYPE;
  }

  @Override
  protected String uniqueId() {
    return wrappedComponent.getClass().getSimpleName();
  }

  @Override
  public RuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public boolean retryMessage(SerializableMessage msg) throws CoreException {
    return wrappedComponent.retryMessage(translator.translate(msg));
  }

  @Override
  public boolean retryMessage(File file) throws IOException, CoreException {
    return wrappedComponent.retryMessage(file);
  }

}
