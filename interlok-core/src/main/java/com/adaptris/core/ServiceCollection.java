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

import java.util.List;

/**
 * <p>
 * Defines behaviour common to collections of <code>Service</code>s.
 * This class extends <code>Service</code> and is thus a <code>Service</code>
 * itself.  Implementations may iterate through the collection in order,
 * provide branching, etc.
 * </p>
 */
public interface ServiceCollection extends Service, EventHandlerAware, List<Service>, ConfigComment {

  /**
   * <p>
   * Returns a <code>List</code> of the <code>Service</code>s in this
   * collection.
   * </p>
   * @return a <code>List</code> of the <code>Service</code>s in this
   * collection
   */
  List<Service> getServices();

  /**
   * <p>
   * Adds a <code>Service</code> to this collection.
   * </p>
   * @param service the <code>Service</code> to add
   * @throws CoreException wrapping any underlying Exception that may occur
   */
  void addService(Service service) throws CoreException;

  /**
   * <p>
   * Handles any exceptions thrown from an embedded {@linkplain Service}.
   * </p>
   *
   * @param service service which threw the Exception
   * @param e the exception which was thrown
   * @param msg the message which caused the exception
   * @throws ServiceException wrapping the exception if
   *           {@link Service#continueOnFailure()} is false
   */
  void handleException(Service service, AdaptrisMessage msg, Exception e)
    throws ServiceException;
}
