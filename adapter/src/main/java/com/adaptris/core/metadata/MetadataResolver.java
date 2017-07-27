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

package com.adaptris.core.metadata;

import com.adaptris.core.AdaptrisMessage;

/**
 * <p>
 * This resolver allows you to specify a prefixed ($$) metadata key in configuration whose metadata value will be used as the actual metadata item key lookup.
 * </p>
 * <p>
 * For example, if I have 3 metadata items, with the following key -- values;
 * <ul>
 * <li>key1 -- value1</li>
 * <li>key2 -- value2</li>
 * </ul>
 * 
 * Resolving the key "key1" will simply return "key1".  No resolution has to be made here. <br />
 * Resolving the key "$$key1" will return the value "value1".  Because we prefixed the key with "$$" resolution does take place.
 * </p>
 * <p>
 * This resolver is useful if you do not happen to know during configuration what metadata key you need to perform an action on.
 * Instead this key can be looked up during runtime as the value of a known metadata key.
 * </p>
 *
 */
public class MetadataResolver {

  private static final String REFERENCE_PREFIX = "$$";
  
  public static String resolveKey(AdaptrisMessage message, String key) {
    if(key == null)
      return null;
    if(key.startsWith(REFERENCE_PREFIX)) {
      return message.getMetadataValue(key.substring(REFERENCE_PREFIX.length()));
    } else
      return key;
  }
  
}
