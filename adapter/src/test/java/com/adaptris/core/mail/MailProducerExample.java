package com.adaptris.core.mail;

import com.adaptris.core.ProducerCase;

public abstract class MailProducerExample extends ProducerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "MailProducerExamples.baseDir";

  public MailProducerExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

}
