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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public interface ChildRuntimeInfoComponentMBean extends BaseComponentMBean, RuntimeInfoComponent {

  /**
   * Get the parents ObjectName representation.
   *
   * @return the Objectname that represents the parent management bean.
   * @throws MalformedObjectNameException
   */
  ObjectName getParentObjectName() throws MalformedObjectNameException;

  /**
   * Get the parent's uniqueid.
   *
   * @return the uniqueid of the parent (e.g. a message-digester will return the adapter-id, and interceptors the workflow-id).
   */
  String getParentId();
}
