package com.adaptris.core.services.cache;

import java.util.concurrent.TimeUnit;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.util.TimeInterval;

public abstract class SingleKeyCacheCase extends ServiceCase {


  public SingleKeyCacheCase() {
    if (PROPERTIES.getProperty(CacheServiceExample.BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(CacheServiceExample.BASE_DIR_KEY));
    }
  }

  protected ExpiringMapCache createCacheInstanceForTests() {
    return new ExpiringMapCache().withMaxEntries(10).withExpiration(new TimeInterval(10L, TimeUnit.SECONDS));
  }

}