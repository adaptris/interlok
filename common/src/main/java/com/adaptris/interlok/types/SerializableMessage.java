package com.adaptris.interlok.types;

import java.io.Serializable;
import java.util.Map;

public interface SerializableMessage extends Serializable {

  public String getUniqueId();

  public void setUniqueId(String uniqueId);

  public String getPayload();

  public void setPayload(String payload);

  public Map<String, String> getMessageHeaders();

  public void setMessageHeaders(Map<String, String> metadata);

  public void addMessageHeader(String key, String value);

  public String getPayloadEncoding();

  public void setPayloadEncoding(String payloadEncoding);

}
