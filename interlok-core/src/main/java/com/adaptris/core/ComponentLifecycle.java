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
 * Defines standard lifecycle operations for components in the framework. Refer
 * to <code>StateManagedComponent</code> for methods which control permitted
 * state transitions. </p>
 * 
 * @see StateManagedComponent
 */
public interface ComponentLifecycle {
  /**
   * <p>
   * Initialises the component. Component initialisation includes config
   * verification, creation of connections etc.
   * </p>
   *
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  void init() throws CoreException;

  /**
   * <p>
   * Starts the component. Once a component is started it should be ready to
   * process messages. In the case of <code>AdaptrisMessageConsumer</code>s,
   * calling start will begin message delivery.
   * </p>
   *
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  void start() throws CoreException;

  /**
   * <p>
   * Stops the component. A stopped component is not expected to be ready to
   * process messages. In the case of <code>AdaptrisMessageConsumer</code>s,
   * calling stop will pause message delivery. Throwing a
   * <code>RuntimeException</code> is considered to be a bug.
   * </p>
   */
  void stop();

  /**
   * <p>
   * Closes the component. A closed component should release any connections it
   * uses, etc. and clean up completely. Throwing a
   * <code>RuntimeException</code> is considered to be a bug.
   * </p>
   */
  void close();
}
