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

package com.adaptris.transform.validate;

/** Interface for validating arbitary XML content against the relevant
 *  schema.
 * @author sellidge
 * @author $Author: lchan $
 */
public interface ContentValidation {
  /** Parse the content and check it's validity.
   * 
   * @param content the content to parse.
   * @return true or false
   * @throws Exception on a fatal error that could not be handled.
   */
  boolean isValid(String content) throws Exception;

  /** Get the message that caused <code>false</code> to be returned
   * 
   * @return the message
   */
  String getMessage();
}
