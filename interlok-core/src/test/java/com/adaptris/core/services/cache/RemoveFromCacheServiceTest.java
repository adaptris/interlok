package com.adaptris.core.services.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.cache.Cache;
import com.adaptris.interlok.junit.scaffolding.services.BasicCacheExampleGenerator;

public class RemoveFromCacheServiceTest extends RetrieveFromCacheServiceTest {

  @Override
  @Test
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

  @Override
  protected RemoveFromCacheService createServiceForExamples() {
    return BasicCacheExampleGenerator.createRemoveFromCache();
  }
}
