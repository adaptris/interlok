/*
 * $Author: lchan $
 * $RCSfile: Output.java,v $
 * $Revision: 1.8 $
 * $Date: 2005/08/05 10:08:23 $
 */
package com.adaptris.security;

import java.io.UnsupportedEncodingException;

import com.adaptris.security.exc.AdaptrisSecurityException;

/**
 * Core interface for handling encrypted/signed payloads.
 * <p>
 * An object implementing this interface is always returned by a
 * <code>SecurityService</code> instance when any of the encrypt, sign or
 * verify methods.
 * </p>
 * <p>
 * Ultimately the object implementing this interface will be reponsible for
 * reading from and writing to an encrypted payload. This payload could be in
 * any particular format e.g. something like S/MIME or EDI-INT with boundaries
 * and such.
 * </p>
 * <p>
 * The type of output that is represented can be determined using the
 * <code>getType()</code> method.
 * <p>
 * <p>
 * Three types of message are defined
 * <ul>
 * <li>ENCRYPTED</li>
 * <li>PLAIN</li>
 * <li>SIGNED</li>
 * </ul>
 * </p>
 * <p>
 * For payloads that are both encrypted and signed, the operations
 * <ul>
 * <li><code>if (Output.getType() & Output.ENCRYPTED) > 0)</code></li>
 * <li><code>if (Output.getType() & Output.SIGNED) > 0)</code></li>
 * </ul>
 * should be true
 * </p>
 * <p>
 * Alternatively, the <code>isEncrypted(), isSigned(), isPlain()</code>
 * methods can be used.
 * 
 * @author $Author: lchan $
 */
public interface Output {

  /** encrypted output */
  int ENCRYPTED = 1;
  /** decrypted output */
  int PLAIN = 2;
  /** signed output */
  int SIGNED = 4;

  /**
   * Return the internal message, either encrypted or decrypted.
   * <p>
   * This will return a String (using the platform default encoding) in the
   * appropriate format, ready for writing to an OutputStream or further
   * processing
   * </p>
   * 
   * @return a string containing required output
   * @throws AdaptrisSecurityException for any error
   */
  String getAsString() throws AdaptrisSecurityException;

  /**
   * Return the internal message, either encrypted or decrypted.
   * <p>
   * This will return a String (using the specified encoding) in the appropriate
   * format, ready for writing to an OutputStream or further processing
   * </p>
   * 
   * @param enc the encoding
   * @return a string containing required output
   * @throws AdaptrisSecurityException wrapping any other underlying exception
   * @throws UnsupportedEncodingException if the encoding type is not supported.
   */
  String getAsString(String enc)
      throws AdaptrisSecurityException, UnsupportedEncodingException;

  /**
   * Return the internal message, either encrypted or decrypted.
   * <p>
   * This returns the raw bytes in the appropriate format, ready for writing to
   * an OutputStream or further processing
   * 
   * @return a byte array containing the required output
   * @throws AdaptrisSecurityException for any error
   */
  byte[] getBytes() throws AdaptrisSecurityException;

  /**
   * Return the type of message this is, encrypted, plain, signed.
   * 
   * @return an integer representing the type.
   */
  int getType();

  /**
   * isEncrypted.
   * 
   * @return true if the output is encrypted
   */
  boolean isEncrypted();

  /**
   * isPlain.
   * 
   * @return true if the output is plain
   */
  boolean isPlain();

  /**
   * isSigned.
   * 
   * @return true if the output is signed
   */
  boolean isSigned();
}