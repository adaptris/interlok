package com.adaptris.core.runtime;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.core.AdaptrisMessage;

public class CacheableAdaptrisMessageWrapper {
  
  private String messageId;
  
  private AdaptrisMessage message;
  
  public CacheableAdaptrisMessageWrapper() {
  }
  
  public CacheableAdaptrisMessageWrapper(String messageId, AdaptrisMessage message) {
    this.setMessageId(messageId);
    this.setMessage(message);
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public AdaptrisMessage getMessage() {
    return message;
  }

  public void setMessage(AdaptrisMessage message) {
    this.message = message;
  }
  
  @Override
  public boolean equals(Object object) {
    if (object == null) 
      return false;
    if (object == this) 
      return true;
    if (object.getClass() != getClass())
      return false;
    
    return (((CacheableAdaptrisMessageWrapper) object).getMessageId().equals(this.getMessageId()));
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(19, 31).append(this.getMessageId()).hashCode();
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Message: [" + this.getMessageId() + "]\n");
    sb.append(this.getMessage());
    
    return sb.toString();
  }
}
