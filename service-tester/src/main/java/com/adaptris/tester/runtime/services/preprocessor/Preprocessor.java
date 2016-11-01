package com.adaptris.tester.runtime.services.preprocessor;

public interface Preprocessor {

  String execute(String input) throws PreprocessorException;
}
