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
 * Lifecycle extension point for reacting to connection state transitions.
 * <p>
 * Implementations are attached to an {@link AdaptrisConnection} and are invoked by the
 * standard connection lifecycle ({@link #init()}, {@link #start()}, {@link #stop()}, {@link #close()}).
 * This lets a handler observe external signals (for example reconnecting/reconnected events)
 * and update connection state via {@link StateManagedComponent#changeState(ComponentState)} when appropriate.
 * </p>
 */
public interface ConnectionStateHandler extends ComponentLifecycle {

  /**
   * Register the owning connection instance before lifecycle methods are invoked.
   *
   * @param connection the connection that owns this handler
   */
  void registerConnection(AdaptrisConnection connection);

  /**
   * Return the registered connection as the requested type.
   *
   * @param type the expected connection type
   * @param <T> target type
   * @return the registered connection cast to {@code type}
   */
  <T> T retrieveConnection(Class<T> type);
}
