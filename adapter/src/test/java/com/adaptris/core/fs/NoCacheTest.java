package com.adaptris.core.fs;


/**
 * @author lchan
 * @author $Author: lchan $
 */
public class NoCacheTest extends ItemCacheCase {

  public NoCacheTest(String name) {
    super(name);
  }

  @Override
  protected void assertCache(ProcessedItemCache cache, int count) {
    assertNotNull(cache);
    assertEquals(0, cache.size());
  }

  @Override
  protected ProcessedItemCache createCache() throws Exception {
    return new NoCache();
  }
}
