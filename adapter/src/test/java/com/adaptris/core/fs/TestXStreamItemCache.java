package com.adaptris.core.fs;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.XStreamMarshaller;

public class TestXStreamItemCache extends MarshallingCacheCase {

  public TestXStreamItemCache(String name) {
    super(name);
  }

  @Override
  protected XStreamItemCache createCache() throws Exception {
    XStreamItemCache cache = new XStreamItemCache(persistentStore.getCanonicalPath());
    return cache;
  }
  
  @Override
  protected AdaptrisMarshaller createMarshaller() throws Exception {
    return new XStreamMarshaller();
  }
}
