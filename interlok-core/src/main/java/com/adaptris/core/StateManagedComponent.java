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


/**
 * <p>
 * Specifies methods for components which manage state transitions.
 * </p>
 * 
 * @see com.adaptris.core.AdaptrisComponent
 */
public interface StateManagedComponent extends AdaptrisComponent {

  /**
   * <p>
   * Returns the last recorde <code>ComponentState</code>.
   * </p>
   * @return the current <code>ComponentState</code>
   */
  ComponentState retrieveComponentState();
  
  /**
   * <p>
   * Updates the state for the component <code>ComponentState</code>.
   * </p>
   */
  void changeState(ComponentState newState);

  /**
   * <p>
   * Request this component is init'd.
   * </p>
   * @throws CoreException wrapping any underlying Exceptions
   */
  void requestInit() throws CoreException;

  /**
   * <p>
   * Request this component is started.
   * </p>
   * @throws CoreException wrapping any underlying Exceptions
   */
  void requestStart() throws CoreException;

  /**
   * <p>
   * Request this component is stopped.
   * </p>
   */
  void requestStop();

  /**
   * <p>
   * Request this component is closed.
   * </p>
   */
  void requestClose();

}
