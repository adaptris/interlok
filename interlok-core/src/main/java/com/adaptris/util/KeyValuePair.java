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

package com.adaptris.util;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.util.Args;
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
public class KeyValuePair implements NameValuePair {

  private static final long serialVersionUID = 2017081501L;

  @NotBlank
  private String key = "";
  @InputFieldDefault(value = "")
  @InputFieldHint(style = "BLANKABLE")
  @NotNull
  private String value = "";

  public KeyValuePair() {
  }

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
  @Override
  public void setKey(String key) {
    this.key = Args.notNull(key, "key");
  }

  @Override
  public String getKey() {
    return key;
  }

  /**
   * <p>
   * Sets the 'value'.
   * </p>
   * @param value may not be null
   */
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
