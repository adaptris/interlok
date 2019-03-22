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
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;

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
   */
  InputStream getInputStream(AdaptrisMessage m) throws Exception;
}
