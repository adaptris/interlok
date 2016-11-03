package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.tester.STExampleConfigCase;
import com.adaptris.util.GuidGenerator;

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
    GuidGenerator guidGenerator = new GuidGenerator();
    Assertion assertion = createAssertion();
    assertion.setUniqueId(guidGenerator.getUUID());
    return assertion;
  }

  protected abstract Assertion createAssertion();
}
