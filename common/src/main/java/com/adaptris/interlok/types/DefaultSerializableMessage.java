package com.adaptris.interlok.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.interlok.types.SerializableMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default {@linkplain SerializableMessage} implementation for clients to use.
 * 
 * @author lchan
 * 
 */
@XStreamAlias("default-serializable-message")
@GenerateBeanInfo
public class DefaultSerializableMessage implements SerializableMessage {
  private static final long serialVersionUID = 2015082401L;

  private Map<String, String> messageHeaders;
  @AutoPopulated
  private String uniqueId;
  private String payload;
  private String payloadEncoding;


  public DefaultSerializableMessage() {
    messageHeaders = new HashMap<>();
    setUniqueId(UUID.randomUUID().toString());
  }

  @Override
  public void addMessageHeader(String key, String val) {
    messageHeaders.put(key, val);
  }


  @Override
  public void removeMessageHeader(String key) {
    messageHeaders.remove(key);
  }


  @Override
  public Map<String, String> getMessageHeaders() {
    return messageHeaders;
  }

  /**
   * Set the message headers associated with the message.
   * 
   * @param hdrs the headers, null means to clear all headers.
   */
  @Override
  public void setMessageHeaders(Map<String, String> hdrs) {
    if (hdrs == null) {
      this.messageHeaders = new HashMap<String, String>();
    } else {
      this.messageHeaders = hdrs;
    }
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
  public String getPayload() {
    return payload;
  }

  @Override
  public void setPayload(String payload) {
    this.payload = payload;
  }

  @Override
  public String getPayloadEncoding() {
    return payloadEncoding;
  }

  @Override
  public void setPayloadEncoding(String payloadEncoding) {
    this.payloadEncoding = payloadEncoding;
  }



  /**
   * Convenience method for chaining.
   * 
   * @param payload the payload
   * @return the current DefaultSerializableMessage object for method chaining
   * @see #setPayload(String)
   */
  public DefaultSerializableMessage withPayload(String payload) {
    setPayload(payload);
    return this;
  }

  /**
   * Convenience method for chaining.
   * 
   * @param uid the uniqueid.
   * @return the current DefaultSerializableMessage object for method chaining
   * @see #setUniqueId(String)
   */
  public DefaultSerializableMessage withUniqueId(String uid) {
    setUniqueId(uid);
    return this;
  }

  /**
   * Convenience method for method chaining.
   * 
   * @param enc the encoding
   * @return the current DefaultSerializableMessage object for method chaining
   */
  public DefaultSerializableMessage withPayloadEncoding(String enc) {
    setPayloadEncoding(enc);
    return this;
  }

  /**
   * Convenience method for method chaining.
   * 
   * @param hdrs the message headers
   * @return the current DefaultSerializableMessage object for method chaining
   * @see #setMessageHeaders(Map)
   */
  public DefaultSerializableMessage withMessageHeaders(Map<String, String> hdrs) {
    setMessageHeaders(hdrs);
    return this;
  }

  /**
   * Convenience method to do the same as {@link #setMessageHeaders(Map)} converting any non-string
   * keys/values into Strings.
   * 
   * @param props the properties that should become message headers; null means to clear all
   *        headers.
   * @return the current DefaultSerializableMessage object for method chaining
   * @see #setMessageHeaders(Map)
   */
  public DefaultSerializableMessage withHeadersFromProperties(Properties props) {
    if (props == null) {
      setMessageHeaders(null);
    } else {
      Map<String, String> result = new HashMap<>(props.size());
      for (Map.Entry e : props.entrySet()) {
        result.put(e.getKey().toString(), e.getValue().toString());
      }
      setMessageHeaders(result);
    }
    return this;
  }


}
