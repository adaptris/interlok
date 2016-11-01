package com.adaptris.tester.runtime.services.preprocessor;


import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("null-preprocessor")
public class NullPreprocessor implements Preprocessor{

  @Override
  public String execute(String input) throws PreprocessorException {
    return input;
  }
}
