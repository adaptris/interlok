package com.adaptris.tester.runtime.services.preprocessor;

public class NullPreprocessorTest extends PreprocessorCase {
  public NullPreprocessorTest(String name) {
    super(name);
  }

  public void testExecute() throws Exception {
    String value  = "value";
    String result = createPreprocessor().execute(value);
    assertEquals(value, result);
  }

  @Override
  protected Preprocessor createPreprocessor() {
    return new NullPreprocessor();
  }
}