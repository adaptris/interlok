package com.adaptris.core.services.cache;

import java.util.ArrayList;
import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.services.cache.translators.JmsReplyToCacheValueTranslator;
import com.adaptris.core.services.cache.translators.MetadataCacheValueTranslator;
import com.adaptris.core.services.cache.translators.StaticCacheValueTranslator;
import com.adaptris.core.services.cache.translators.StringPayloadCacheTranslator;

public class RetrieveFromCacheServiceTest extends CacheServiceBaseCase {
  static final String TARGET_METADATA_KEY = "targetMetadataKey";
  static final String SRC_METADATA_KEY = "srcMetadataKey";
  static final String LOOKUP_VALUE = "lookupValue";
  static final String LOOKED_UP_VALUE = "lookedUpValue";

  public void testSetExceptionIfNotFound() {
    RetrieveFromCacheService service = createServiceForTests();
    assertNull(service.getExceptionIfNotFound());
    assertEquals(true, (boolean)service.exceptionIfNotFound());

    service.setExceptionIfNotFound(Boolean.FALSE);
    assertEquals(false, (boolean)service.exceptionIfNotFound());

    service.setExceptionIfNotFound(null);
    assertNull(service.getExceptionIfNotFound());
    assertEquals(true, (boolean)service.exceptionIfNotFound());
  }

  public void testDoService() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(SRC_METADATA_KEY, LOOKUP_VALUE)
    }));

    Cache cache = createCacheInstanceForTests();
    RetrieveFromCacheService service = createServiceForTests();
    try {
      service.setConnection(new CacheConnection(cache));
      start(service);

      // Now add the TARGET DATA to the cache.
      cache.put(LOOKUP_VALUE, LOOKED_UP_VALUE);
      service.doService(msg);
      assertTrue(msg.headersContainsKey(TARGET_METADATA_KEY));
      assertEquals(LOOKED_UP_VALUE, msg.getMetadataValue(TARGET_METADATA_KEY));
    }
    finally {
      stop(service);
    }
  }

  public void testDoService_AddToMessageFailure() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(SRC_METADATA_KEY, LOOKUP_VALUE)
    }));

    Cache cache = createCacheInstanceForTests();
    RetrieveFromCacheService service = createServiceForTests();
    CacheEntryEvaluator eval = service.getCacheEntryEvaluators().get(0);
    // we know that StaticCacheValueTranslator should throw an exception if
    // we try to apply a value into the message.
    eval.setValueTranslator(new StaticCacheValueTranslator("dummy value"));
    try {
      service.setConnection(new CacheConnection(cache));
      start(service);

      // Now add the TARGET DATA to the cache.
      cache.put(LOOKUP_VALUE, LOOKED_UP_VALUE);
      service.doService(msg);
      fail("Expected a ServiceException");
    }
    catch (ServiceException expected) {

    }
    finally {
      stop(service);
    }
  }

  public void testDoService_NullValueTranslator() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(SRC_METADATA_KEY, LOOKUP_VALUE)
    }));

    Cache cache = createCacheInstanceForTests();
    RetrieveFromCacheService service = createServiceForTests();
    CacheEntryEvaluator eval = service.getCacheEntryEvaluators().get(0);
    // we know that StaticCacheValueTranslator should throw an exception if
    // we try to apply a value into the message.
    eval.setValueTranslator(new StaticCacheValueTranslator("dummy value"));
    try {
      service.setConnection(new CacheConnection(cache));
      start(service);

      // Now add the TARGET DATA to the cache.
      cache.put(LOOKUP_VALUE, LOOKED_UP_VALUE);
      service.doService(msg);
      fail("Expected a ServiceException");
    }
    catch (ServiceException expected) {

    }
    finally {
      stop(service);
    }
  }

  public void testDoService_KeyNotFound() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", new ArrayList<MetadataElement>());
    Cache cache = createCacheInstanceForTests();
    RetrieveFromCacheService service = createService();
    CacheEntryEvaluator eval = new CacheEntryEvaluator();
    eval.setErrorOnEmptyKey(false);
    service.addCacheEntryEvaluator(eval);
    try {
      service.setConnection(new CacheConnection(cache));
      start(service);
      service.doService(msg);
    }
    finally {
      stop(service);
    }
  }

  public void testDoService_ExceptionIfNotFound_NotFound() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(SRC_METADATA_KEY, LOOKUP_VALUE)
    }));

    Cache cache = createCacheInstanceForTests();
    RetrieveFromCacheService service = createServiceForTests();
    service.setExceptionIfNotFound(true);
    try {
      service.setConnection(new CacheConnection(cache));
      start(service);
      service.doService(msg);
      fail();
    }
    catch (ServiceException expected) {

    }
    finally {
      stop(service);
    }
  }

  public void testDoService_NoExceptionIfNotFound_NotFound() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(SRC_METADATA_KEY, LOOKUP_VALUE)
    }));

    Cache cache = createCacheInstanceForTests();
    RetrieveFromCacheService service = createServiceForTests();
    service.setExceptionIfNotFound(false);
    try {
      service.setConnection(new CacheConnection(cache));
      start(service);
      service.doService(msg);
      assertFalse(msg.headersContainsKey(TARGET_METADATA_KEY));
    }
    finally {
      stop(service);
    }
  }

  @Override
  protected RetrieveFromCacheService createService() {
    return new RetrieveFromCacheService();
  }

  protected RetrieveFromCacheService createServiceForTests() {
    RetrieveFromCacheService service = createService();
    CacheEntryEvaluator eval = new CacheEntryEvaluator();

    eval.setKeyTranslator(new MetadataCacheValueTranslator(SRC_METADATA_KEY));
    eval.setValueTranslator(new MetadataCacheValueTranslator(TARGET_METADATA_KEY));
    service.addCacheEntryEvaluator(eval);

    return service;
  }

  @Override
  protected RetrieveFromCacheService createServiceForExamples() {
    RetrieveFromCacheService service = createService();
    CacheEntryEvaluator eval1 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval2 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval3 = new CacheEntryEvaluator();

    eval1.setKeyTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Whose_Value_Is_The_Cache_key"));
    eval1.setValueTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Which_Will_Contain_What_We_Find_in_The_Cache"));

    eval2.setKeyTranslator(new MetadataCacheValueTranslator(
        "MetadataKey_Whose_Value_Is_The_Cache_Key_And_This_Key_Contains_A_Payload"));
    eval2.setValueTranslator(new StringPayloadCacheTranslator());

    eval3.setKeyTranslator(new MetadataCacheValueTranslator("JMSCorrelationID"));
    eval3.setValueTranslator(new JmsReplyToCacheValueTranslator());

    service.setCacheEntryEvaluators(new ArrayList(Arrays.asList(new CacheEntryEvaluator[]
    {
        eval1, eval2, eval3
    })));
    return service;
  }
}
