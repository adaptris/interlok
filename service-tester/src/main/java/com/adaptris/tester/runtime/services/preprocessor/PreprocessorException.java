package com.adaptris.tester.runtime.services.preprocessor;

public class PreprocessorException extends Exception {

  public PreprocessorException() {
    super();
  }

  public PreprocessorException(String message){
    super(message);
  }

  public PreprocessorException(Exception e) {
    super(e);
  }

  public PreprocessorException(String message, Exception e){
    super(message, e);
  }
}