package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A key-value pair of <code>String</code> metadata. Instances of this class are used by implementations of
 * <code>AdaptrisMessage</code> to store metadata. Semantic equality of <code>MetadataElement</code> s is based on the value of the
 * 'key' only.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>metadata-element</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@XStreamAlias("metadata-element")
@GenerateBeanInfo
public class MetadataElement extends KeyValuePair implements Cloneable {

  private static transient final GuidGenerator UUID = new GuidGenerator();
  /**
   *
   */
  private static final long serialVersionUID = 2013111201L;

  /**
   * Default Constructor.
   * <p>
   * By default, each metadata element is given a unique key using {@link GuidGenerator#getUUID()}, as {@link KeyValuePair} only
   * enforces null checking, but metadata elements must have a non-empty key.
   * </p>
   */
  public MetadataElement() {
    setKey(UUID.getUUID());
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param kp the keyvalue pair this metadata element should wrap
   */
  public MetadataElement(KeyValuePair kp) {
    this(kp.getKey(), kp.getValue());
  }


  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param key may not be null or empty
   * @param value may not be null or empty
   */
  public MetadataElement(String key, String value) {
    this();
    setKey(key);
    setValue(value);
  }

  /**
   * <p>
   * Sets the 'key'.
   * </p>
   * 
   * @param key may not be null or the empty string.
   */
  @Override
  public void setKey(String key) {
    if (isEmpty(key)) {
      throw new IllegalArgumentException("illegal key [" + key + "]");
    }
    else {
      super.setKey(key);
    }
  }

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    MetadataElement obj = (MetadataElement) super.clone();
    obj.setKey(getKey());
    obj.setValue(getValue());
    return obj;
  }
}
