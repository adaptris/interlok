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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link Resolver}.
 * 
 */
public abstract class ResolverImp implements Resolver {

  protected Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Resolve a key against a map.
   * 
   * @param key the key
   * @param map the map of properties / environment
   * @return the resolved value or the value of {@code key} if not found in the map.
   */
  protected static String resolve(String key, Map<String, String> map) {
    if (map.containsKey(key)) {
      return map.get(key);
    }
    return key;
  }

  /**
   * Convenience method to turn a Properties into a Map&lt;String, String&gt;
   * 
   * @param p the properties, if null, will return an empty map
   * @return a map
   */
  protected static Map<String, String> asMap(Properties p) {
    Map<String, String> result = new HashMap<String, String>();
    if (p == null) {
      return result;
    }
    for (String key : p.stringPropertyNames()) {
      result.put(key, p.getProperty(key));
    }
    return result;
  }

}
