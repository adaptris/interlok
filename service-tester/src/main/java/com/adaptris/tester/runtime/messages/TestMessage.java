package com.adaptris.tester.runtime.messages;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.HashMap;
import java.util.Map;

public class TestMessage{

  @XStreamOmitField
  private Map<String, String> messageHeaders;
  @XStreamOmitField
  private String payload;

  public TestMessage(){
    setMessageHeaders(new HashMap<String, String>());
    setPayload("");
  }

  public TestMessage(Map<String, String> messageHeaders, String payload){
    setMessageHeaders(messageHeaders);
    setPayload(payload);
  }

  public Map<String, String> getMessageHeaders()  {
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
