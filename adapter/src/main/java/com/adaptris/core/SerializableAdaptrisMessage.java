package com.adaptris.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The SerializableAdaptrisMessage simply represents an AdaptrisMessage that can be serialized.
 * <p>
 * Many of the AdaptrisMessage's class members have been removed to facilitate serialization, such as object metadata - considering
 * any object may be placed in object metadata, we could never be sure the message would serialize.
 * </p>
 * 
 * @config serializable-adaptris-message
 * 
 */
@XStreamAlias("serializable-adaptris-message")
public class SerializableAdaptrisMessage implements Serializable {

  private static final long serialVersionUID = 20121213141400L;

  /**
   * The unique ID that represents one instance of a SerializableAdaptrisMessage
   */
  private String uniqueId;
  /**
   * A string representation of the AdaptrisMessage
   */
  private String payload;

  /**
   * The character-set of the payload.
   */
  private String payloadEncoding;

  /**
   * A pure copy of the AdaptrisMessage
   */
  private KeyValuePairSet metadata;

  public SerializableAdaptrisMessage() {
    metadata = new KeyValuePairSet();
  }

  public SerializableAdaptrisMessage(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  public SerializableAdaptrisMessage(String uniqueId, String payload) {
    this();
    setUniqueId(uniqueId);
    setPayload(payload);
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public KeyValuePairSet getMetadata() {
    return metadata;
  }

  public void setMetadata(KeyValuePairSet metadata) {
    this.metadata = metadata;
  }

  public synchronized void setMetadata(Set<MetadataElement> set) {
    if (set != null) {
      for (Iterator<MetadataElement> i = set.iterator(); i.hasNext();) {
        MetadataElement e = i.next();
        addMetadata(e);
      }
    }
  }

  public synchronized void addMetadata(String key, String value) {
    this.addMetadata(new MetadataElement(key, value));
  }

  /** @see AdaptrisMessage#addMetadata(MetadataElement) */
  public synchronized void addMetadata(MetadataElement e) {
    if (metadata.contains(e)) {
      removeMetadata(e);
    }
    metadata.addKeyValuePair(e);
  }

  public void removeMetadata(MetadataElement element) {
    metadata.removeKeyValuePair(element);
  }

  public boolean containsKey(String key) {
    return metadata.contains(new MetadataElement(key, ""));
  }

  public String getMetadataValue(String key) { // is case-sensitive
    if (key != null) {
      return metadata.getValue(key);
    }
    return null;
  }

  public String getPayloadEncoding() {
    return payloadEncoding;
  }

  public void setPayloadEncoding(String payloadEncoding) {
    this.payloadEncoding = payloadEncoding;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }
    if (object == this) {
      return true;
    }
    if (object.getClass() != getClass()) {
      return false;
    }
    SerializableAdaptrisMessage rhs = (SerializableAdaptrisMessage) object;
    return new EqualsBuilder().append(getPayload(), rhs.getPayload()).append(getPayloadEncoding(), rhs.getPayloadEncoding())
        .append(getUniqueId(), rhs.getUniqueId()).append(getMetadata(), rhs.getMetadata()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(13, 17).append(getPayload()).append(getPayloadEncoding()).append(getUniqueId())
        .append(getMetadata()).toHashCode();
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();

    buffer.append("ID = " + getUniqueId() + "\n");
    buffer.append("Payload Encoding = " + getPayloadEncoding() + "\n");
    buffer.append("Payload = " + getPayload() + "\n");
    buffer.append("Metadata = " + getMetadata().toString());

    return buffer.toString();
  }
}
