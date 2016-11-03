package com.adaptris.tester.runtime.services.preprocessor;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class VarSubPropsPreprocessorTest extends PreprocessorCase {

  public VarSubPropsPreprocessorTest(String name) {
    super(name);
  }

  @Test
  public void testExecute() throws Exception {
    String result = createPreprocessor().execute("Hello ${foo}");
    assertEquals("Hello bar", result);
  }

  @Override
  protected Preprocessor createPreprocessor(){
    Map<String, String> properties = new HashMap<>();
    properties.put("foo", "bar");
    return new VarSubPropsPreprocessor(properties);
  }

}