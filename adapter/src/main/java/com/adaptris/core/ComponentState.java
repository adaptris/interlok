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

import java.io.Serializable;

/**
 * <p> 
 * Defines the state transition requests that can be made to a
 * <code>StateManagedComponent</code>.  Concrete implementations represent an 
 * actual state and permitted transitions from that state. 
 * </p>
 */
public interface ComponentState extends Serializable {

  /**
   * <p>
   * Perform operations required to move <code>comp</code> from this
   * state to <code>InitialisedState</code>, if possible.
   * </p>
   * @param comp the <code>StateManagedComponent</code> to manipulate
   * @throws CoreException wrapping any underlying exceptions
   */
  void requestInit(StateManagedComponent comp) throws CoreException;
  
  /**
   * <p>
   * Perform operations required to move <code>comp</code> from this
   * state to <code>StartedState</code>, if possible.
   * </p>
   * @param comp the <code>StateManagedComponent</code> to manipulate
   * @throws CoreException wrapping any underlying exceptions
   */
  void requestStart(StateManagedComponent comp) throws CoreException; 
  
  /**
   * <p>
   * Perform operations required to move <code>comp</code> from this
   * state to <code>StoppedState</code>, if possible.
   * </p>
   * @param comp the <code>StateManagedComponent</code> to manipulate
   */
  void requestStop(StateManagedComponent comp);
  
  /**
   * <p>
   * Perform operations required to move <code>comp</code> from this
   * state to <code>ClosedState</code>, if possible.
   * </p>
   * @param comp the <code>StateManagedComponent</code> to manipulate
   */
  void requestClose(StateManagedComponent comp);
  
  /**
   * <p>
   * Perform operations required to restart the <code>comp</code>.  Differs
   * from start in that underlying components which are in the 
   * <code>StartedState</code> will be stopped, closed, inited and started.
   * </p>
   * @param comp the <code>StateManagedComponent</code> to manipulate
   * @throws CoreException wrapping any underlying exceptions
   */
  void requestRestart(StateManagedComponent comp) throws CoreException;
}
