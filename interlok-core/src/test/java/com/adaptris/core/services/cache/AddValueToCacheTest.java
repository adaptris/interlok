package com.adaptris.core.services.cache;

import java.util.EnumSet;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.services.cache.translators.StringPayloadCacheTranslator;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;;

public class AddValueToCacheTest extends SingleKeyCacheCase {

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

  @Override
  protected AddValueToCache retrieveObjectForSampleConfig() {
    return new AddValueToCache().withValueTranslator(new StringPayloadCacheTranslator()).withKey("%message{%uniqueId}")
        .withConnection(new CacheConnection().withCacheInstance(createCacheInstanceForTests()));
  }

}
