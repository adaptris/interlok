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

import java.util.EventListener;

/**
 * Interface for EventListeners on the cache. Implementations of this interface may be registered to listen for occurrences on the
 * cache, such as items being evicted, etc.
 *
 * @author stuellidge
 *
 */
public interface CacheEventListener extends EventListener {

  /**
   * Notification that the provided key / value was evicted from the cache
   *
   */
  public void itemEvicted(String key, Object value);

  /**
   * Notification that the provided key / value has expired from the cache
   * 
   */
  public void itemExpired(String key, Object value);

  /**
   * Notification that the provided key / value was put into the cache
   *
   */
  public void itemPut(String key, Object value);

  /**
   * Notification that the provided key / value was removed from the cache
   *
   */
  public void itemRemoved(String key, Object value);

  /**
   * Notification that the provided key / value was updated in the cache
   *
   */
  public void itemUpdated(String key, Object value);

}
