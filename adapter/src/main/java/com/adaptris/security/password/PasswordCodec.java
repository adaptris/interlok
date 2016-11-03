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

package com.adaptris.security.password;

import com.adaptris.security.exc.PasswordException;

/**
 * Primary interface for handling password encoding and decoding.
 *
 * @author lchan
 * 
 */
public interface PasswordCodec {

  /**
   * Invokes {@link #encode(String, String)} with null as the charset parameter
   */
  String encode(String plainText) throws PasswordException;

  /**
   * Obfuscate the plain text.
   *
   * @param plainText the text to obfuscate
   * @param charset the character set that should be used to create the
   *          resulting plain text password, if null uses UTF-8
   * @return the obfuscated text.
   */
  String encode(String plainText, String charset) throws PasswordException;

  /**
   * Show the plain text.
   *
   * @param encryptedPassword the encrypted password
   * @param charset the character set that should be used to create the
   *          resulting plain text password, if null uses UTF-8
   * @return the plain text password
   */
  String decode(String encryptedPassword, String charset) throws PasswordException;


  /**
   * Invokes {@link #decode(String, String)} with null as the charset parameter
   *
   * @see #decode(String, String)
   */
  String decode(String encryptedPassword) throws PasswordException;

  /**
   * Can this implementation handle an encoded password of this type.
   * 
   * @param type the type
   * @return true or false.
   */
  boolean canHandle(String type);

}
