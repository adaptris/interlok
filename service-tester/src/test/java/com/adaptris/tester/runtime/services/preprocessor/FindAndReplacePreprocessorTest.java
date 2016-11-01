package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import org.junit.Test;

import static org.junit.Assert.*;

public class FindAndReplacePreprocessorTest {
  @Test
  public void execute() throws Exception {
    FindAndReplacePreprocessor preprocessor = new FindAndReplacePreprocessor();
    KeyValuePairSet set = new KeyValuePairSet();
    set.add(new KeyValuePair("foo", "bar"));
    preprocessor.setReplacementKeys(set);
    String result = preprocessor.execute("Hello foo");
    assertEquals("Hello bar", result);

  }

}