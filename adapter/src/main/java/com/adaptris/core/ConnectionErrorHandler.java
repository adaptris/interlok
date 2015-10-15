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
 * Implementations of this class encapsualte behaviour that is invoked when an
 * <code>Exception</code> relating to a connection is encountered.
 * </p>
 */
public interface ConnectionErrorHandler extends ComponentLifecycle {

  /**
   * <p>
   * Sets the {@link AdaptrisConnection} to handle errors for.
   * </p>
   *
   * @param connection the <code>AdaptrisConnection</code> to handle errors for
   */
  void registerConnection(AdaptrisConnection connection);

  /**
   * Return this components underlying connection.
   * 
   * @param type the type of connection
   * @return the connection
   */
  <T> T retrieveConnection(Class<T> type);

  /**
   * Is this error handler allowed to work with this error handler.
   *
   * @param ceh other error handler.
   * @return true if the two error handlers can work together.
   */
  boolean allowedInConjunctionWith(ConnectionErrorHandler ceh);

  /**
   * Handle the error.
   *
   */
  void handleConnectionException();

}
