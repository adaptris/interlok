package com.adaptris.core.services.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.services.cache.translators.MetadataCacheValueTranslator;
import com.adaptris.core.services.cache.translators.StaticCacheValueTranslator;

public class RetrieveFromCacheServiceTest extends CacheServiceBaseCase {
  static final String TARGET_METADATA_KEY = "targetMetadataKey";
  static final String SRC_METADATA_KEY = "srcMetadataKey";
  static final String LOOKUP_VALUE = "lookupValue";
  static final String LOOKED_UP_VALUE = "lookedUpValue";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Test
  public void testSetExceptionIfNotFound() {
    RetrieveFromCacheService service = createServiceForTests();
    assertNull(service.getExceptionIfNotFound());
    assertEquals(true, service.exceptionIfNotFound());

    service.setExceptionIfNotFound(Boolean.FALSE);
    assertEquals(false, service.exceptionIfNotFound());

    service.setExceptionIfNotFound(null);
    assertNull(service.getExceptionIfNotFound());
    assertEquals(true, service.exceptionIfNotFound());
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
    return BasicCacheExampleGenerator.createRetrieveFromCache();
  }
}
