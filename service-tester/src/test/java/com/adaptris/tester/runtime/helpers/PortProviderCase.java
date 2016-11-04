package com.adaptris.tester.runtime.helpers;

import com.adaptris.tester.STExampleConfigCase;

public abstract class PortProviderCase extends STExampleConfigCase {

  public static final String BASE_DIR_KEY = "PortProviderCase.baseDir";

  public PortProviderCase(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createPortProvider();
  }

  protected abstract PortProvider createPortProvider();
}
