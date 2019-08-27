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

package com.adaptris.core.stubs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.interlok.types.SerializableMessage;

public class StubSerializableMessage implements SerializableMessage {
  private static final long serialVersionUID = 2015082101L;

  private String uniqueId;
  private String payload;
  private String payloadEncoding;
  private String nextServiceId;
  private Map<String, String> messageHeaders;

  public StubSerializableMessage() {
    messageHeaders = new HashMap<String, String>();
    setNextServiceId(null);
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

  @Override
  public String getNextServiceId() {
    return nextServiceId;
  }

  @Override
  public void setNextServiceId(String next) {
    nextServiceId = StringUtils.defaultIfEmpty(next, "");
  }



}
