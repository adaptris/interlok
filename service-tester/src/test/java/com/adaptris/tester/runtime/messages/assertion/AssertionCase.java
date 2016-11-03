package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.tester.STExampleConfigCase;

public abstract class AssertionCase extends STExampleConfigCase {

  public static final String BASE_DIR_KEY = "AssertionCase.baseDir";

  public AssertionCase(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createAssertion();
  }

  protected abstract Assertion createAssertion();
}
