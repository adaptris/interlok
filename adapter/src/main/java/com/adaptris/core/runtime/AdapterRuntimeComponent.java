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

import com.adaptris.core.StateManagedComponent;


/**
 * Base interface that exposes management functionality for an Adapter component.
 *
 * @author lchan
 */
public interface AdapterRuntimeComponent<S extends StateManagedComponent> extends AdapterComponentMBean {

  /**
   * Get the component that this implementation manages.
   *
   * @return my component.
   */
  S getWrappedComponent();

  /**
   * Get the class name that is wrapped by this MBean.
   * 
   * @return the class name (e.g. {@code com.adaptris.core.StandardWorkflow})
   */
  String getWrappedComponentClassname();

  /**
   * Get all the descendants of this ParentComponent implementation.
   *
   * @return all the children, and children's children, etc...
   */
  Collection<BaseComponentMBean> getAllDescendants();
}
