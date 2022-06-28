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

package com.adaptris.security.keystore;

import java.util.Optional;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.util.SecurityUtil;

/**
 * @author lchan
 * @author $Author: lchan $
 */
abstract class KeystoreLocationImp implements KeystoreLocation {

  protected static Logger logR = LoggerFactory.getLogger(KeystoreLocation.class);
  @Getter
  private char[] keystorePassword;
  @Getter
  @Setter
  private String keystoreType;
  @Getter
  @Setter
  private Properties additionalParams;

  /**
   * @see Object#Object()
   *
   *
   */
  public KeystoreLocationImp() {
    SecurityUtil.addProvider();
    setAdditionalParams(new Properties());
  }

  /**
   * Set the keystore password.
   *
   * @param s
   *          the keystore password.
   */
  public void setKeystorePassword(String s) {
    Optional.ofNullable(s).ifPresent((pw) -> setKeystorePassword(pw.toCharArray()));
  }

  /**
   * Set the keystore password.
   *
   * @param c the keystore password.
   */
  public void setKeystorePassword(char[] c) {
    keystorePassword = c;
  }

  /**
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public abstract boolean equals(Object o);

  /**
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public abstract int hashCode();
}
