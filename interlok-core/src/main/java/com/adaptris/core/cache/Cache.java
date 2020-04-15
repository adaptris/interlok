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
import com.adaptris.util.TimeInterval;

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
   * Puts any object into the cache (optional operation).
   * <p>
   * Not guaranteed to be supported by all cache implementations as some persistent caches require serialization.
   * </p>
   * 
   * @param key key to store the value against
   * @param value value to be stored
   * @implNote The default implementation throws an instance of {@link UnsupportedOperationException} and performs no other action.
   * @throws UnsupportedOperationException if the operation is not supported by this cache instance
   * @throws CoreException if there was an exception accessing the cache.
   */
  default void put(String key, Object value) throws CoreException {
    throw new UnsupportedOperationException("put(String,Object)");
  }

  /**
   * Puts a serializable object into the cache, specifying a expiration (optional operation).
   * <p>
   * Since JSR107 doesn't define per item cache expiry; this may not be supported by all cache implementations.
   * </p>
   * 
   * @param key key to store the value against
   * @param value value to be stored
   * @param expiry the expiry expressed as a {@link TimeInterval}
   * @implNote The default implementation just calls {@link #put(String, Serializable, long)}
   * @throws CoreException if there was an exception accessing the cache.
   */
  default void put(String key, Serializable value, TimeInterval expiry) throws CoreException {
    put(key, value, expiry.toMilliseconds());
  }

  /**
   * Puts a serializable object into the cache, specifying a expiration (optional operation).
   * <p>
   * Since JSR107 doesn't define per item cache expiry; this may not be supported by all cache implementations.
   * </p>
   * 
   * @param key key to store the value against
   * @param value value to be stored
   * @param ttl the TTL in milliseconds.
   * @implNote The default implementation throws an instance of {@link UnsupportedOperationException} and performs no other action.
   * @throws UnsupportedOperationException if the this operation is not supported by this cache instance
   * @throws CoreException if there was an exception accessing the cache.
   */
  default void put(String key, Serializable value, long ttl) throws CoreException {
    throw new UnsupportedOperationException("put(String,Serializable,long)");
  }

  /**
   * Puts an object into the cache, specifying a expiration (optional operation).
   * <p>
   * Since JSR107 doesn't define per item cache expiry; this may not be supported by all cache implementations.
   * </p>
   * 
   * @param key key to store the value against
   * @param value value to be stored
   * @param expiry the expiry specified as a TimeInterval
   * @implNote The default implementation just calls {@link #put(String, Object, long)}
   * @throws CoreException if there was an exception accessing the cache.
   */
  default void put(String key, Object value, TimeInterval expiry) throws CoreException {
    put(key, value, expiry.toMilliseconds());
  }


  /**
   * Puts an object into the cache, specifying a expiration (optional operation).
   * <p>
   * Since JSR107 doesn't define per item cache expiry; this may not be supported by all cache implementations.
   * </p>
   * 
   * @param key key to store the value against
   * @param value value to be stored
   * @param ttl the TTL in milliseconds.
   * @implNote The default implementation throws an instance of {@link UnsupportedOperationException} and performs no other action.
   * @throws UnsupportedOperationException if the this operation is not supported by this cache instance
   * @throws CoreException if there was an exception accessing the cache.
   */
  default void put(String key, Object value, long ttl) throws CoreException {
    throw new UnsupportedOperationException("put(String,Object,long)");
  }

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
   * Retrieves a List of all the keys in the cache (optional operation).
   * 
   * @implNote The default implementation throws an instance of {@link UnsupportedOperationException} and performs no other action.
   * @throws UnsupportedOperationException if the operation is not supported by this cache instance
   * @return a List<String> of all the keys in the cache
   * @throws CoreException if there was an exception accessing the cache.
   */
  default List<String> getKeys() throws CoreException {
    throw new UnsupportedOperationException("getKeys");
  }

  /**
   * Clears all entries from the cache (optional operation).
   * 
   * @implNote The default implementation throws an instance of {@link UnsupportedOperationException} and performs no other action.
   * @throws UnsupportedOperationException if the operation is not supported by this cache instance
   * @throws CoreException if there was an exception accessing the cache.
   */
  default void clear() throws CoreException {
    throw new UnsupportedOperationException("clear");
  }
  
  /**
   * @return the number of items in the cache (optional operation).
   * @implNote The default implementation throws an instance of {@link UnsupportedOperationException} and performs no other action.
   * @throws UnsupportedOperationException if the operation is not supported by this cache instance
   * @throws CoreException if there was an exception accessing the cache.
   */
  default int size() throws CoreException {
    throw new UnsupportedOperationException("size");
  }
}