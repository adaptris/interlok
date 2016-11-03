package com.adaptris.tester.runtime.services.sources;

public class SourceException extends Exception {

  public SourceException(String message, Exception e){
    super(message, e);
  }
}