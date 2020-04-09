package com.adaptris.core.cache;

import org.junit.Test;

public class CacheEventLoggerTest {

  @Test
  public void testItemEvicted() {
    CacheEventLogger logger = new CacheEventLogger();
    logger.itemEvicted("key", new Object());
  }

  @Test
  public void testItemExpired() {
    CacheEventLogger logger = new CacheEventLogger();
    logger.itemExpired("key", new Object());
  }

  @Test
  public void testItemPut() {
    CacheEventLogger logger = new CacheEventLogger();
    logger.itemPut("key", new Object());
  }

  @Test
  public void testItemRemoved() {
    CacheEventLogger logger = new CacheEventLogger();
    logger.itemRemoved("key", new Object());
  }

  @Test
  public void testItemUpdated() {
    CacheEventLogger logger = new CacheEventLogger();
    logger.itemUpdated("key", new Object());
  }

}
