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

package com.adaptris.core.services.dynamic;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.Service;

/**
 * Interface for use with {@link DynamicServiceExecutor}.
 *
 */
public interface ServiceExtractor extends ComponentLifecycle {

  /**
   * Build a service from the message.
   *
   * @param msg the message
   * @param m the marshaller
   * @return a service
   * @throws Exception if no service could be created
   * @since 3.8.4
   */
  Service getService(AdaptrisMessage msg, AdaptrisMarshaller m) throws Exception;

}
