package com.adaptris.core;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The SerializableAdaptrisMessage simply represents an AdaptrisMessage that can be serialized.
 * <p>
 * Many of the AdaptrisMessage's class members have been removed to facilitate serialization, such
 * as object metadata - considering any object may be placed in object metadata, we could never be
 * sure the message would serialize. The semantics of each method will attempt to follow that
 * defined by {@link AdaptrisMessage} even though it does not implement that interface.
 * </p>
 * 
 * @config serializable-adaptris-message
 * 
 */
@XStreamAlias("serializable-adaptris-message")
public class SerializableAdaptrisMessage implements SerializableMessage {

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

  public SerializableAdaptrisMessage(SerializableMessage orig) {
    this();
    setUniqueId(orig.getUniqueId());
    setMessageHeaders(orig.getMessageHeaders());
    setPayload(orig.getPayload(), orig.getPayloadEncoding());
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

  public void setPayload(String payload, String payloadEncoding) {
    setPayload(payload);
    setPayloadEncoding(payloadEncoding);
  }

  public KeyValuePairSet getMetadata() {
    return metadata;
  }

  /**
   * Set the metadata for this message.
   * <p>
   * This overwrites all metadata in the message with the corresponding set; passing in null or an
   * empty {@link KeyValuePairSet} will remove all metadata.
   * </p>
   * 
   * @param metadata the metadata to set.
   * 
   */
  public void setMetadata(KeyValuePairSet metadata) {
    if (metadata == null) {
      this.metadata = new KeyValuePairSet();
    } else {
      this.metadata = metadata;
    }
  }

  /**
   * Adds all the associated {@link MetadataElement} as metadata.
   * <p>
   * This will overwrite any pre-existing keys, but will not remove existing metadata
   * </p>
   * 
   * @param set the metadata to add.
   * @see AdaptrisMessage#setMetadata(Set)
   */
  public void setMetadata(Set<MetadataElement> set) {
    synchronized (metadata) {
      if (set != null) {
        for (Iterator<MetadataElement> i = set.iterator(); i.hasNext();) {
          MetadataElement e = i.next();
          addMetadata(e);
        }
      }
    }
  }


  /**
   * Add a single item of metadata.
   * 
   * @param key the key
   * @param value the value
   * @see AdaptrisMessage#addMetadata(String, String)
   */
  public synchronized void addMetadata(String key, String value) {
    this.addMetadata(new MetadataElement(key, value));
  }

  /**
   * Add a single item of metadata.
   * 
   * @param e the metadata to add.
   */
  public void addMetadata(MetadataElement e) {
    // Make sure that when we do the actual add, we turn it into a real key/value pair
    // this avoids additional class="" when you serialize using XStream.
    metadata.addKeyValuePair(new KeyValuePair(e.getKey(), e.getValue()));
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

  @Override
  public void addMessageHeader(String key, String value) {
    addMetadata(new MetadataElement(key, value));
  }

  @Override
  public Properties getMessageHeaders() {
    return KeyValuePairSet.asProperties(metadata);
  }

  @Override
  public void setMessageHeaders(Properties arg0) {
    setMetadata(new KeyValuePairSet(arg0));
  }

  @Override
  public String getPayloadEncoding() {
    return payloadEncoding;
  }

  @Override
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
}
