package com.adaptris.core.services.cache;

import java.util.concurrent.TimeUnit;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.util.TimeInterval;

public abstract class SingleKeyCacheCase
    extends com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase {


  public SingleKeyCacheCase() {
    if (PROPERTIES.getProperty(
        com.adaptris.interlok.junit.scaffolding.services.CacheServiceExample.BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(
          com.adaptris.interlok.junit.scaffolding.services.CacheServiceExample.BASE_DIR_KEY));
    }
  }

  protected ExpiringMapCache createCacheInstanceForTests() {
    return new ExpiringMapCache().withMaxEntries(10).withExpiration(new TimeInterval(10L, TimeUnit.SECONDS));
  }

}