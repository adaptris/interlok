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

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_LOG_HANDLER_TYPE;

import java.io.IOException;

import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.ChildRuntimeInfoComponentImpl;
import com.adaptris.core.runtime.RuntimeInfoComponent;


/**
 * Wraps functionality exposing {@link FileLogHandler} to JMX.
 *
 * @author lchan
 *
 */
public class FileLogHandlerJmx extends ChildRuntimeInfoComponentImpl implements FileLogHandlerJmxMBean {

  private transient AdapterManager parent;
  private transient FileLogHandler wrappedComponent;

  private FileLogHandlerJmx() {
    super();
  }

  FileLogHandlerJmx(AdapterManager p, FileLogHandler e) {
    parent = p;
    wrappedComponent = e;
  }

  @Override
  protected String getType() {
    return JMX_LOG_HANDLER_TYPE;
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
  public void cleanupLogfiles() throws IOException {
    wrappedComponent.clean();
  }
}
