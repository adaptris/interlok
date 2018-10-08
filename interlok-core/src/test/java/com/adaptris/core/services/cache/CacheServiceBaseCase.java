package com.adaptris.core.services.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.Service;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.cache.NullCacheImplementation;
import com.adaptris.core.cache.RetryingCacheProxy;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

import net.jodah.expiringmap.ExpirationPolicy;

public abstract class CacheServiceBaseCase extends CacheServiceExample {

  protected enum CacheImps implements CacheExampleImplementation {

    ExpiringMapCache() {
      @Override
      public Cache createCacheImplementation() {
        return new ExpiringMapCache().withExpiration(new TimeInterval(60L, TimeUnit.MICROSECONDS)).withMaxEntries(1024)
            .withExpirationPolicy(ExpirationPolicy.ACCESSED);
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThis is a basic cache that expires entries.\n\n-->\n";
      }

      @Override
      public boolean matches(Cache impl) {
        return ExpiringMapCache.class.equals(impl.getClass());
      }
    },
    RetryCacheProxy() {
      @Override
      public Cache createCacheImplementation() {
        return new RetryingCacheProxy().withProxiedCache(new ExpiringMapCache()).withMaxAttempts(2)
            .withRetryInterval(new TimeInterval(2L, TimeUnit.SECONDS));
      }

      @Override
      public String getXmlHeader() {
        return "<!--\n\nThis cache proxies another cache (in this instance ExpiringMapCache) and will retry the cache every 2  seconds\n"
            + "if the cache returns null for a given lookup (max 2 retry attempts)" + "\n\n-->\n";
      }

      @Override
      public boolean matches(Cache impl) {
        return RetryingCacheProxy.class.equals(impl.getClass());
      }

    };

    public abstract boolean matches(Cache impl);
  }

  protected AdaptrisMessage createMessage(String payload, Collection<MetadataElement> metadata) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    for (MetadataElement element : metadata) {
      msg.addMetadata(element);
    }
    return msg;
  }


  protected abstract CacheServiceBase createService();

  @SuppressWarnings("deprecation")
  public void testSetCache() throws Exception {
    CacheServiceBase service = createService();
    assertNull(service.getCache());
    NullCacheImplementation newImp = new NullCacheImplementation();
    service.setCache(newImp);
    assertEquals(newImp, service.getCache());
  }

  @SuppressWarnings("deprecation")
  public void testRetrieveCache_Legacy() throws Exception {
    CacheServiceBase service = createService();
    try {
      service.setCache(new ExpiringMapCache());
      LifecycleHelper.initAndStart(service);
      assertEquals(ExpiringMapCache.class, service.retrieveCache().getClass());
    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @SuppressWarnings("deprecation")
  public void testPrepare() throws Exception {
    CacheServiceBase service = createService();
    try {
      LifecycleHelper.prepare(service);
      fail();
    }
    catch (CoreException expected) {

    }
    service = createService();
    service.setCache(new ExpiringMapCache());
    LifecycleHelper.prepare(service);
    service = createService();
    service.setConnection(new CacheConnection());
    LifecycleHelper.prepare(service);
  }

  public void testRetrieveCache_Connection() throws Exception {
    CacheServiceBase service = createService();
    try {
      assertNull(service.getConnection());
      service.setConnection(new CacheConnection());
      LifecycleHelper.initAndStart(service);
      assertEquals(NullCacheImplementation.class, service.retrieveCache().getClass());
      LifecycleHelper.stopAndClose(service);
      service.setConnection(new CacheConnection(new ExpiringMapCache()));
      LifecycleHelper.initAndStart(service);
      assertEquals(ExpiringMapCache.class, service.retrieveCache().getClass());
    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  protected ExpiringMapCache createCacheInstanceForTests() {
    return new ExpiringMapCache().withMaxEntries(10).withExpiration(new TimeInterval(10L, TimeUnit.SECONDS));
  }

  @Override
  protected CacheExampleImplementation getImplementation(CacheServiceBase service) {
    CacheImps result = null;
    for (CacheImps sort : CacheImps.values()) {
      if (sort.matches(((CacheConnection) service.getConnection()).getCacheInstance())) {
        result = sort;
        break;
      }
    }
    return result;
  }

  protected abstract Service createServiceForExamples();

  @Override
  protected Iterable<CacheExampleImplementation> getExampleCacheImplementations() {
    return Arrays.asList(CacheImps.values());
  }

  @Override
  protected Iterable<CacheExampleServiceGenerator> getExampleGenerators() {
    return Arrays.asList(new CacheExampleServiceGenerator() {

      @Override
      public Service createExampleService() {
        return createServiceForExamples();
      };
    });
  }
}