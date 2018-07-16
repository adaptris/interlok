package com.adaptris.core.services.cache;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.cache.Cache;

public class RemoveFromCacheServiceTest extends RetrieveFromCacheServiceTest {

  @Override
  public void testDoService() throws Exception {
    AdaptrisMessage msg = createMessage("Hello World", Arrays.asList(new MetadataElement[]
    {
      new MetadataElement(SRC_METADATA_KEY, LOOKUP_VALUE)
    }));

    Cache cache = createCacheInstanceForTests();
    CacheServiceBase service = createServiceForTests();
    try {
      service.setConnection(new CacheConnection(cache));
      start(service);

      // Now add the TARGET DATA to the cache.
      cache.put(LOOKUP_VALUE, LOOKED_UP_VALUE);
      service.doService(msg);
      assertTrue(msg.headersContainsKey(TARGET_METADATA_KEY));
      assertEquals(LOOKED_UP_VALUE, msg.getMetadataValue(TARGET_METADATA_KEY));

      // The service should have removed it.
      assertFalse(cache.getKeys().contains(LOOKUP_VALUE));
    }
    finally {
      stop(service);
    }
  }


  @Override
  protected RemoveFromCacheService createService() {
    return new RemoveFromCacheService();
  }
}
