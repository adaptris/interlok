package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.core.CoreException;
import com.adaptris.core.xinclude.XincludePreProcessor;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xinclude-preprocessor")
public class XincludePreprocessor implements Preprocessor {
  @Override
  public String execute(String input) throws PreprocessorException {
    try {
      XincludePreProcessor processor = new XincludePreProcessor(new KeyValuePairSet());
      return processor.process(input);
    } catch (CoreException e) {
      throw new PreprocessorException("Failed to perform xinclude", e);
    }
  }
}
