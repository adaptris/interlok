package com.adaptris.core.services;

import com.adaptris.core.ServiceCase;

public abstract class SyntaxRoutingServiceExample extends ServiceCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "SyntaxRoutingServiceExamples.baseDir";

  public SyntaxRoutingServiceExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

}
