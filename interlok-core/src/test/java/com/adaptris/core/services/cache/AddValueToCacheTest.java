package com.adaptris.core.services.cache;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.time.Duration;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.cache.MyCacheEventListener;
import com.adaptris.core.services.cache.translators.StringPayloadCacheTranslator;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.DateFormatUtil;

public class AddValueToCacheTest extends SingleKeyCacheCase {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testDoService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");

    Cache cache = createCacheInstanceForTests();
    AddValueToCache service = new AddValueToCache().withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      service.doService(msg);
      Object value = cache.get(msg.getUniqueId());
      assertEquals("Hello World", value);
    } finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_WithError() throws Exception {
    
    AdaptrisMessage msg = new DefectiveMessageFactory(EnumSet.of(WhenToBreak.METADATA_GET)).newMessage("Hello World");
    msg.addMetadata("metadataKey", "value");
    Cache cache = createCacheInstanceForTests();
    AddValueToCache service = new AddValueToCache().withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{metadataKey}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      service.doService(msg);
      fail();
    } catch (ServiceException expected) {

    } finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_NoExpiryMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    ExpiringMapCache cache = createCacheInstanceForTests().withExpiration(new TimeInterval(1L, TimeUnit.SECONDS));
    MyCacheEventListener listener = new MyCacheEventListener();    
    cache.getEventListener().addEventListener(listener);
    AddValueToCache service =
        new AddValueToCache().withExpiry("%message{expiry}").withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      // msg.addMetadata("expiry", "a"); // empty expiry should not expire.
      service.doService(msg);
      Object value = cache.get(msg.getUniqueId());
      assertEquals("Hello World", value);
      await()
          .atMost(Duration.ofSeconds(5))
          .with()
          .pollInterval(Duration.ofMillis(100))
          .until(listener::expiredCount, greaterThanOrEqualTo(1));
      assertEquals(0, cache.size());      
    } finally {
      stop(service);
    }
  }
  

  @Test
  public void testDoService_Expiry_Unsupported() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    ExpiringMapCache cache = createCacheInstanceForTests().withExpiration(new TimeInterval(1L, TimeUnit.SECONDS));
    MyCacheEventListener listener = new MyCacheEventListener();    
    cache.getEventListener().addEventListener(listener);
    AddValueToCache service =
        new AddValueToCache().withExpiry("%message{expiry}").withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      msg.addMetadata("expiry", "a"); // unparseable expiry should not expire.
      service.doService(msg);
      Object value = cache.get(msg.getUniqueId());
      assertEquals("Hello World", value);
      await()
          .atMost(Duration.ofSeconds(5))
          .with()
          .pollInterval(Duration.ofMillis(100))
          .until(listener::expiredCount, greaterThanOrEqualTo(1));
      assertEquals(0, cache.size());      
    } finally {
      stop(service);
    }
  }
  
  @Test
  public void testDoService_RelativeExpiry() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    ExpiringMapCache cache = createCacheInstanceForTests();
    MyCacheEventListener listener = new MyCacheEventListener();    
    cache.getEventListener().addEventListener(listener);
    AddValueToCache service =
        new AddValueToCache().withExpiry("%message{expiry}").withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      msg.addMetadata("expiry", "500"); // relative expiry, 500ms
      service.doService(msg);
      Object value = cache.get(msg.getUniqueId());
      assertEquals("Hello World", value);
      
      // Default expiration is > 5 seconds.
      await()
          .atMost(Duration.ofSeconds(5))
          .with()
          .pollInterval(Duration.ofMillis(100))
          .until(listener::expiredCount, greaterThanOrEqualTo(1));
      assertEquals(0, cache.size());      
    } finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_AbsoluteExpiry_Millis() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    ExpiringMapCache cache = createCacheInstanceForTests();
    MyCacheEventListener listener = new MyCacheEventListener();    
    cache.getEventListener().addEventListener(listener);
    AddValueToCache service =
        new AddValueToCache().withExpiry("%message{expiry}").withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      // Expire in 2 seconds or so.
      long expiry = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000) + 1000;
      msg.addMetadata("expiry", String.valueOf(expiry));
      System.err.println(getName() + ":" + msg.getMetadataValue("expiry"));
      service.doService(msg);
      Object value = cache.get(msg.getUniqueId());
      assertEquals("Hello World", value);
      
      // Default expiration is > 5 seconds.
      await()
          .atMost(Duration.ofSeconds(5))
          .with()
          .pollInterval(Duration.ofMillis(100))
          .until(listener::expiredCount, greaterThanOrEqualTo(1));
      assertEquals(0, cache.size());      
    } finally {
      stop(service);
    }
  }

  
  @Test
  public void testDoService_AbsoluteExpiry_Date() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    ExpiringMapCache cache = createCacheInstanceForTests();
    MyCacheEventListener listener = new MyCacheEventListener();    
    cache.getEventListener().addEventListener(listener);
    AddValueToCache service =
        new AddValueToCache().withExpiry("%message{expiry}").withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      // Expire in 2 seconds or so.
      Date expiry = new Date(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000) + 1000);
      msg.addMetadata("expiry", DateFormatUtil.format(expiry));
      service.doService(msg);
      Object value = cache.get(msg.getUniqueId());
      assertEquals("Hello World", value);
      
      // Default expiration is > 5 seconds.
      await()
          .atMost(Duration.ofSeconds(5))
          .with()
          .pollInterval(Duration.ofMillis(100))
          .until(listener::expiredCount, greaterThanOrEqualTo(1));
      assertEquals(0, cache.size());      
    } finally {
      stop(service);
    }
  }
  
  @Override
  protected AddValueToCache retrieveObjectForSampleConfig() {
    return new AddValueToCache().withValueTranslator(new StringPayloadCacheTranslator()).withKey("%message{%uniqueId}")
        .withConnection(new CacheConnection().withCacheInstance(createCacheInstanceForTests()));
  }

}
