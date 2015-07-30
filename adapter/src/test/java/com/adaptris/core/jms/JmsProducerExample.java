package com.adaptris.core.jms;

import com.adaptris.core.ProducerCase;

public abstract class JmsProducerExample extends ProducerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "JmsProducerExamples.baseDir";

  public JmsProducerExample(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }
}
