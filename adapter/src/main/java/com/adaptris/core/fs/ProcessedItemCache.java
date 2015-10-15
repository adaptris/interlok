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

package com.adaptris.core.fs;

import com.adaptris.core.AdaptrisComponent;

/**
 * Simple interface to track items that have been processed for {@link NonDeletingFsConsumer}
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface ProcessedItemCache extends AdaptrisComponent {

  /**
   * Update the cache with the tracked item.
   *
   * @param i the tracked item.
   */
  void update(ProcessedItem i);

  /**
   * Get the TrackedItem associated with the key.
   *
   * @param key the key
   * @return the TrackedItem or null if it does not exist.
   */
  ProcessedItem get(String key);

  /**
   * Query the cache for this key.
   *
   * @param key the key
   * @return true if the item exists.
   */
  boolean contains(String key);

  /**
   * Return the number of items in the cache.
   *
   * @return the number of items in the cache.
   */
  int size();

  /**
   * Clear the cache.
   *
   */
  void clear();

  /**
   * Update the cache with the associated list of entries.
   *
   * @param list a list of tracked items
   */
  void update(ProcessedItemList list);

  /**
   * Explicitly save the state of the cache.
   *
   *
   */
  void save();

  /**
   * evict any items in the cache that require it..
   *
   *
   */
  void evict();

}
