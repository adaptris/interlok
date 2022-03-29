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

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.util.SecurityUtil;

/**
 * @author lchan
 * @author $Author: lchan $
 */
abstract class KeystoreLocationImp implements KeystoreLocation {

  protected static Logger logR = LoggerFactory.getLogger(KeystoreLocation.class);
  private char[] keystorePassword;

  private String keystoreType;
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
   * Get the type of keystore.
   * <p>
   * Natively jdk1.4 supports the <b>JKS </b> and <b>JCEKS </b> types, <b>JCEKS
   * </b> being more secure.
   * </p>
   * <p>
   * In addition to these two types, we also support the <b>BKS </b> keystore
   * type which is part of the BC JCE implementation
   * </p>
   *
   * @return the keystore type.
   */
  public String getKeystoreType() {
    return keystoreType;
  }

  /**
   * Set the keystore type.
   *
   * @see #getKeystoreType()
   * @param s
   *          the keystore type
   */
  public void setKeystoreType(String s) {
    keystoreType = s;
  }


  /**
   * Set the keystore password.
   *
   * @param s
   *          the keystore password.
   */
  public void setKeystorePassword(String s) {
    if (s != null) {
      keystorePassword = s.toCharArray();
    }
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
   * Return the keystore password.
   *
   * @return the keystore password
   */
  public char[] getKeystorePassword() {
    return keystorePassword;
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

  /**
   * @return the additionalParams
   */
  public Properties getAdditionalParams() {
    return additionalParams;
  }

  /**
   * @param p the additionalParams to set
   */
  public void setAdditionalParams(Properties p) {
    this.additionalParams = p;
  }
}
