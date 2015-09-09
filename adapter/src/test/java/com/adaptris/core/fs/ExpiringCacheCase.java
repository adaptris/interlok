package com.adaptris.core.fs;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.adaptris.util.TimeInterval;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class ExpiringCacheCase extends ItemCacheCase {

  public ExpiringCacheCase(String name) {
    super(name);
  }

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

      Thread.sleep(lifetime + new Random().nextInt(100) + 10);
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
