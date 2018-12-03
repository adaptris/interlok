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

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.BaseCase;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class ItemCacheCase extends BaseCase {

  public ItemCacheCase(String name) {
    super(name);
  }

  protected transient Log logR = LogFactory.getLog(this.getClass());

  protected static final String CACHE_PREFIX = "CacheEntry_";

  public void testCache() throws Exception {
    String oldName = Thread.currentThread().getName();
    ProcessedItemCache cache = null;
    try {
      Thread.currentThread().setName("testCache");
      cache = createCache();
      start(cache);
      int count = 100;
      for (ProcessedItem item : createCacheEntries(count).getProcessedItems()) {
        cache.update(item);
      }
      assertCache(cache, count);

    }
    finally {
      stop(cache);
      Thread.currentThread().setName(oldName);
    }
  }

  public void testUpdateProcessedItemList() throws Exception {
    String oldName = Thread.currentThread().getName();
    ProcessedItemCache cache = null;
    try {
      Thread.currentThread().setName("testUpdateProcessedItemList");
      cache = createCache();
      start(cache);
      int count = 100;
      cache.update(createCacheEntries(count));
      assertCache(cache, count);

    }
    finally {
      stop(cache);
      Thread.currentThread().setName(oldName);
    }
  }


  public void testCacheClear() throws Exception {
    String oldName = Thread.currentThread().getName();
    ProcessedItemCache cache = null;
    try {
      Thread.currentThread().setName("testCacheClear");
      cache = createCache();
      start(cache);

      int count = 100;
      cache.update(createCacheEntries(count));
      assertCache(cache, count);
      cache.clear();
      assertEquals(0, cache.size());
      for (int i = 0; i < count; i++) {
        assertFalse(cache.contains(CACHE_PREFIX + i));
        assertNull(CACHE_PREFIX + i, cache.get(CACHE_PREFIX + i));
      }
    }
    finally {
      stop(cache);
      Thread.currentThread().setName(oldName);
    }
  }


  protected abstract ProcessedItemCache createCache() throws Exception;

  protected void assertCache(ProcessedItemCache cache, int count) {
    assertNotNull(cache);
    for (int i = 0; i < count; i++) {
      assertTrue(cache.contains(CACHE_PREFIX + i));
      assertNotNull(CACHE_PREFIX + i, cache.get(CACHE_PREFIX + i));
    }
  }

  protected ProcessedItemList createCacheEntries(int count) {
    ProcessedItemList result = new ProcessedItemList();
    Random r = new Random();
    for (int i = 0; i < count; i++) {
      result.addProcessedItem(new ProcessedItem(CACHE_PREFIX + i, r.nextLong(), r.nextLong()));
    }
    return result;
  }
}
