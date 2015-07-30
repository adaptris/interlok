/*
 * $RCSfile: MetadataTradingRelationshipCreator.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/11/29 13:25:37 $
 * $Author: lchan $
 */
package com.adaptris.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>TradingRelationshipCreator</code> which populates the <code>TradingRelationship</code> with values
 * returned from configurable metadata keys.
 * </p>
 * 
 * @config metadata-trading-relationship-creator
 */
@XStreamAlias("metadata-trading-relationship-creator")
public class MetadataTradingRelationshipCreator
  implements TradingRelationshipCreator {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private String sourceKey;
  private String destinationKey;
  private String typeKey;

  /**
   * <p>
   * Creates a new instance.  Default keys are empty <code>String</code>s.
   * </p>
   */
  public MetadataTradingRelationshipCreator() {
    this.sourceKey = "";
    this.destinationKey = "";
    this.typeKey = "";
  }

  public MetadataTradingRelationshipCreator(String srcKey, String destKey, String typKey) {
    this();
    setSourceKey(srcKey);
    setDestinationKey(destKey);
    setTypeKey(typKey);
  }

  /**
   * <p>
   * If any key is empty or if any key returns a value of null or empty a
   * <code>CoreException</code> is thrown.
   * </p>
   * @see com.adaptris.core.TradingRelationshipCreator
   *   #create(com.adaptris.core.AdaptrisMessage) */
  public TradingRelationship create(AdaptrisMessage msg) throws CoreException {
    TradingRelationship result = null;

    String source = this.obtainValue(sourceKey, msg);
    String destination = this.obtainValue(destinationKey, msg);
    String type = this.obtainValue(typeKey, msg);

    result = new TradingRelationship(source, destination, type);

    log.trace("created " + result);

    return result;
  }

  private String obtainValue(String key, AdaptrisMessage msg)
    throws CoreException {

    if ("".equals(key)) {
      throw new CoreException("empty metadata key");
    }

    String result = msg.getMetadataValue(key);

    if (result == null || "".equals(result)) {
      throw new CoreException("key [" + key + "] returned null or empty");
    }

    return result;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(this.getClass().getName());
    result.append(" source key [");
    result.append(this.getSourceKey());
    result.append("] destination key [");
    result.append(this.getDestinationKey());
    result.append("] type key [");
    result.append(this.getTypeKey());
    result.append("]");

    return result.toString();
  }

  // getters & setters...

  /**
   * <p>
   * Returns the metadata key used to obtain the destination.
   * </p>
   * @return the metadata key used to obtain the destination
   */
  public String getDestinationKey() {
    return destinationKey;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the destination. May not be null or
   * empty.
   * </p>
   * @param s the metadata key used to obtain the destination
   */
  public void setDestinationKey(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    this.destinationKey = s;
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the source.
   * </p>
   * @return the metadata key used to obtain the source
   */
  public String getSourceKey() {
    return sourceKey;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the source. May not be null or empty.
   * </p>
   * @param s the metadata key used to obtain the source
   */
  public void setSourceKey(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    this.sourceKey = s;
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the type.
   * </p>
   * @return the metadata key used to obtain the type
   */
  public String getTypeKey() {
    return typeKey;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the type.  May not be null or empty.
   * </p>
   * @param s the metadata key used to obtain the type
   */
  public void setTypeKey(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    this.typeKey = s;
  }
}
