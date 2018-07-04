package com.adaptris.core.services.cache;

import com.adaptris.core.ServiceCase;

public abstract class CacheServiceExample extends ServiceCase {

  private static final String BASE_DIR_KEY = "CacheServiceExamples.baseDir";

  public CacheServiceExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

}
