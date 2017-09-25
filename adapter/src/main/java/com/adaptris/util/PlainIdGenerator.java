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

import java.security.SecureRandom;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.util.text.Conversion;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Basic {@linkplain IdGenerator} implementation
 * <p>
 * The ID is generated from the a combination of {@linkplain SecureRandom#nextBytes(byte[])}, the hashcode of the object, and
 * current timestamp. No guarantees are made for the uniqueness of the ID generated.
 * </p>
 * 
 * @config plain-id-generator
 */
@XStreamAlias("plain-id-generator")
public class PlainIdGenerator implements IdGenerator {

  @InputFieldHint(style = "BLANKABLE")
  @InputFieldDefault(value = ".")
  private String separator;
  private static SecureRandom random = new SecureRandom();

  public PlainIdGenerator() {
    setSeparator(".");
  }

  public PlainIdGenerator(String separator) {
    this();
    setSeparator(separator);
  }

  @Override
  public String create(Object msg) {
    byte[] bytes = new byte[8];
    random.nextBytes(bytes);
    return Conversion.byteArrayToBase64String(bytes).replaceAll("=", "") + getSeparator()
        + (msg == null ? "0" : Integer.toHexString(msg.hashCode()))
        + getSeparator() + System.currentTimeMillis();
  }

  /**
   * @return the separator
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * @param s the separator to set
   */
  public void setSeparator(String s) {
    separator = s;
  }

}
