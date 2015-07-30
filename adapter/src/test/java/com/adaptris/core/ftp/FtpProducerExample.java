package com.adaptris.core.ftp;

import com.adaptris.core.ProducerCase;

public abstract class FtpProducerExample extends ProducerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "FtpProducerExamples.baseDir";

  public FtpProducerExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }
}
