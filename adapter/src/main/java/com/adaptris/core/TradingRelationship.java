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

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * <p>
 * Encapsulates a source, destination and type <i>trading relationship</i>.  A
 * wild card identifier may be used for any or all of the three components.
 * </p>
 */
public class TradingRelationship implements Cloneable {

  /**
   * <code>String</code> indicating 'any'.  Value is <code>*</code>.
   */
  public static final String WILD_CARD = "*"; // make configurable?

  private String source;
  private String destination;
  private String type;

  /**
   * <p>
   * Creates a new instance. Defaults to <code>WILD_CARD</code> for source,
   * destination and type.
   * </p>
   */
  public TradingRelationship() {
    // defaults...
    this(WILD_CARD, WILD_CARD, WILD_CARD);
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   * @param src the source identifier, may not be null or empty
   * @param dst the destination identifier, may not be null or empty
   * @param typ the message type identifier, may not be null or empty
   */
  public TradingRelationship(String src, String dst, String typ) {
    setSource(src);
    setDestination(dst);
    setType(typ);
  }

  /**
   * <p>
   * <code>ServiceId</code> instances are sematically equal if their
   * component source, destination and type are equal ignoring case.
   * <p>
   * @see java.lang.Object#equals(java.lang.Object) */
  @Override
  public boolean equals(Object obj) {
    boolean result = false;

    if (obj instanceof TradingRelationship) { // false if obj is null
      TradingRelationship m = (TradingRelationship) obj;

      if (m.getSource().equalsIgnoreCase(getSource())) {
        if (m.getDestination().equalsIgnoreCase(getDestination())) {
          if (m.getType().equalsIgnoreCase(getType())) {
            result = true;
          }
        }
      }
    }

    return result;
  }

  /**
   * <p>
   * The <code>hashCode</code> of an instance of this class is equal to
   * the <code>hashCode</code> of its component source, destination and type
   * converted to lower case.
   * </p>
   * @see java.lang.Object#hashCode() */
  @Override
  public int hashCode() {
    int result = 0;

    result += getSource().toLowerCase().hashCode();
    result += getDestination().toLowerCase().hashCode();
    result += getType().toLowerCase().hashCode();

    return result;
  }

  /** @see java.lang.Object#clone() */
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone(); // shallow, okay for Strings
  }

  /**
   * <p>
   * Returns true if any of <code>source</code>, <code>destination</code>
   * or <code>type</code> equal the wild card character, otherwise
   * false.
   * </p>
   * @return true if any of <code>source</code>, <code>destination</code>
   * or <code>type</code> equal the wild card character, otherwise
   * false
   */
  public boolean hasWildCards() {
    boolean result = false;

    if (getSource().equals(TradingRelationship.WILD_CARD)
      || getDestination().equals(TradingRelationship.WILD_CARD)
        || getType().equals(TradingRelationship.WILD_CARD)) {

      result = true;
    }
    return result;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(this.getClass().getSimpleName());
    result.append("{ source [");
    result.append(getSource());
    result.append("] destination [");
    result.append(getDestination());
    result.append("] type [");
    result.append(getType());
    result.append("]}");

    return result.toString();
  }

  // getters & setters...

  /**
   * <p>
   * Returns the <i>destination</i> identifier.
   * </p>
   * @return the <i>destination</i> identifier
   */
  public String getDestination() {
    return destination;
  }

  /**
   * <p>
   * Sets the <i>destination</i> identifier.  May not be null or empty.
   * </p>
   * @param s the <i>destination</i> identifier
   */
  public void setDestination(String s) {
    if (isBlank(s)) {
      throw new IllegalArgumentException("destination is null or empty param");
    }
    destination = s;
  }

  /**
   * <p>
   * Returns the <i>source</i> identifier.
   * </p>
   * @return the <i>source</i> identifier
   */
  public String getSource() {
    return source;
  }

  /**
   * <p>
   * Sets the <i>source</i> identifier. May not be null or empty.
   * </p>
   * @param s the <i>source</i> identifier
   */
  public void setSource(String s) {
    if (isBlank(s)) {
      throw new IllegalArgumentException("source is null or empty param");
    }
    source = s;
  }

  /**
   * <p>
   * Returns the <i>type</i> identifier.
   * </p>
   * @return the <i>type</i> identifier
   */
  public String getType() {
    return type;
  }

  /**
   * <p>
   * Sets the <i>type</i> identifier. May not be null or empty.
   * </p>
   * @param s the <i>type</i> identifier
   */
  public void setType(String s) {
    if (isBlank(s)) {
      throw new IllegalArgumentException("type is null or empty param");
    }
    type = s;
  }
}
