package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import org.junit.Test;

public class FindAndReplacePreprocessorTest extends PreprocessorCase {
  public FindAndReplacePreprocessorTest(String name) {
    super(name);
  }

  @Test
  public void testExecute() throws Exception {
    String result = createPreprocessor().execute("Hello foo");
    assertEquals("Hello bar", result);

  }

  protected FindAndReplacePreprocessor createPreprocessor(){
    FindAndReplacePreprocessor preprocessor = new FindAndReplacePreprocessor();
    KeyValuePairSet set = new KeyValuePairSet();
    set.addKeyValuePair(new KeyValuePair("foo", "bar"));
    preprocessor.setReplacementKeys(set);
    return preprocessor;
  }


}