package com.adaptris.core.http;

import com.adaptris.core.ConsumerCase;

public abstract class HttpConsumerExample extends ConsumerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "HttpConsumerExamples.baseDir";

  public HttpConsumerExample(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }
}
