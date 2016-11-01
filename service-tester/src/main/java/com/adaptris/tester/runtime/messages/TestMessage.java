package com.adaptris.tester.runtime.messages;

import java.util.HashMap;
import java.util.Map;

public class TestMessage{

  private Map<String, String> messageHeaders;
  private String payload;

  public TestMessage(){
    this.messageHeaders = new HashMap<String, String>();
    this.payload = "";
  }

  public TestMessage(Map<String, String> messageHeaders, String payload){
    this.messageHeaders = messageHeaders;
    this.payload = payload;
  }

  public Map<String, String> getMessageHeaders() {
    return messageHeaders;
  }

  public void setMessageHeaders(Map<String, String> messageHeaders) {
    this.messageHeaders = messageHeaders;
  }

  public void addMessageHeader(String key, String value){
    messageHeaders.put(key, value);
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  @Override
  public String toString() {
    return "Metadata: " + getMessageHeaders() + "\nPayload: " + getPayload();
  }
}
