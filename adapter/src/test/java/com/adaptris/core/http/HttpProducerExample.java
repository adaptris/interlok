package com.adaptris.core.http;

import com.adaptris.core.ProducerCase;

public abstract class HttpProducerExample extends ProducerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "HttpProducerExamples.baseDir";

  public HttpProducerExample(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }
}
