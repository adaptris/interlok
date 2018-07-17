package com.adaptris.core.services.cache;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.services.cache.translators.MetadataCacheValueTranslator;

public class CheckAndRetrieveCacheTest extends CacheServiceBaseCase {
  private static final String FOUND = "found";
  private static final String NOT_FOUND = "notFound";
  static final String LOOKUP_VALUE = "lookupValue";
  static final String LOOKUP_METADATA_KEY = "lookupMetadataKey";
  static final String LOOKED_UP_VALUE = "lookedUpValue";

  public void testIsBranching() throws Exception {

    CheckCacheService service = createServiceForTests();
    assertTrue(service.isBranching());
  }

  public void testDoService_InCache() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
        new MetadataElement(LOOKUP_VALUE, LOOKUP_VALUE)
    }));

    ExpiringMapCache cache = createCacheInstanceForTests();
    CheckAndRetrieve service = createServiceForTests();
    try {
      service.setConnection(new CacheConnection(cache));
      service.setKeysFoundServiceId(FOUND);
      service.setKeysNotFoundServiceId(NOT_FOUND);
      start(service);
      cache.put(LOOKUP_VALUE, LOOKED_UP_VALUE);
      service.doService(msg);
      assertEquals(FOUND, msg.getNextServiceId());
      assertEquals(LOOKED_UP_VALUE, msg.getMetadataValue(LOOKUP_METADATA_KEY));
    }
    finally {
      stop(service);
    }
  }

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
      assertFalse(msg.headersContainsKey(LOOKUP_METADATA_KEY));
    }
    finally {
      stop(service);
    }
  }

  @Override
  protected CheckAndRetrieve createService() {
    return new CheckAndRetrieve();
  }

  private CheckAndRetrieve createServiceForTests() {
    CheckAndRetrieve service = createService();
    CacheEntryEvaluator eval = new CacheEntryEvaluator();

    eval.setKeyTranslator(new MetadataCacheValueTranslator(LOOKUP_VALUE));
    eval.setValueTranslator(new MetadataCacheValueTranslator(LOOKUP_METADATA_KEY));

    service.addCacheEntryEvaluator(eval);

    return service;
  }

  @Override
  protected BranchingServiceCollection createServiceForExamples() {
    return BasicCacheExampleGenerator.createCheckAndRetrieveCache();

  }
}
