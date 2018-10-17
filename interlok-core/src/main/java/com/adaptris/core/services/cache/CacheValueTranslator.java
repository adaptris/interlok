/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.services.cache;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Interface that defines a two way interaction - one to retrieve a value from an {@link AdaptrisMessage} and another to inject a
 * value into one.
 *
 */
public interface CacheValueTranslator<S> {

  /**
   * Retrieves a value from a message
   *
   */
  S getValueFromMessage(AdaptrisMessage msg) throws CoreException;

  /**
   * Injects the supplied Object value into the message,
   *
   */
  void addValueToMessage(AdaptrisMessage msg, S value) throws CoreException;

}
