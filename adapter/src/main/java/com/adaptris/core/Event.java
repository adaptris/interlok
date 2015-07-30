package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Contains behaviour common to all <code>Event</code>s in the framework.
 * </p><p>
 * Note on unique ids: initially UID was generated for each new Event instance
 * and immutable.  I.e. a 'system' level UID.  This meant that if an Event
 * was marshalled then unmarshalled it would have a different UID. This has been
 * changed to an 'application' level UID, which doesn't change during
 * marshalling / unmarshalling.
 * </p>
 */
public abstract class Event {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private String uniqueId;
  private String destinationId;
  private String sourceId;
  private transient String nameSpace = EventNameSpaceConstants.EVENT;
  private long creationTime;

  protected Event() {
    setCreationTime(System.currentTimeMillis());
  }

  /**
   * <p>
   * Creates a new instance with passed name space.
   * </p>
   * @param s the name space of this <code>Event</code>, may not be null or
   * empty
   */
  protected Event(String s) {
    this();
    if (isEmpty(s)) {
      throw new IllegalArgumentException("illegal param [" + s + "]");
    }
    nameSpace = s;
  }

  /**
   * Creates the hierarchical name space for this event.
   * <p>
   * The namespace is made up of the configured (or default namespace for the event, plus the source adapter unique id, plus the
   * destination adapter id. These elements are separated by '.', the configured name space may contain '.' delimiters as required
   * and destination id may be null.
   * </p>
   * 
   * @return the <code>Event</code>'s name space
   */
  public String createNameSpace() {
    StringBuffer result = new StringBuffer(getNameSpace());
    result.append(".");
    result.append(getSourceId());
    result.append(".");
    result.append(getDestinationId());

    return result.toString();
  }

  /**
   * <p>
   * Sets the <code>Event</code>'s unique ID.
   * </p>
   * 
   * @param s the <code>Event</code>'s unique ID, may not be null or empty
   */
  public void setUniqueId(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("illegal param [" + s + "]");
    }
    uniqueId = s;
  }

  /**
   * <p>
   * Returns the <code>Event</code>'s unique ID.
   * </p>
   * @return the <code>Event</code>'s unique ID
   */
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * <p>
   * Sets the unique ID of the destination <code>Adapter</code> (or other
   * entity such as a GUI).  This is optional.
   * </p>
   * @param s the unique ID of the destination <code>Adapter</code>
   */
  public void setDestinationId(String s) {
    destinationId = s;
  }

  /**
   * <p>
   * Returns the unique ID of the destination <code>Adapter</code> (or other
   * entity such as a GUI).
   * </p>
   * @return the unique ID of the destination <code>Adapter</code>
   */
  public String getDestinationId() {
    return destinationId;
  }

  /**
   * <p>
   * Sets the unique ID of the source <code>Adapter</code> (or other
   * entity such as a GUI).
   * </p>
   * @param s the unique ID of the source <code>Adapter</code>
   */
  public void setSourceId(String s) {
    sourceId = s;
  }

  /**
   * <p>
   * Returns the unique ID of the source <code>Adapter</code> (or other
   * entity such as a GUI).
   * </p>
   * @return the unique ID of the source <code>Adapter</code>
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * <p>
   * Sets the creation time of this <code>Event</code>.
   * </p>
   * @param l the creation time of this <code>Event</code>
   */
  public void setCreationTime(long l) {
    creationTime = l;
  }

  /**
   * <p>
   * Returns the creation time of this <code>Event</code>.
   * </p>
   * @return the creation time of this <code>Event</code>
   */
  public long getCreationTime() {
    return creationTime;
  }

  /**
   * <p>
   * Returns the immutable name space of this <code>Event</code>.
   * </p>
   * @return the namespace of this <code>Event</code>
   */
  public String getNameSpace() {
    return nameSpace;
  }
}
