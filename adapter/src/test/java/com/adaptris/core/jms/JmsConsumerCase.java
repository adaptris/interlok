package com.adaptris.core.jms;

import com.adaptris.core.ConsumerCase;

public abstract class JmsConsumerCase extends ConsumerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "JmsConsumerExamples.baseDir";

  public JmsConsumerCase(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }
}
