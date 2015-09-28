package com.adaptris.core.stubs;

import java.util.HashMap;
import java.util.Map;

import com.adaptris.interlok.types.SerializableMessage;

public class StubSerializableMessage implements SerializableMessage {
  private static final long serialVersionUID = 2015082101L;

  private String uniqueId;
  private String payload;
  private String payloadEncoding;
  private Map<String, String> messageHeaders;

  public StubSerializableMessage() {
    messageHeaders = new HashMap<String, String>();
  }

  @Override
  public void addMessageHeader(String key, String value) {
    messageHeaders.put(key, value);
  }


  @Override
  public void removeMessageHeader(String key) {
    messageHeaders.remove(key);
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Override
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public String getContent() {
    return payload;
  }

  @Override
  public void setContent(String payload) {
    this.payload = payload;
  }

  @Override
  public String getContentEncoding() {
    return payloadEncoding;
  }

  @Override
  public void setContentEncoding(String payloadEncoding) {
    this.payloadEncoding = payloadEncoding;
  }

  @Override
  public Map<String, String> getMessageHeaders() {
    return messageHeaders;
  }

  @Override
  public void setMessageHeaders(Map<String, String> hdrs) {
    this.messageHeaders = hdrs;
  }



}
