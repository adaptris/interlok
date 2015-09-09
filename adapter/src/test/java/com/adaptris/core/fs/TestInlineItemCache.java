package com.adaptris.core.fs;


/**
 * @author lchan
 * @author $Author: lchan $
 */
public class TestInlineItemCache extends ExpiringCacheCase {


  public TestInlineItemCache(String name) {
    super(name);
  }

  @Override
  protected InlineItemCache createCache() {
    return new InlineItemCache();
  }
}
