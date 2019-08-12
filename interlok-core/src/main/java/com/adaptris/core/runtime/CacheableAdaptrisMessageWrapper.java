/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.runtime;

import org.apache.commons.lang3.builder.HashCodeBuilder;

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
