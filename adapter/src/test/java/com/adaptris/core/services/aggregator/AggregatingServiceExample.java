package com.adaptris.core.services.aggregator;

import com.adaptris.core.ServiceCase;

public abstract class AggregatingServiceExample extends ServiceCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "AggregatingServiceExamples.baseDir";

  public AggregatingServiceExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }


}
