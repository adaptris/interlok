package com.adaptris.core;

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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.IdGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

/**
 * <p>
 * Standard implementation of {@link AdaptrisMessage} interface.
 * </p>
 *
 * @see DefaultMessageFactory
 * @see AdaptrisMessageFactory
 * @see AdaptrisMessage
 * @author hfraser
 * @author $Author: lchan $
 */
public abstract class AdaptrisMessageImp implements AdaptrisMessage, Cloneable {

  private IdGenerator guidGenerator;
  private transient Logger log = LoggerFactory.getLogger(AdaptrisMessage.class);

  // persistent fields
  private String uniqueId;
  private KeyValuePairSet metadata;
  private String charEncoding;

  // in memory only e.g. lost on send or persist
  private MessageLifecycleEvent messageLifeCycle;
  private Map objectMetadata;
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
    objectMetadata = new HashMap();
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

  /** @see AdaptrisMessage#setCharEncoding(String) */
  @Override
  public void setCharEncoding(String charEnc) {
      charEncoding = charEnc;
  }

  /** @see AdaptrisMessage#getCharEncoding() */
  @Override
  public String getCharEncoding() {
    return charEncoding;
  }

  /** @see AdaptrisMessage#containsKey(String) */
  @Override
  public boolean containsKey(String key) {
    return metadata.contains(new KeyValuePair(key, ""));
  }

  /** @see AdaptrisMessage#addMetadata(String, String) */
  @Override
  public synchronized void addMetadata(String key, String value) {
    this.addMetadata(new MetadataElement(key, value));
  }

  /** @see AdaptrisMessage#addMetadata(MetadataElement) */
  @Override
  public synchronized void addMetadata(MetadataElement e) {
    if (metadata.contains(e)) {
      removeMetadata(e);
    }
    metadata.addKeyValuePair(e);
  }

  /** @see AdaptrisMessage#removeMetadata(MetadataElement) */
  @Override
  public void removeMetadata(MetadataElement element) {
    metadata.removeKeyValuePair(element);
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

  /** @see AdaptrisMessage#clearMetadata() */
  @Override
  public synchronized void clearMetadata() {
    metadata = new KeyValuePairSet(); // overwrite with empty
  }

  /** @see AdaptrisMessage#getMetadataValue(String) */
  @Override
  public String getMetadataValue(String key) { // is case-sensitive
    if (key != null) {
      return metadata.getValue(key);
    }
    return null;
  }

  /** @see AdaptrisMessage#getMetadata(String) */
  @Override
  public MetadataElement getMetadata(String key) {
    if (key != null && containsKey(key)) {
      return new MetadataElement(metadata.getKeyValuePair(key));
    }
    return null;
  }

  /** @see AdaptrisMessage#getMetadata() */
  @Override
  public Set<MetadataElement> getMetadata() {
    Set<MetadataElement> newSet = new HashSet<MetadataElement>();
    for (Iterator i = metadata.getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair kp = (KeyValuePair) i.next();
      newSet.add(new MetadataElement(kp.getKey(), kp.getValue()));
    }
    return newSet;
  }

  /** @see AdaptrisMessage#getUniqueId() */
  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  /** @see AdaptrisMessage#setUniqueId(String) */
  @Override
  public void setUniqueId(String s) {
    uniqueId = s;
    messageLifeCycle.setMessageUniqueId(getUniqueId());
  }

  @Override
  public Reader getReader() throws IOException {
    return getCharEncoding() != null ? new InputStreamReader(getInputStream(), getCharEncoding()) : new InputStreamReader(
        getInputStream());
  }

  @Override
  public Writer getWriter() throws IOException {
    return getCharEncoding() != null ? new OutputStreamWriter(getOutputStream(), getCharEncoding()) : new OutputStreamWriter(
        getOutputStream());
  }

  @Override
  public Writer getWriter(String encoding) throws IOException {
    Writer result;
    if (!isEmpty(encoding)) {
      setCharEncoding(encoding);
      result = new OutputStreamWriter(getOutputStream(), encoding);
    }
    else {
      result = getWriter();
    }
    return result;
  }

  /** @see AdaptrisMessage#addEvent(MessageEventGenerator, boolean) */
  @Override
  public void addEvent(MessageEventGenerator meg, boolean wasSuccessful) {
    String confirmationId = (String) getObjectMetadata().get(MessageEventGenerator.CONFIRMATION_ID_KEY);
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

  /**
   * @see AdaptrisMessage#addObjectMetadata(String,Object)
   */
  @Override
  public void addObjectMetadata(String key, Object object) {
    objectMetadata.put(key, object);

  }

  /** @see AdaptrisMessage#getObjectMetadata() */
  @Override
  public Map getObjectMetadata() {
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

  private MleMarker getNextMleMarker(MessageEventGenerator meg, boolean successful, String confId) {
    int seq = nextSequenceNumber();
    this.addMetadata(CoreConstants.MLE_SEQUENCE_KEY, String.valueOf(seq));
    MleMarker mleMarker = new MleMarker(meg, successful, seq, guidGenerator.create(meg), confId);
    return mleMarker;
  }

  private int nextSequenceNumber() {
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
    objMdCopy.putAll(getObjectMetadata());
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
