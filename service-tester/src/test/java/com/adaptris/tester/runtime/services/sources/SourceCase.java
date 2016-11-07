package com.adaptris.tester.runtime.services.sources;

import com.adaptris.tester.STExampleConfigCase;

public abstract class SourceCase extends STExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "SourceCase.baseDir";

  public SourceCase(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createSource();
  }

  protected abstract Source createSource();

}
