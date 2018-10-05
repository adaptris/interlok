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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.util.text.Conversion;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Pseudo Random {@linkplain IdGenerator} implementation
 * <p>
 * The ID is generated from the a combination of {@linkplain Random#nextBytes(byte[])} (8 bytes, and then base64 encoded),
 * along with the configured prefix to generate an id. No guarantees are made for the uniqueness of the id that is generated. If you
 * wish for a unique-id then you should consider using {@link GuidGenerator} instead.
 * </p>
 * <p>
 * Effectively the ID that is generated follows the form
 * 
 * <pre>
 * {@code 
    ID              = prefix unique-sequence
    prefix          = *VCHAR
    unique sequence = 12(ALPHA/DIGIT/"+"/"/"/"=")
    }
   </pre>
 * 
 * </p>
 * <p>
 * If you wish to restrict the length of the resulting ID, then bear in mind that with padding 8 bytes base64 encoded is 12
 * characters long so consider how long your prefix is.
 * </p>
 * 
 * @config pseudo-random-id-generator
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("pseudo-random-id-generator")
public class PseudoRandomIdGenerator implements IdGenerator {

  @InputFieldDefault(value = "")
  private String prefix;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean useSecureRandom;
  private final static SecureRandom SECURE_RAND = new SecureRandom();

  public PseudoRandomIdGenerator() {
    setPrefix("");
  }

  public PseudoRandomIdGenerator(String prefix) {
    this();
    setPrefix(prefix);
  }

  public PseudoRandomIdGenerator(String prefix, boolean useSecureRandom) {
    this();
    setPrefix(prefix);
    setUseSecureRandom(useSecureRandom);
  }

  /**
   * A generated ID.
   *
   *
   *
   */
  public String create(Object msg) {
    byte[] bytes = new byte[8];
    random().nextBytes(bytes);
    return getPrefix() + Conversion.byteArrayToBase64String(bytes);
  }

  /**
   * @return the separator
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * @param s the separator to set
   */
  public void setPrefix(String s) {
    if (isEmpty(s)) {
      prefix = "";
    }
    else {
      prefix = s;
    }
  }

  /**
   * @return the useSecureRandom
   */
  public Boolean getUseSecureRandom() {
    return useSecureRandom;
  }

  /**
   * Whether or not to use {@link SecureRandom} when generating a random sequence of bytes.
   * 
   * @param u the useSecureRandom to set, if not set, defaults to true.
   */
  public void setUseSecureRandom(Boolean u) {
    this.useSecureRandom = u;
  }

  boolean useSecureRandom() {
    return getUseSecureRandom() != null ? getUseSecureRandom().booleanValue() : true;
  }

  Random random() {
    return useSecureRandom() ? SECURE_RAND : ThreadLocalRandom.current();
  }
}
