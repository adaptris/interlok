package com.adaptris.core.services.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Collection;
import javax.jms.JMSException;
import javax.jms.Queue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.services.cache.translators.JmsReplyToCacheValueTranslator;
import com.adaptris.core.services.cache.translators.MetadataCacheValueTranslator;

public class AddToCacheServiceTest extends CacheServiceBaseCase {
  private static final String QUEUE_NAME = "TempReplyQueue";
  private static final String CORRELATION_ID = "12345ABCDE";

  private static final String SRC_KEY = "srcKey";
  private static final String SRC_VALUE = "srcValue";


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testEnforceSerializable() throws Exception {
    AddToCacheService service = createService();
    assertNull(service.getEnforceSerializable());
    assertFalse(service.enforceSerializable());

    service.setEnforceSerializable(Boolean.TRUE);
    assertEquals(true, service.enforceSerializable());
    assertTrue(service.getEnforceSerializable());

    service.setEnforceSerializable(null);
    assertNull(service.getEnforceSerializable());
    assertFalse(service.enforceSerializable());
  }

  @Test
  public void testDoService() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(JmsConstants.JMS_CORRELATION_ID, CORRELATION_ID)
    }));

    Cache cache = createCacheInstanceForTests();
    AddToCacheService service = createServiceForTests();
    try {
      service.setConnection(new CacheConnection(cache));
      service.setEnforceSerializable(false);
      start(service);
      service.doService(msg);
      Object value = cache.get(CORRELATION_ID);
      assertTrue("Cached object should be a JMS Queue", value instanceof Queue);
      assertEquals(QUEUE_NAME, ((Queue) value).getQueueName());
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_EnforceSerializable_NotSerializable() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(JmsConstants.JMS_CORRELATION_ID, CORRELATION_ID)
    }));

    Cache cache = createCacheInstanceForTests();
    AddToCacheService service = createServiceForTests();
    try {
      service.setConnection(new CacheConnection(cache));

      service.setEnforceSerializable(true);
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
  public void testDoService_EnforceSerializable_Serializable() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
        new MetadataElement(JmsConstants.JMS_CORRELATION_ID, CORRELATION_ID), new MetadataElement(SRC_KEY, SRC_VALUE)
    }));

    Cache cache = createCacheInstanceForTests();
    AddToCacheService service = createServiceForTests();
    CacheEntryEvaluator eval = service.getCacheEntryEvaluators().get(0);
    eval.setValueTranslator(new MetadataCacheValueTranslator(SRC_KEY));
    try {
      service.setConnection(new CacheConnection(cache));

      service.setEnforceSerializable(true);
      start(service);
      service.doService(msg);
      Object value = cache.get(CORRELATION_ID);
      assertEquals(SRC_VALUE, value.toString());
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_NoErrorOnEmpty_NoKeyValue() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(JmsConstants.JMS_CORRELATION_ID, CORRELATION_ID)
    }));

    Cache cache = createCacheInstanceForTests();
    AddToCacheService service = createServiceForTests();
    CacheEntryEvaluator eval = service.getCacheEntryEvaluators().get(0);
    eval.setErrorOnEmptyKey(false);
    eval.setKeyTranslator(new MetadataCacheValueTranslator(SRC_KEY));
    try {
      service.setConnection(new CacheConnection(cache));

      start(service);
      service.doService(msg);
      assertNull(cache.get(SRC_VALUE));
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_NoErrorOnEmpty_NoValue() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(JmsConstants.JMS_CORRELATION_ID, CORRELATION_ID)
    }));

    Cache cache = createCacheInstanceForTests();
    AddToCacheService service = createServiceForTests();
    CacheEntryEvaluator eval = service.getCacheEntryEvaluators().get(0);
    eval.setErrorOnEmptyValue(false);
    eval.setValueTranslator(new MetadataCacheValueTranslator(SRC_KEY));
    try {
      service.setConnection(new CacheConnection(cache));

      start(service);
      service.doService(msg);
      assertNull(cache.get(SRC_VALUE));
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_NoErrorOnEmpty_NeitherKeyOrValue() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(JmsConstants.JMS_CORRELATION_ID, CORRELATION_ID)
    }));

    Cache cache = createCacheInstanceForTests();
    AddToCacheService service = createServiceForTests();
    CacheEntryEvaluator eval = service.getCacheEntryEvaluators().get(0);
    eval.setErrorOnEmptyKey(false);
    eval.setErrorOnEmptyValue(false);
    eval.setValueTranslator(new MetadataCacheValueTranslator(SRC_KEY));
    eval.setKeyTranslator(new MetadataCacheValueTranslator(SRC_KEY));
    try {
      service.setConnection(new CacheConnection(cache));

      start(service);
      service.doService(msg);
      assertNull(cache.get(SRC_VALUE));
    }
    finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_ErrorOnEmpty() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(JmsConstants.JMS_CORRELATION_ID, CORRELATION_ID)
    }));

    Cache cache = createCacheInstanceForTests();
    AddToCacheService service = createServiceForTests();
    CacheEntryEvaluator eval = service.getCacheEntryEvaluators().get(0);
    eval.setValueTranslator(new MetadataCacheValueTranslator(SRC_KEY));
    eval.setKeyTranslator(new MetadataCacheValueTranslator(SRC_KEY));
    try {
      service.setConnection(new CacheConnection(cache));

      start(service);
      service.doService(msg);
      fail();
    } catch (ServiceException expected) {

    }
    finally {
      stop(service);
    }
  }

  @Override
  protected AddToCacheService createService() {
    return new AddToCacheService();
  }

  private AddToCacheService createServiceForTests() {
    AddToCacheService service = createService();
    CacheEntryEvaluator eval = new CacheEntryEvaluator();

    eval.setKeyTranslator(new MetadataCacheValueTranslator("JMSCorrelationID"));
    eval.setValueTranslator(new JmsReplyToCacheValueTranslator());
    service.addCacheEntryEvaluator(eval);

    return service;
  }

  @Override
  protected AddToCacheService createServiceForExamples() {
    return BasicCacheExampleGenerator.createAddToCacheService();
  }

  @Override
  protected AdaptrisMessage createMessage(String payload, Collection<MetadataElement> metadata) {
    AdaptrisMessage msg = super.createMessage(payload, metadata);
    msg.getObjectHeaders().put(JmsConstants.OBJ_JMS_REPLY_TO_KEY, new Queue() {
      @Override
      public String getQueueName() throws JMSException {
        return QUEUE_NAME;
      }
    });
    return msg;
  }

}
