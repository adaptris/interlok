package com.adaptris.core.fs;

import com.adaptris.core.ProducerCase;

public abstract class FsProducerExample extends ProducerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "FsProducerExamples.baseDir";

  public FsProducerExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

}
