package com.adaptris.core.services.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.services.cache.translators.MetadataCacheValueTranslator;
import com.adaptris.util.TimeInterval;

public class CheckCacheServiceTest extends CacheServiceBaseCase {
  private static final String FOUND = "found";
  private static final String NOT_FOUND = "notFound";
  static final String LOOKUP_VALUE = "lookupValue";
  static final String LOOKED_UP_VALUE = "lookedUpValue";

  @Test
  public void testIsBranching() throws Exception {

    CheckCacheService service = createServiceForTests();
    assertTrue(service.isBranching());
  }

  @Test
  public void testDoService_Error() throws Exception {
    AdaptrisMessage msg =
        createMessage("Hello World", Arrays.asList(new MetadataElement[] {new MetadataElement(LOOKUP_VALUE, LOOKUP_VALUE)}));
    ExpiringMapCache cache =
        new KeysSizeUnsupportedCache().withMaxEntries(10).withExpiration(new TimeInterval(10L, TimeUnit.SECONDS));
    CheckCacheService service = new CheckCacheService() {
      @Override
      protected boolean eval(AdaptrisMessage msg, FoundInCache callback) throws CoreException {
        throw new CoreException();
      }
    };
    try {
      service.withConnection(new CacheConnection().withCacheInstance(cache));
      service.setKeysFoundServiceId(FOUND);
      service.setKeysNotFoundServiceId(NOT_FOUND);
      start(service);
      cache.put(LOOKUP_VALUE, LOOKED_UP_VALUE);
      service.doService(msg);
      fail();
    } catch (ServiceException expected) {
    } finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_InCache() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
        new MetadataElement(LOOKUP_VALUE, LOOKUP_VALUE)
    }));

    ExpiringMapCache cache = createCacheInstanceForTests();
    CheckCacheService service = createServiceForTests();
    try {
      service.setConnection(new CacheConnection(cache));
      service.setKeysFoundServiceId(FOUND);
      service.setKeysNotFoundServiceId(NOT_FOUND);
      start(service);
      cache.put(LOOKUP_VALUE, LOOKED_UP_VALUE);
      service.doService(msg);
      assertEquals(FOUND, msg.getNextServiceId());
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_DoesNotUseKeys() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
        new MetadataElement(LOOKUP_VALUE, LOOKUP_VALUE)
    }));

    ExpiringMapCache cache = new KeysSizeUnsupportedCache().withMaxEntries(10)
        .withExpiration(new TimeInterval(10L, TimeUnit.SECONDS));

    CheckCacheService service = createServiceForTests();
    try {
      service.setConnection(new CacheConnection(cache));
      service.setKeysFoundServiceId(FOUND);
      service.setKeysNotFoundServiceId(NOT_FOUND);
      start(service);
      cache.put(LOOKUP_VALUE, LOOKED_UP_VALUE);
      service.doService(msg);
      assertEquals(FOUND, msg.getNextServiceId());
    } finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_NotInCache() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
        new MetadataElement(LOOKUP_VALUE, LOOKUP_VALUE)
    }));

    ExpiringMapCache cache = createCacheInstanceForTests();

    CheckCacheService service = createServiceForTests();
    try {
      service.setConnection(new CacheConnection(cache));
      service.setKeysFoundServiceId(FOUND);
      service.setKeysNotFoundServiceId(NOT_FOUND);
      start(service);
      service.doService(msg);
      assertEquals(NOT_FOUND, msg.getNextServiceId());
    }
    finally {
      stop(service);
    }
  }

  @Override
  protected CheckCacheService createService() {
    return new CheckCacheService();
  }

  private CheckCacheService createServiceForTests() {
    CheckCacheService service = createService();
    CacheEntryEvaluator eval = new CacheEntryEvaluator();

    eval.setKeyTranslator(new MetadataCacheValueTranslator(LOOKUP_VALUE));
    service.addCacheEntryEvaluator(eval);

    return service;
  }

  @Override
  protected BranchingServiceCollection createServiceForExamples() {
    return BasicCacheExampleGenerator.createCheckCache();
  }

  protected static class KeysSizeUnsupportedCache extends ExpiringMapCache {

    @Override
    public List<String> getKeys() {
      throw new UnsupportedOperationException("getKeys");
    }

    @Override
    public int size() {
      throw new UnsupportedOperationException("size");
    }
  }
}
