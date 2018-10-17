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

/**
 * Alias in a keystore.
 * <p>
 * This object is simply a wrapper that holds the information required to access
 * the appropriate certificate / private keys out of the keystore.
 * </p>
 * @author $Author: lchan $
 */
public final class Alias {

  private String alias;
  private char[] aliasPassword;

  /**
   * @see Object#Object()
   * 
   * 
   */
  public Alias() {
  }

  /**
   * Constructor
   * 
   * @param a the alias
   * @param pw the password
   */
  public Alias(String a, String pw) {
    this();
    setKeyStoreAlias(a, pw);
  }

  /**
   * 
   * @param a the alias
   * @param pw the password
   */
  public Alias(String a, char[] pw) {
    this();
    setKeyStoreAlias(a, pw);
  }

  /**
   * Constructor
   * 
   * @param a the alias
   */
  public Alias(String a) {
    this(a, "");
  }

  /**
   * Set the keystore alias entry.
   * 
   * @param a the alias
   * @param pw the password
   */
  public void setKeyStoreAlias(String a, String pw) {
    if (pw != null) {
      setKeyStoreAlias(a, pw.toCharArray());
    } else {
      alias = a;
    }
  }

  /**
   * Set the keystore alias entry.
   * 
   * @param a the alias
   * @param pw the password
   */
  public void setKeyStoreAlias(String a, char[] pw) {
    alias = a;
    aliasPassword = pw;
  }

  /**
   * Get the alias associated with this object.
   * 
   * @return the alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * Get the password associated with this object.
   * 
   * @return the alias password
   */
  public char[] getAliasPassword() {
    return aliasPassword;
  }
}
