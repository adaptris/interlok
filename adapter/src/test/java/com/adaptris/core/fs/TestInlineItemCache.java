/*
 * $Author: lchan $
 * $RCSfile: TestInlineItemCache.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/10/12 09:37:26 $
 */
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
