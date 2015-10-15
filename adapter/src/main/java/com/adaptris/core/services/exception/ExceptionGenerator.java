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

package com.adaptris.core.services.exception;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Interface for generating an exception from an {@link AdaptrisMessage} object.
 *
 * @author lchan
 * 
 */
public interface ExceptionGenerator {

  /**
   * Generate a service exception from the AdaptrisMessage.
   *
   * @param msg the message
   * @return a ServiceException ready to be thrown, or null if no exception is
   *         appropriate
   */
  ServiceException create(AdaptrisMessage msg);

}
