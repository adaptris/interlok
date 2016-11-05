package com.adaptris.tester.runtime.messages;

public class MessageException extends Exception {

  public MessageException(String message, Exception e){
    super(message, e);
  }
}