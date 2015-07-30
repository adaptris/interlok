package com.adaptris.core.services.metadata;

import com.adaptris.core.ServiceCase;

public abstract class MetadataServiceExample extends ServiceCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "MetadataServiceExamples.baseDir";

  public MetadataServiceExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

}
