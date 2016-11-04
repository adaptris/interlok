package com.adaptris.tester.runtime.helpers;

import com.adaptris.tester.STExampleConfigCase;

public abstract class HelperCase extends STExampleConfigCase {

  public static final String BASE_DIR_KEY = "HelperCase.baseDir";

  public HelperCase(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createHelper();
  }

  protected abstract Helper createHelper();
}
