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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.util.TimeInterval;

public abstract class ExpiringCacheCase extends ItemCacheCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testSetAgeBeforeEviction() throws Exception {
    InlineItemCache cache = createCache();
    TimeInterval defaultInterval = new TimeInterval(12L, TimeUnit.HOURS);
    TimeInterval interval = new TimeInterval(11L, TimeUnit.DAYS);
    assertNull(cache.getAgeBeforeEviction());
    assertEquals(defaultInterval.toMilliseconds(), cache.ageBeforeEvictionMs());

    cache.setAgeBeforeEviction(interval);
    assertEquals(interval, cache.getAgeBeforeEviction());
    assertEquals(interval.toMilliseconds(), cache.ageBeforeEvictionMs());

    cache.setAgeBeforeEviction(null);
    assertNull(cache.getAgeBeforeEviction());
    assertEquals(defaultInterval.toMilliseconds(), cache.ageBeforeEvictionMs());

  }

  @Test
  public void testCacheExpiry() throws Exception {
    String oldName = Thread.currentThread().getName();
    InlineItemCache cache = null;
    try {
      Thread.currentThread().setName("testCacheFlush");
      cache = createCache();
      long lifetime = 100;
      cache.setAgeBeforeEviction(new TimeInterval(lifetime, TimeUnit.MILLISECONDS));
      start(cache);

      int count = 100;
      cache.update(createCacheEntries(count));
      assertCache(cache, count);

      Thread.sleep(lifetime + ThreadLocalRandom.current().nextInt(100) + 10);
      cache.evict();
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

  @Override
  protected abstract InlineItemCache createCache() throws Exception;
}
