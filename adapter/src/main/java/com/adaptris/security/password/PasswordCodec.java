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

}
