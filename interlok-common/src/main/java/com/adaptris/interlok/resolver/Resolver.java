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
package com.adaptris.interlok.resolver;

/**
 * Resolve a value from an external source.
 * 
 */
public interface Resolver {
  /**
   * Attempt to resolve a value externally.
   * 
   * @return the resolved value
   */
  String resolve(String lookupValue);

  /**
   * Can this resolver handle this type of value.
   * 
   * @param value the value e.g. {@code %env{MY_ENV_VAR}}
   * @return true or false.
   */
  boolean canHandle(String value);

  default String resolve(String value, String target) {
    return resolve(value);
  }
}
