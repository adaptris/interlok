package com.adaptris.core.services.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.util.EnumSet;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;

public class RemoveKeyFromCacheTest extends SingleKeyCacheCase {


  @Test
  public void testDoService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");

    Cache cache = createCacheInstanceForTests();
    RemoveKeyFromCache service =
        new RemoveKeyFromCache().withKey("%message{%uniqueId}").withConnection(new CacheConnection().withCacheInstance(cache));
    try {
      start(service);
      cache.put(msg.getUniqueId(), msg.getContent());
      assertNotNull(cache.get(msg.getUniqueId()));
      service.doService(msg);
      assertNull(cache.get(msg.getUniqueId()));
    } finally {
      stop(service);
    }
  }

  @Test
  public void testDoService_WithError() throws Exception {

    AdaptrisMessage msg = new DefectiveMessageFactory(EnumSet.of(WhenToBreak.METADATA_GET)).newMessage("Hello World");
    msg.addMetadata("metadataKey", "value");
    Cache cache = createCacheInstanceForTests();
    RemoveKeyFromCache service =
        new RemoveKeyFromCache().withKey("%message{metadataKey}").withConnection(new CacheConnection().withCacheInstance(cache));
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
  protected RemoveKeyFromCache retrieveObjectForSampleConfig() {
    return new RemoveKeyFromCache().withKey("%message{%uniqueId}")
        .withConnection(new CacheConnection().withCacheInstance(createCacheInstanceForTests()));
  }

}
