package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.tester.STExampleConfigCase;

public abstract class PreprocessorCase extends STExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "PreprocessorCase.baseDir";

  public PreprocessorCase(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createPreprocessor();
  }

  protected abstract Preprocessor createPreprocessor();

}
