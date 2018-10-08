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
 * Base interface for translating messages to and from AdaptrisMessage
 * instances.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface AdaptrisMessageTranslator {
  /**
   * Register the message factory that should be used to create messages.
   * 
   * @param f the message factory.
   */
  void registerMessageFactory(AdaptrisMessageFactory f);

  /**
   * Return the currently registered message factory.
   * 
   * @return the message factory.
   */
  AdaptrisMessageFactory currentMessageFactory();
}
