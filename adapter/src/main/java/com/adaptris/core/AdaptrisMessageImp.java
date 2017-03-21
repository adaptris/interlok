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

import static com.adaptris.core.metadata.MetadataResolver.resolveKey;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.IdGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

/**
 * <p>
 * Standard implementation of {@link com.adaptris.core.AdaptrisMessage} interface.
 * </p>
 * <p>
 * When referring to metadata items by key you can use a form of indirection by specifying a prefix to your key ("$$").<br />
 * Using this prefix on any metadata key will not perform an action (retrieve, add etc) to that metadata item but instead will look up
 * the value of that metadata key and use the value as the metadata item key to be performed on. <br />
 * Example: <br />
 * Calling <b>addMetadata("myKey", "myValue");</b><br />
 * Will create a new metadata item with the key "myKey" and the value "myValue".<br />
 * However calling <b>addMetadata("$$myKey", "myValue")</b> will lookup the value of the metadata key named "myKey" and use that value as the 
 * key for the addMetadata() method. 
 * </p>
 * 
 *
 * @see DefaultMessageFactory
 * @see AdaptrisMessageFactory
 * @see AdaptrisMessage
 * @author hfraser
 * @author $Author: lchan $
 */
public abstract class AdaptrisMessageImp implements AdaptrisMessage, Cloneable {

  // If we have %message{key1}%message{key2} group(1) is key2
  // Which is then replaced so it all works out int the end.
  private static final String RESOLVE_REGEXP = "^.*%message\\{([\\w]+)\\}.*$";

  private transient Logger log = LoggerFactory.getLogger(AdaptrisMessage.class);
  private transient Pattern resolverPattern = Pattern.compile(RESOLVE_REGEXP);

  private IdGenerator guidGenerator;
  // persistent fields
  private String uniqueId;
  private KeyValuePairSet metadata;
  private String contentEncoding;

  // in memory only e.g. lost on send or persist
  private MessageLifecycleEvent messageLifeCycle;
  private Map<Object, Object> objectMetadata;
  private String nextServiceId;
  private AdaptrisMessageFactory factory;

  private AdaptrisMessageImp() {

  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param guid a GuidGenerator that will be used to create unique-ids.
   */
  protected AdaptrisMessageImp(IdGenerator guid, AdaptrisMessageFactory fac) {
    this();
    factory = fac;
    metadata = new KeyValuePairSet();
    objectMetadata = new HashMap<>();
    guidGenerator = guid;
    messageLifeCycle = new MessageLifecycleEvent();
    messageLifeCycle.setCreationTime(System.currentTimeMillis());
    messageLifeCycle.setUniqueId(guidGenerator.create(messageLifeCycle));
    setUniqueId(guidGenerator.create(this));
    // setPayload(new byte[0]);
    nextServiceId = "";
  }

  @Override
  public AdaptrisMessageFactory getFactory() {
    return factory;
  }


  @Override
  public void setCharEncoding(String charEnc) {
      contentEncoding = charEnc;
  }

  @Override
  public String getCharEncoding() {
    return contentEncoding;
  }
  
  /** @see AdaptrisMessage#setContentEncoding(String) */
  @Override
  public void setContentEncoding(String charEnc) {
      contentEncoding = charEnc;
  }

  /** @see AdaptrisMessage#getContentEncoding() */
  @Override
  public String getContentEncoding() {
    return contentEncoding;
  }

  @Override
  public boolean containsKey(String key) {
    return metadata.contains(new KeyValuePair(resolveKey(this, key), ""));
  }
  
  /** @see AdaptrisMessage#headersContainsKey(String)*/
  @Override
  public boolean headersContainsKey(String key) {
    return metadata.contains(new KeyValuePair(resolveKey(this, key), ""));
  }

  @Override
  public synchronized void addMetadata(String key, String value) {
    this.addMessageHeader(resolveKey(this, key), value);
  }
  
  @Override
  public void addMessageHeader(String key, String value) {
    this.addMetadata(new MetadataElement(resolveKey(this, key), value));
  }

  /** @see AdaptrisMessage#addMetadata(MetadataElement) */
  @Override
  public synchronized void addMetadata(MetadataElement e) {
    if (metadata.contains(e)) {
      removeMetadata(e);
    }
    e.setKey(resolveKey(this, e.getKey()));
    metadata.addKeyValuePair(e);
  }

  /** @see AdaptrisMessage#removeMetadata(MetadataElement) */
  @Override
  public void removeMetadata(MetadataElement element) {
    element.setKey(resolveKey(this, element.getKey()));
    metadata.removeKeyValuePair(element);
  }
  
  /** @see AdaptrisMessage#removeMessageHeader(String) */
  @Override
  public void removeMessageHeader(String key) {
    metadata.remove(new MetadataElement(resolveKey(this, key), ""));
  }

  /** @see AdaptrisMessage#setMetadata(Set) */
  @Override
  public synchronized void setMetadata(Set set) {
    if (set != null) {
      for (Iterator i = set.iterator(); i.hasNext();) {
        MetadataElement e = (MetadataElement) i.next();
        addMetadata(e);
      }
    }
  }
  
  @Override
  public void setMessageHeaders(Map<String, String> metadata) {
    for(String key : metadata.values()) {
      this.addMessageHeader(key, metadata.get(key));
    }
  }

  /** @see AdaptrisMessage#clearMetadata() */
  @Override
  public synchronized void clearMetadata() {
    metadata = new KeyValuePairSet(); // overwrite with empty
  }

  /** @see AdaptrisMessage#getMetadataValue(String) */
  @Override
  public String getMetadataValue(String key) { // is case-sensitive
    if (key != null) {
      return metadata.getValue(resolveKey(this, key));
    }
    return null;
  }

  /** @see AdaptrisMessage#getMetadata(String) */
  @Override
  public MetadataElement getMetadata(String key) {
    if (key != null && containsKey(key)) {
      return new MetadataElement(metadata.getKeyValuePair(resolveKey(this, key)));
    }
    return null;
  }
  
  @Override
  public Map<String, String> getMessageHeaders() {
    Map<String, String> newSet = new HashMap<String, String>();
    for (Iterator<KeyValuePair> i = metadata.getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair kp = i.next();
      newSet.put(kp.getKey(), kp.getValue());
    }
    return newSet;
  }


  @Override
  public Set<MetadataElement> getMetadata() {
    Set<MetadataElement> newSet = new HashSet<MetadataElement>();
    for (Iterator i = metadata.getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair kp = (KeyValuePair) i.next();
      newSet.add(new MetadataElement(kp.getKey(), kp.getValue()));
    }
    return newSet;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  @Override
  public void setUniqueId(String s) {
    uniqueId = s;
    messageLifeCycle.setMessageUniqueId(getUniqueId());
  }

  @Override
  public Reader getReader() throws IOException {
    return getContentEncoding() != null ? new InputStreamReader(getInputStream(), getContentEncoding()) : new InputStreamReader(
        getInputStream());
  }

  @Override
  public Writer getWriter() throws IOException {
    return getContentEncoding() != null ? new OutputStreamWriter(getOutputStream(), getContentEncoding()) : new OutputStreamWriter(
        getOutputStream());
  }

  @Override
  public Writer getWriter(String encoding) throws IOException {
    Writer result;
    if (!isEmpty(encoding)) {
      setContentEncoding(encoding);
      result = new OutputStreamWriter(getOutputStream(), encoding);
    }
    else {
      result = getWriter();
    }
    return result;
  }

  @Override
  public void addEvent(MessageEventGenerator meg, boolean wasSuccessful) {
    String confirmationId = (String) getObjectHeaders().get(MessageEventGenerator.CONFIRMATION_ID_KEY);
    if (meg == null) {
      messageLifeCycle.addMleMarker(getNextMleMarker(new MessageEventGenerator() {
        @Override
        public String createName() {
          return "Unknown Event";
        }

        @Override
        public String createQualifier() {
          return "";
        }

        @Override
        public boolean isTrackingEndpoint() {
          return false;
        }

        @Override
        public boolean isConfirmation() {
          return false;
        }

      }, wasSuccessful, confirmationId));
    }
    else {
      messageLifeCycle.addMleMarker(getNextMleMarker(meg, wasSuccessful, confirmationId));
    }
  }

  /** @see AdaptrisMessage#getMessageLifecycleEvent() */
  @Override
  public MessageLifecycleEvent getMessageLifecycleEvent() {
    return messageLifeCycle;
  }

  /** @see AdaptrisMessage#encode(AdaptrisMessageEncoder) */
  @Override
  public byte[] encode(AdaptrisMessageEncoder encoder) throws CoreException {
    if (encoder != null) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      encoder.writeMessage(this, out);
      return out.toByteArray();
    }
    return getPayload();
  }

  @Override
  public void addObjectMetadata(String key, Object object) {
    objectMetadata.put(key, object);
  }
  
  /**
   * <p>
   * Adds an <code>Object</code> to this message as metadata. Object metadata is
   * intended to be used within a single <code>Workflow</code> only and will not
   * be encoded or otherwise transported between Workflows.
   * </p>
   *
   * @param object the <code>Object</code> to set as metadata
   * @param key the key to store this object against.
   */
  public void addObjectHeader(Object key, Object object) {
    objectMetadata.put(key, object);
  }

  @Override
  public Map getObjectMetadata() {
    return objectMetadata;
  }
  
  @Override
  public Map<Object, Object> getObjectHeaders() {
    return objectMetadata;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return this.toString(false);
  }

  @Override
  public String toString(boolean includePayload, boolean includeEvents) {
    StringBuffer result = new StringBuffer();

    result.append("[");
    result.append(this.getClass().getName());
    result.append("] uniqueId [");
    result.append(uniqueId);
    result.append("] metadata [");
    result.append(metadata);
    if (includeEvents) {
      result.append("] message events [");
      result.append(messageLifeCycle);
    }
    if (includePayload) {
      result.append("] payload [");
      result.append(getPayloadForLogging());
    }
    result.append("]");
    return result.toString();
  }

  /** @see AdaptrisMessage#toString(boolean) */
  @Override
  public String toString(boolean extended) {
    return toString(extended, false);
  }

  protected abstract String getPayloadForLogging();

  /** @see AdaptrisMessage#getNextServiceId() */
  @Override
  public String getNextServiceId() {
    return nextServiceId;
  }

  /** @see AdaptrisMessage#setNextServiceId(java.lang.String) */
  @Override
  public void setNextServiceId(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Illegal service-id [" + s + "]");
    }

    nextServiceId = s;
  }

  @Override
  public String resolve(String s) {
    if (s == null) {
      return null;
    }
    String result = s;
    Matcher m = resolverPattern.matcher(s);
    while (m.matches()) {
      String key = m.group(1);
      String metadataValue = getMetadataValue(key);
      String toReplace = "%message{" + key + "}";
      result = result.replace(toReplace, metadataValue);
      m = resolverPattern.matcher(result);
    }
    return result;
  }

  private MleMarker getNextMleMarker(MessageEventGenerator meg, boolean successful, String confId) {
    long seq = nextSequenceNumber();
    this.addMetadata(CoreConstants.MLE_SEQUENCE_KEY, String.valueOf(seq));
    MleMarker mleMarker = new MleMarker(meg, successful, seq, guidGenerator.create(meg), confId);
    return mleMarker;
  }

  private long nextSequenceNumber() {
    int result= 0;
    if (containsKey(CoreConstants.MLE_SEQUENCE_KEY)) {
      try {
        result = Integer.parseInt(metadata.getValue(CoreConstants.MLE_SEQUENCE_KEY));
      }
      catch (Exception e) {
        log.warn("Failed to assign next lifecycle marker number, resetting");
        result = 0;
      }
      result++;
    }
    // This s somewhat a fudge, because,
    // * The MessageLifecycleEvent is sent after the producer has produced
    // the message.
    // * The Producer adds a ProduceSuccess marker event after the payload has
    // been produced,
    // This means that the message, at the time it is produced, is always 1
    // step out of sync, use the firstMleMarker to fix this state of affairs
    // when we read the message back.
    if (messageLifeCycle.getMleMarkers().size() == 0) {
      result++;
    }
    return result;
  }


  /**
   * @see com.adaptris.core.AdaptrisMessage
   *      #getMetadataValueIgnoreKeyCase(java.lang.String)
   */
  @Override
  public String getMetadataValueIgnoreKeyCase(String key) {
    String result = getMetadataValue(key);

    if (result == null) { // no exact match
      for (KeyValuePair pair : metadata.getKeyValuePairs()) {
        if (pair.getKey().equalsIgnoreCase(key)) {
          result = pair.getValue();
          break;
        }
      }
    }
    return result;
  }

  /** @see Object#clone() */
  @Override
  public Object clone() throws CloneNotSupportedException {
    AdaptrisMessage result = (AdaptrisMessage) super.clone();

    Set metadataCopy = this.getMetadata(); // returns a clone
    result.clearMetadata(); // creates new empty HashSet
    result.setMetadata(metadataCopy); // copies into HashSet

    MessageLifecycleEvent mle = getMessageLifecycleEvent();
    MessageLifecycleEvent copy = (MessageLifecycleEvent) mle.clone();
    ((AdaptrisMessageImp) result).messageLifeCycle = copy;

    Map objMdCopy = new HashMap();
    objMdCopy.putAll(getObjectHeaders());
    ((AdaptrisMessageImp) result).objectMetadata = objMdCopy;

    return result;
  }

  /**
   * Copy the payload from one AdaptrisMessage to another.
   *
   * @param src the source adaptris message
   * @param dest the destination adaptris message
   * @throws IOException on exception
   */
  public static void copyPayload(AdaptrisMessage src, AdaptrisMessage dest) throws IOException {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = src.getInputStream();
      out = dest.getOutputStream();
      IOUtils.copy(in, out);
      out.flush();
    }
    finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }

  protected static boolean areEqual(String s1, String s2) {
    boolean result = false;

    if (s1 == null) {
      if (s2 == null) {
        result = true;
      }
    }
    else {
      if (s1.equals(s2)) {
        result = true;
      }
    }

    return result;
  }
}
