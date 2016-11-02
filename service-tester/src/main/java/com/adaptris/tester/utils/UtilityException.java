package com.adaptris.tester.utils;

public class UtilityException extends Exception {

  public UtilityException(){
    super();
  }

  public UtilityException(String message){
    super(message);
  }

  public UtilityException(Exception e) {
    super(e);
  }
}
