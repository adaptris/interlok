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

package com.adaptris.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
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
    setPayload(orig.getContent(), orig.getContentEncoding());
  }

  public SerializableAdaptrisMessage(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  public SerializableAdaptrisMessage(String uniqueId, String payload) {
    this();
    setUniqueId(uniqueId);
    setContent(payload);
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

  public void setPayload(String payload, String payloadEncoding) {
    setContent(payload);
    setContentEncoding(payloadEncoding);
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
        for (MetadataElement e : set) {
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
  public void addMetadata(String key, String value) {
    this.addMetadata(new MetadataElement(key, value));
  }

  /**
   * Add a single item of metadata.
   * 
   * @param e the metadata to add.
   */
  public void addMetadata(MetadataElement e) {
    synchronized (metadata) {
      if (containsKey(e.getKey())) {
        removeMetadata(e);
      }
      // Make sure that when we do the actual add, we turn it into a real key/value pair
      // this avoids additional class="" when you serialize using XStream.
      metadata.addKeyValuePair(new KeyValuePair(e.getKey(), e.getValue()));
    }
  }

  public void removeMetadata(MetadataElement element) {
    synchronized (metadata) {
      metadata.removeKeyValuePair(element);
    }
  }

  public boolean containsKey(String key) {
    return metadata.contains(new KeyValuePair(key, ""));
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
  public void removeMessageHeader(String key) {
    removeMetadata(new MetadataElement(key, ""));
  }

  @Override
  public Map<String, String> getMessageHeaders() {
    return Collections.unmodifiableMap(toMap(metadata));
  }

  @Override
  public void setMessageHeaders(Map<String, String> arg0) {
    setMetadata(new KeyValuePairSet(arg0));
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
    return new EqualsBuilder().append(getContent(), rhs.getContent()).append(getContentEncoding(), rhs.getContentEncoding())
        .append(getUniqueId(), rhs.getUniqueId()).append(getMetadata(), rhs.getMetadata()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(13, 17).append(getContent()).append(getContentEncoding()).append(getUniqueId())
        .append(getMetadata()).toHashCode();
  }

  private Map<String, String> toMap(KeyValuePairBag bag) {
    Map<String, String> result = new HashMap<>(bag.size());
    for (KeyValuePair kvp : bag) {
      result.put(kvp.getKey(), kvp.getValue());
    }
    return result;
  }

}
