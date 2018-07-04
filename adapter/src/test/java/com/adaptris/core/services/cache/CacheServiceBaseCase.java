package com.adaptris.core.services.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.cache.NullCacheImplementation;
import com.adaptris.core.cache.RetryingCacheProxy;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

import net.jodah.expiringmap.ExpirationPolicy;

public abstract class CacheServiceBaseCase extends CacheServiceExample {
  protected static final String HYPHEN = "-";

  protected enum CacheImps {

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

    public abstract Cache createCacheImplementation();

    public abstract String getXmlHeader();

    public abstract boolean matches(Cache impl);
  }

  public CacheServiceBaseCase(String s) {
    super(s);
  }

  protected AdaptrisMessage createMessage(String payload, Collection<MetadataElement> metadata) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    for (MetadataElement element : metadata) {
      msg.addMetadata(element);
    }
    return msg;
  }

  protected abstract CacheServiceBase createServiceForExamples();

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

  protected CacheImps getCacheImp(CacheServiceBase service) {
    CacheImps result = null;
    for (CacheImps sort : CacheImps.values()) {
      if (sort.matches(((CacheConnection) service.getConnection()).getCacheInstance())) {
        result = sort;
        break;
      }
    }
    return result;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List<CacheServiceBase> retrieveObjectsForSampleConfig() {
    List<CacheServiceBase> result = new ArrayList<CacheServiceBase>();
    for (CacheImps c : CacheImps.values()) {
      CacheServiceBase service = createServiceForExamples();
      service.setConnection(new CacheConnection(c.createCacheImplementation()));
      result.add(service);
    }
    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object) + getCacheImp((CacheServiceBase) object).getXmlHeader();
  }

  @Override
  protected String createBaseFileName(Object object) {
    CacheServiceBase p = (CacheServiceBase) object;
    return super.createBaseFileName(object) + HYPHEN
        + ((CacheConnection) p.getConnection()).getCacheInstance().getClass().getSimpleName();
  }

}