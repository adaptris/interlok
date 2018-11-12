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

import java.util.Map;

import com.adaptris.annotation.Removal;

/**
 * <p>
 * Defines behaviour related to sending and receiving <code>Event</code>s using
 * other standard framework components.
 * </p>
 */
public interface EventHandler 
 extends AdaptrisComponent, StateManagedComponent, ComponentLifecycleExtension {

  /**
   * <p>
   * Send an <code>Event</code> to the configured default destination.
   * </p>
   * @param evt the <code>Event</code> to send
   * @throws CoreException wrapping any underlying Exceptions
   */
  void send(Event evt) throws CoreException;

  /**
   * <p>
   * Send an <code>Event</code> to the configured default destination.
   * </p>
   * @param evt the <code>Event</code> to send
   * @param properties any additional properties that should be sent if possible.
   * @throws CoreException wrapping any underlying Exceptions
   */
  void send(Event evt, Map<String,String> properties) throws CoreException;

  
  /**
   * <p>
   * Send an <code>Event</code> to the specified destination.
   * </p>
   * @param evt the <code>Event</code> to send
   * @param destination the <code>ProduceDestination</code> to send to
   * @throws CoreException wrapping any underlying Exceptions
   * @deprecated since 3.8.2; events should really be explicitly configured, will be removed with no replacement
   */
  @Deprecated
  @Removal(version="3.10.0")
  void send(Event evt, ProduceDestination destination) throws CoreException;

  /**
   * <p>
   * Sets the source id for this EventHandler. The source id may be used for routing or replying to events and is generally the
   * unique id of the <code>Adapter</code>.
   * </p>
   * 
   * @param sourceId the source id to be sent as part of the <code>Event</code>
   */
  void registerSourceId(String sourceId);

  /**
   * <p>
   * Retrieve the source id for this EventHandler. The source id may be used for routing or replying to events and is generally the
   * unique id of the <code>Adapter</code>.
   * </p>
   * 
   */
  String retrieveSourceId();
  
}
