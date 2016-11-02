package com.adaptris.tester.runtime.services.preprocessor;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class VarSubPropsPreprocessorTest {
  @Test
  public void execute() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("foo", "bar");
    Preprocessor p = new VarSubPropsPreprocessor(properties);
    String result = p.execute("Hello ${foo}");
    assertEquals("Hello bar", result);
  }

}