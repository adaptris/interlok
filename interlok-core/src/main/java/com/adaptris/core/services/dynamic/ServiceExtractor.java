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

import java.io.InputStream;

import com.adaptris.annotation.Removal;
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
   * Get an {@link InputStream} that can be unmarshalled into a service.
   * 
   * @param m the adaptris message.
   * @return an input stream.
   * @deprecated since 3.8.4 use {@link #getService(AdaptrisMessage, AdaptrisMarshaller)} instead.
   */
  @Deprecated
  @Removal(version = "3.11.0")
  InputStream getInputStream(AdaptrisMessage m) throws Exception;

  /**
   * Build a service from the message.
   * 
   * @param msg the message
   * @param m the marshaller
   * @return a service
   * @throws Exception if no service could be created
   * @since 3.8.4
   * @implSpec The default implementation still uses {@link #getInputStream(AdaptrisMessage)} for
   *           expediency but will be removed in 3.11.0. Just extend {@link ServiceExtractorImpl}
   *           for future compatibility.
   */
  default Service getService(AdaptrisMessage msg, AdaptrisMarshaller m) throws Exception {
    try (InputStream in = getInputStream(msg)) {
      return (Service) m.unmarshal(in);
    }
  }

}
