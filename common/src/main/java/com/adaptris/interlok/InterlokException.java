package com.adaptris.interlok;



public class InterlokException extends Exception {

  private static final long serialVersionUID = 2015082101L;
  
  public InterlokException() {
    super();
  }

  public InterlokException(Throwable cause) {
    super(cause);
  }

  public InterlokException(String description) {
    super(description);
  }

  public InterlokException(String description, Throwable cause) {
    super(description, cause);
  }
}
