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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.util.Args;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.NameValuePair;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A key-value pair of <code>String</code> metadata. Instances of this class are used by
 * implementations of <code>AdaptrisMessage</code> to store metadata. Semantic equality of
 * <code>MetadataElement</code> s is based on the value of the 'key' only.
 * </p>
 * 
 * @config metadata-element
 */
@XStreamAlias("metadata-element")
public class MetadataElement implements NameValuePair, Cloneable {

  private static transient final GuidGenerator UUID = new GuidGenerator();
  /**
   *
   */
  private static final long serialVersionUID = 2013111201L;

  @NotBlank
  private String key;
  @InputFieldDefault(value = "")
  @InputFieldHint(style = "BLANKABLE")
  @NotBlank
  private String value = "";


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


  public void setKey(String key) {
    this.key = Args.notBlank(key, "key");
  }

  public String getKey() {
    return key;
  }

  public void setValue(String value) {
      this.value = Args.notNull(value, "value");
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("key", getKey())
        .append("value", getValue()).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NameValuePair) { // false if obj is null
      if (((NameValuePair) obj).getKey().equals(getKey())) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
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
