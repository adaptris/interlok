package com.adaptris.util;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A key value pair.
 * <p>
 * Primarily used in configuration to avoid the use of maps.
 * </p>
 * 
 * @config key-value-pair
 */
@XStreamAlias("key-value-pair")
public class KeyValuePair implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 2013111201L;

  private String key = "";
  private String value = "";

  /**
   * <p>
   * Creates an empty new instance.  Defaults to empty <code>String</code>s.
   * </p>
   */
  public KeyValuePair() {
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   * @param key may not be null
   * @param value may not be null
   */
  public KeyValuePair(String key, String value) {
    setKey(key);
    setValue(value);
  }

  /**
   * <p>
   * Sets the 'key'.
   * </p>
   * @param key may not be null.
   */
  public void setKey(String key) {
    if (key == null) {
      throw new IllegalArgumentException("illegal key [" + key + "]");
    }
    else {
      this.key = key;
    }
  }

  /**
   * <p>
   * Returns the key.
   * </p>
   * @return the <code>String</code> key, never null
   */
  public String getKey() {
    return key;
  }

  /**
   * <p>
   * Sets the 'value'.
   * </p>
   * @param value may not be null
   */
  public void setValue(String value) {
    if (value == null) {
      throw new IllegalArgumentException("illegal value [" + value + "]");
    }
    else {
      this.value = value;
    }
  }

  /**
   * <p>
   * Returns the value.
   * </p>
   * @return the <code>String</code> value, never null
   */
  public String getValue() {
    return value;
  }

  /**
   * <p>
   * Returns a <code>String</code> representation of this object.
   * </p>
   * @return a <code>String</code> representation of this object
   */
  @Override
  public String toString() {
    return "key [" + key + "] value [" + value + "]";
  }

  /**
   * <p>
   * <code>KeyValuePair</code>s are semantically equally if their
   * keys <b>only</b> are equal.
   * </p>
   * @param obj the <code>Object</code> to test
   * @return true if <code>obj</code> is semantically equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof KeyValuePair) { // false if obj is null
      if (((KeyValuePair) obj).getKey().equals(getKey())) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
