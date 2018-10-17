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

import java.util.Set;

/**
 * <p>
 * Represents a connection> to an application or of a protocol type. E.g. JMS, database, etc.
 * </p>
 * <p>
 * It is generally the responsibility of implementations of this interface to to deliver messages to
 * registered {@link com.adaptris.core.AdaptrisMessageConsumer} instances.
 * </p>
 * 
 * @since 3.0.3 extends {@link ComponentLifecycleExtension} to satisfy any underlying
 *        pre-initialisation activities.
 */
public interface AdaptrisConnection extends AdaptrisComponent, ComponentLifecycleExtension,
    StateManagedComponent, JndiBindable {
  
  /**
   * Return a collection of components that need to be restarted on exception.
   *
   * @return a list of Components that need to be restarted of any exceptions.
   * @see ConnectionErrorHandler
   */
  Set<StateManagedComponent> retrieveExceptionListeners();

  /**
   * Add a component that will be notified upon exception.
   *
   * @param comp the component that will be notified.
   */
  void addExceptionListener(StateManagedComponent comp);

  /**
   * <p>
   * Adds a <code>AdaptrisMessageProducer</code> to this connection's
   * internal store of message producers.
   * </p>
   * @param producer the <code>AdaptrisMessageProducer</code> to add
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  void addMessageProducer(AdaptrisMessageProducer producer)
    throws CoreException;

  /**
   * <p>
   * Returns a <code>List</code> of this connection's
   * <code>AdaptrisMessageProducer</code>s.
   * </p>
   * @return a <code>List</code> of this connection's
   * <code>AdaptrisMessageProducer</code>s
   */
  Set<AdaptrisMessageProducer> retrieveMessageProducers();

  /**
   * <p>
   * Adds a <code>AdaptrisMessageConsumer</code> to this connection's
   * internal store of message consumers.
   * </p>
   * @param consumer the <code>AdaptrisMessageConsumer</code> to add
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  void addMessageConsumer(AdaptrisMessageConsumer consumer)
    throws CoreException;

  /**
   * <p>
   * Returns a <code>List</code> of this connection's
   * <code>AdaptrisMessageConsumer</code>s.
   * </p>
   * @return a <code>List</code> of this connection's
   * <code>AdaptrisMessageConsumer</code>s
   */
  Set<AdaptrisMessageConsumer> retrieveMessageConsumers();

  /**
   * Sets the {@link ConnectionErrorHandler} to use.
   */
  void setConnectionErrorHandler(ConnectionErrorHandler handler);

  /**
   * Returns the configured {@link ConnectionErrorHandler}.
   * 
   */
  ConnectionErrorHandler getConnectionErrorHandler();

  /**
   * Returns the currently active {@link ConnectionErrorHandler}
   * 
   * @return the active {@link ConnectionErrorHandler}, which may not be the same as the configured...
   */
  ConnectionErrorHandler connectionErrorHandler();

  /**
   * Return the connection as represented by this connection
   *
   * @param type the type of connection
   * @return the connection
   */
  <T> T retrieveConnection(Class<T> type);


  /**
   * Make a copy of this object for test purposes.
   * 
   * @return a copy of this object
   * @throws CoreException wrapping any exceptions
   */
  AdaptrisConnection cloneForTesting() throws CoreException;
}
