package com.adaptris.tester.runtime.services.preprocessor;

public class PreprocessorException extends Exception {

  public PreprocessorException(String message){
    super(message);
  }

  public PreprocessorException(String message, Exception e){
    super(message, e);
  }
}