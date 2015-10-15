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

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * <p>
 * Implementation of <code>TradingRelationshipCreator</code> which creates a <code>TradingRelationship</code> with the configured
 * values.
 * </p>
 * 
 * @config configured-trading-relationship-creator
 */
@XStreamAlias("configured-trading-relationship-creator")
public final class ConfiguredTradingRelationshipCreator implements
    TradingRelationshipCreator {

  private String source;
  private String destination;
  private String type;

  /**
   * <p>
   * Creates a new instance. Default keys are empty <code>String</code>s.
   * </p>
   */
  public ConfiguredTradingRelationshipCreator() {
    this.source = "";
    this.destination = "";
    this.type = "";
  }

  public ConfiguredTradingRelationshipCreator(String src, String dest, String type) {
    this();
    setSource(src);
    setDestination(dest);
    setType(type);
  }

  /**
   * <p>
   * If any key is empty or if any key returns a value of null or empty a
   * <code>CoreException</code> is thrown.
   * </p>
   *
   * @see com.adaptris.core.TradingRelationshipCreator
   *      #create(com.adaptris.core.AdaptrisMessage)
   */
  public TradingRelationship create(AdaptrisMessage msg) throws CoreException {
    return new TradingRelationship(source, destination, type);
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(this.getClass().getName());
    result.append(" source [");
    result.append(this.getSource());
    result.append("] destination [");
    result.append(this.getDestination());
    result.append("] type [");
    result.append(this.getType());
    result.append("]");

    return result.toString();
  }

  // getters & setters...

  /**
   * <p>
   * Returns the metadata key used to obtain the destination.
   * </p>
   *
   * @return the metadata key used to obtain the destination
   */
  public String getDestination() {
    return destination;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the destination. May not be null or
   * empty.
   * </p>
   *
   * @param s the metadata key used to obtain the destination
   */
  public void setDestination(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("Null Destination not allowed");
    }
    this.destination = s;
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the source.
   * </p>
   *
   * @return the metadata key used to obtain the source
   */
  public String getSource() {
    return source;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the source. May not be null or empty.
   * </p>
   *
   * @param s the metadata key used to obtain the source
   */
  public void setSource(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("Null Source not allowed");
    }
    this.source = s;
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the type.
   * </p>
   *
   * @return the metadata key used to obtain the type
   */
  public String getType() {
    return type;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the type. May not be null or empty.
   * </p>
   *
   * @param s the metadata key used to obtain the type
   */
  public void setType(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("Null Type not allowed");
    }
    this.type = s;
  }
}
