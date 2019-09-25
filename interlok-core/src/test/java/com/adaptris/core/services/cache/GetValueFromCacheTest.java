package com.adaptris.core.services.cache;

import static org.junit.Assert.assertNotEquals;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.services.cache.translators.StringPayloadCacheTranslator;;

public class GetValueFromCacheTest extends SingleKeyCacheCase {

  public void testDoService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");

    Cache cache = createCacheInstanceForTests();
    GetValueFromCache service = new GetValueFromCache().withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      cache.put(msg.getUniqueId(), "Goodbye Cruel World");

      service.doService(msg);
      assertNotEquals("Hello World", msg.getContent());
      assertEquals("Goodbye Cruel World", msg.getContent());
    } finally {
      stop(service);
    }
  }

  public void testDoService_NotFoundWithError() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    Cache cache = createCacheInstanceForTests();
    GetValueFromCache service =
        new GetValueFromCache().withExceptionIfNotFound(false).withValueTranslator(new StringPayloadCacheTranslator())
            .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      service.doService(msg);
      assertNotEquals("Hello World", msg.getContent());
    } catch (ServiceException expected) {

    } finally {
      stop(service);
    }
  }



  public void testDoService_WithError() throws Exception {
    
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    Cache cache = createCacheInstanceForTests();
    GetValueFromCache service =
        new GetValueFromCache().withExceptionIfNotFound(true).withValueTranslator(new StringPayloadCacheTranslator())
        .withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      service.doService(msg);
      fail();
    } catch (ServiceException expected) {

    } finally {
      stop(service);
    }
  }

  @Override
  protected GetValueFromCache retrieveObjectForSampleConfig() {
    return new GetValueFromCache().withValueTranslator(new StringPayloadCacheTranslator()).withKey("%message{%uniqueId}")
        .withConnection(new CacheConnection().withCacheInstance(createCacheInstanceForTests()));
  }

}
