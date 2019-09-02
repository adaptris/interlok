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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
  private static final long serialVersionUID = 2017081501L;

  @NotBlank
  private String key;
  @InputFieldDefault(value = "")
  @InputFieldHint(style = "BLANKABLE")
  @NotNull
  private String value = "";


  /**
   * Default Constructor.
   * <p>
   * By default, each metadata element is given a unique key using {@link GuidGenerator#getUUID()}.
   * </p>
   */
  public MetadataElement() {
    setKey(UUID.getUUID());
  }

  public MetadataElement(KeyValuePair kp) {
    this(kp.getKey(), kp.getValue());
  }


  public MetadataElement(String key, String value) {
    this();
    setKey(key);
    setValue(value);
  }


  @Override
  public void setKey(String key) {
    this.key = Args.notBlank(key, "key");
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public void setValue(String value) {
      this.value = Args.notNull(value, "value");
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "key [" + key + "] value [" + value + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MetadataElement) { // false if obj is null
      if (((MetadataElement) obj).getKey().equals(getKey())) {
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
