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
package com.adaptris.core.cache;

import java.io.Serializable;
import java.util.List;

import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;

/**
 * Interface that defines basic general cache operations for use within the adapter.
 *
 *
 */
public interface Cache extends ComponentLifecycle {
  /**
   * Puts a serializable object into the cache. Guaranteed to be supported by all cache implementations.
   * 
   * @param key key to store the value against
   * @param value value to be stored
   * @throws CoreException if there was an exception accessing the cache.
   */
  void put(String key, Serializable value) throws CoreException;

  /**
   * Puts any object into the cache. Not guaranteed to be supported by all cache implementations as some persistent caches require
   * serialization.
   * 
   * @param key key to store the value against
   * @param value value to be stored
   * @throws CoreException if there was an exception accessing the cache.
   */
  void put(String key, Object value) throws CoreException;

  /**
   * Retrieves an object from the cache
   * 
   * @param key the key to look up the object
   * @return the object from the cache or null if it doesn't exist
   * @throws CoreException if there was an exception accessing the cache.
   */
  Object get(String key) throws CoreException;

  /**
   * Removes an object from the cache. Ignores nonexistent elements
   * 
   * @param key the key to locate the object in the cache
   * @throws CoreException if there was an exception accessing the cache.
   */
  void remove(String key) throws CoreException;

  /**
   * Retrieves a java.util.List of all the keys in the cache
   * 
   * @return a java.util.List of all the keys in the cache
   * @throws CoreException if there was an exception accessing the cache.
   */
  List<String> getKeys() throws CoreException;

  /**
   * Clears all entries from the cache
   * 
   * @throws CoreException if there was an exception accessing the cache.
   */
  void clear() throws CoreException;

  /**
   * @return the number of items in the cache
   * @throws CoreException if there was an exception accessing the cache.
   */
  int size() throws CoreException;

}