package com.adaptris.core;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Records information about activities (generally {@link Service} implementations) performed on a {@link AdaptrisMessage} during a
 * workflow.
 * </p>
 * 
 * @config mle-marker
 * 
 * @see MessageLifecycleEvent
 */
@XStreamAlias("mle-marker")
public class MleMarker implements Cloneable, Serializable {

  private static final long serialVersionUID = 2013110501L;

  private String uniqueId;
  private String name;
  private String qualifier;
  private String confirmationId;
  private boolean wasSuccessful;
  private boolean isTrackingEndpoint;
  private boolean isConfirmation;
  private int sequenceNumber;
  private long creationTime;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MleMarker() {
    setQualifier("");
    setName("");
    setCreationTime(System.currentTimeMillis());
    setWasSuccessful(false);
    setSequenceNumber(-1);
  }

  public MleMarker(MessageEventGenerator meg, boolean success, int seq, String uniqueId, String confirmationId) {
    this();
    setName(meg.createName());
    setQualifier(meg.createQualifier());
    setIsConfirmation(meg.isConfirmation());
    setIsTrackingEndpoint(meg.isTrackingEndpoint());
    setWasSuccessful(success);
    setSequenceNumber(seq);
    setUniqueId(uniqueId);
    setConfirmationId(confirmationId);
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param s the name of the 'event'
   * @param b true if successful
   * @param seq the sequence number
   * @param id the Unique id
   */
  public MleMarker(String s, boolean b, int seq, String id) {
    this();
    setName(s);
    setWasSuccessful(b);
    setSequenceNumber(seq);
    setUniqueId(id);
  }

  /**
   * Set the creation time for this marker.
   *
   * @param l the creation time.
   */
  public void setCreationTime(long l) {
    creationTime = l;
  }

  /**
   * Get the creation time.
   *
   * @return the creation time.
   */
  public long getCreationTime() {
    return creationTime;
  }

  /**
   * Set the name of this marker.
   *
   * @param s the name of the marker, may not be null
   * @see MessageEventGenerator#createName()
   */
  public void setName(String s) {
    if (s == null) {
      throw new IllegalArgumentException("param is null");
    }
    name = s;
  }

  /**
   * Set the sequence number for this event.
   *
   * @param i the sequence number
   */
  public void setSequenceNumber(int i) {
    sequenceNumber = i;
  }

  /**
   * Get the sequence number for this marker.
   *
   * @return the sequence number.
   */
  public int getSequenceNumber() {
    return sequenceNumber;
  }

  /**
   * <p>
   * Returns the name of the 'event'.
   * </p>
   *
   * @return the name of the 'event'
   */
  public String getName() {
    return name;
  }

  /**
   * <p>
   * Set whether the 'event' was successful or not.
   * </p>
   *
   * @param b true if the 'event' was successful.
   */
  public void setWasSuccessful(boolean b) {
    wasSuccessful = b;
  }

  /**
   * <p>
   * Return true if the named 'event' was successful, otherwise false.
   * </p>
   *
   * @return true if the named 'event' was successful, otherwise false
   */
  public boolean getWasSuccessful() {
    return wasSuccessful;
  }

  /**
   * Set the unique id for this marker.
   *
   * @param id the unique id.
   */
  public void setUniqueId(String id) {
    uniqueId = id;
  }

  /**
   * Get the unique id.
   *
   * @return the uniqueid.
   */
  public String getUniqueId() {
    return uniqueId;
  }

  /** @see java.lang.Object#clone() */
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("[");
    result.append(this.getClass().getName());
    result.append("] name [");
    result.append(name);
    result.append("] qualifier [");
    result.append(qualifier);
    result.append("] was successful [");
    result.append(wasSuccessful);
    result.append("] unique id [");
    result.append(uniqueId);
    result.append("]");

    return result.toString();
  }

  /** @see java.lang.Object#equals(java.lang.Object) */
  @Override
  public boolean equals(Object o) {
    if (!this.getClass().equals(o.getClass())) {
      return false;
    }
    MleMarker m = (MleMarker) o;
    int count = 0;
    count += name.equals(m.getName()) ? 1 : 0;
    count += qualifier.equals(m.getQualifier()) ? 1 : 0;
    count += wasSuccessful == m.getWasSuccessful() ? 1 : 0;
    count += sequenceNumber == m.getSequenceNumber() ? 1 : 0;
    return count == 4;
  }

  @Override
  public int hashCode() {
    return name.hashCode() + qualifier.hashCode() + Boolean.valueOf(getWasSuccessful()).hashCode()
        + new Integer(getSequenceNumber()).hashCode();
  }

  /**
   * @see MessageEventGenerator#isTrackingEndpoint()
   */
  public boolean getIsTrackingEndpoint() {
    return isTrackingEndpoint;
  }

  /**
   * @see MessageEventGenerator#isTrackingEndpoint()
   */
  public void setIsTrackingEndpoint(boolean b) {
    isTrackingEndpoint = b;
  }

  /**
   * <p>
   * Returns the optional confirmation ID. If set this is used to set up a
   * confirmation in tracking.
   * </p>
   *
   * @return the optional confirmation ID
   */
  public String getConfirmationId() {
    return confirmationId;
  }

  /**
   * <p>
   * Sets the optional confirmation ID. If set this is used to set up a
   * confirmation in tracking.
   * </p>
   *
   * @param s the optional confirmation ID
   */
  public void setConfirmationId(String s) {
    confirmationId = s;
  }

  /**
   * <p>
   * Returns true if this is a confirmation otherwise false.
   * </p>
   *
   * @return true if this is a confirmation otherwise false.
   */
  public boolean getIsConfirmation() {
    return isConfirmation;
  }

  /**
   * <p>
   * Sets whether this is a confirmation (for the given confirmation id).
   * </p>
   *
   * @param b this is a confirmation (for the given confirmation id)
   */
  public void setIsConfirmation(boolean b) {
    isConfirmation = b;
  }

  /**
   * The qualifier for the event in question
   *
   * <p>
   * In most cases, the qualifier is the unique-id of the
   * {@link MessageEventGenerator} that created this marker
   * </p>
   *
   * @return the qualifier
   */
  public String getQualifier() {
    return qualifier;
  }

  public void setQualifier(String qualifier) {
    this.qualifier = qualifier;
  }
}
