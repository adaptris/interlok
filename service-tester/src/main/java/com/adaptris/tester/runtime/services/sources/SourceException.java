package com.adaptris.tester.runtime.services.sources;

public class SourceException extends Exception {

  public SourceException() {
    super();
  }

  public SourceException(Exception e) {
    super(e);
  }

  public SourceException(String message, Exception e){
    super(message, e);
  }
}