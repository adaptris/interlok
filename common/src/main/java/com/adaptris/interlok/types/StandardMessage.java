package com.adaptris.interlok.types;

import java.io.Serializable;
import java.util.Properties;

public interface StandardMessage extends Serializable {

  public String getUniqueId();

  public void setUniqueId(String uniqueId);

  public String getPayload();

  public void setPayload(String payload);

  public Properties getMessageHeaders();

  public void setMessageHeaders(Properties metadata);

  public void addMessageHeader(String key, String value);

  public String getPayloadEncoding();

  public void setPayloadEncoding(String payloadEncoding);

}
