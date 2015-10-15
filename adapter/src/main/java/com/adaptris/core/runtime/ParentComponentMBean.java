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

import java.util.Collection;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Basic interface of MBeans that contain child member components.
 *
 * @author lchan
 */
public interface ParentComponentMBean extends BaseComponentMBean {

  /**
   * Get the list of {@link ObjectName} instances that map to all the immeidate child components of this parent.
   *
   * @return a list of {@link ObjectName} instances.
   */
  Collection<ObjectName> getChildren() throws MalformedObjectNameException;

}
