package com.adaptris.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.net.util.Base64;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.EncryptException;
import com.adaptris.security.exc.VerifyException;

/**
 * @see Output
 * @author $Author: lchan $
 */
final class StdOutput implements Output {

  private byte[] message = null, encryptedData = null, sessionIV = null,
      sessionKey = null, signature = null, decryptedData = null;
  private int type;

  /**
   * Constructor
   * 
   * @param t the type of output
   */
  StdOutput(int t) {
    type = t;
  }

  /**
   * @see Output#getAsString()
   */
  public String getAsString() throws AdaptrisSecurityException {
    return new String(this.getBytes());
  }

  /**
   * 
   * @see com.adaptris.security.Output#getAsString(java.lang.String)
   */
  public String getAsString(String charset)
      throws AdaptrisSecurityException, UnsupportedEncodingException {
    return new String(this.getBytes(), charset);
  }

  /**
   * @see Output#getBytes()
   */
  public byte[] getBytes() throws AdaptrisSecurityException {
    if (this.isEncrypted() || this.isSigned()) {
      return (this.formatBase64());
    }
    return (this.getDecryptedData(true));
  }

  /**
   * @see Output#getType()
   */
  public int getType() {
    return type;
  }

  /**
   * @see Output#isEncrypted()
   */
  public boolean isEncrypted() {
    return ((this.getType() & Output.ENCRYPTED) > 0);
  }

  /**
   * @see Output#isPlain()
   */
  public boolean isPlain() {
    return ((this.getType() & Output.PLAIN) > 0);
  }

  /**
   * @see Output#isSigned()
   */
  public boolean isSigned() {
    return ((this.getType() & Output.SIGNED) > 0);
  }

  /**
   * @see #split(byte[])
   */
  void readEncryptedMessage(String fullMessage)
      throws AdaptrisSecurityException {
    this.split(fullMessage.getBytes());
  }

  /**
   * This splits our full data message into it's constituent parts.
   * <p>
   * This output defines the encrypted payload be a base64 payload containing,
   * in order
   * <ul>
   * <li>InitialisationVector.length</li>
   * <li>InitialisationVector</li>
   * <li>SessionKey.length</li>
   * <li>SessionKey</li>
   * <li>Data.length</li>
   * <li>Data</li>
   * <li>Signature.length</li>
   * <li>Signature</li>
   * </ul>
   * </p>
   * 
   * @param fullMessage the data to be treated as an encrypted message
   * @throws AdaptrisSecurityException if an error occurs
   */
  void split(byte[] fullMessage)
      throws AdaptrisSecurityException {
    try {
      message = fullMessage;
      this.split();
    }
    catch (Exception e) {
      throw new VerifyException("Cannot parse payload", e);
    }
  }

  /**
   * Set the encrypted data.
   * 
   * @param bytes the byte array containing the encrypted data
   */
  void setEncryptedData(byte[] bytes) {
    if ((bytes != null) && (bytes.length > 0)) {
      encryptedData = bytes;
    }
  }

  /**
   * Set the signed data.
   * 
   * @param bytes the byte array containing the signature
   */
  void setSignature(byte[] bytes) {
    if ((bytes != null) && (bytes.length > 0)) {
      signature = bytes;
    }
  }

  /**
   * Get the signature that is stored in this container
   * 
   * @return a byte array containing the signature
   */
  byte[] getSignature() {
    return signature;
  }

  /**
   * Set the session key for this encryption instance.
   * 
   * @param bytes the byte array containing the session key
   */
  void setSessionKey(byte[] bytes) {
    if ((bytes != null) && (bytes.length > 0)) {
      sessionKey = bytes;
    }
  }

  /**
   * Return the stored session key.
   * 
   * @return a byte array containing the session key
   */
  byte[] getSessionKey() {
    return sessionKey;
  }

  /**
   * Set the session initialisation vector for this encryption instance.
   * 
   * @param bytes the byte array containing the session vector
   */
  void setSessionVector(byte[] bytes) {
    if ((bytes != null) && (bytes.length > 0)) {
      sessionIV = bytes;
    }
  }

  /**
   * Get the session vector for this encryption.
   * 
   * @return a byte array containing the session vector
   */
  byte[] getSessionVector() {
    return sessionIV;
  }

  /**
   * Set the decrypted Data.
   * 
   * @param b the byte array containing the decrypted data,
   */
  void setDecryptedData(byte[] b) {
    decryptedData = b;
  }

  /**
   * Set the decrypted Data.
   * 
   * @param s the String containing the decrypted data
   */
  void setDecryptedData(String s) {
    this.setDecryptedData(s.getBytes());
  }

  /**
   * Get the encrypted data stored in this output object.
   * 
   * @param getCopy whether to return a copy of the bytes, rather than the
   *          actual bytes
   * @return a copy of the encrypted data as a byte array
   */
  byte[] getEncryptedData(boolean getCopy) {

    return (getCopy) ? copy(encryptedData) : encryptedData;
  }

  /**
   * Get the decrypted data stored in this output object.
   * 
   * @param getCopy whether to return a copy of the bytes, rather than the
   *          actual bytes
   * @return a copy of the decrypted data as a byte array
   */
  byte[] getDecryptedData(boolean getCopy) {

    return (getCopy) ? copy(decryptedData) : decryptedData;
  }

  /**
   * Set the output type.
   * 
   * @param t a type specified by the Output interface or combination thereof
   * @see Output#ENCRYPTED
   * @see Output#PLAIN
   * @see Output#SIGNED
   */
  void setType(int t) {
    type = t;
  }

  /**
   * Split this encrypted payload into it's constituent parts.
   * 
   * @see #readEncryptedMesage(byte[])
   * @throws IOException if we can't read the message
   * @throws Base64Exception if the message is not correctly encoded
   */
  private void split() throws IOException {

    ByteArrayInputStream byteStream = new ByteArrayInputStream(Base64.decodeBase64(message));
    DataInputStream in = new DataInputStream(byteStream);
    setSessionVector(read(in));
    setSessionKey(read(in));
    setEncryptedData(read(in));
    setSignature(read(in));
    in.close();
    byteStream.close();

  }

  /**
   * Return the encrypted message ready for immediate writing to file.
   * 
   * @return the bytes ready for writing.
   * @throws AdaptrisSecurityException if an error occurs
   */
  private byte[] formatBase64() throws EncryptException {

    DataOutputStream out = null;
    ByteArrayOutputStream byteStream = null;
    byte[] returnBytes = null;
    try {
      byteStream = new ByteArrayOutputStream();
      out = new DataOutputStream(byteStream);
      write(out, getSessionVector());
      write(out, getSessionKey());
      write(out, getEncryptedData(false) == null
          ? getDecryptedData(false)
          : getEncryptedData(false));
      write(out, getSignature());
      returnBytes = Base64.encodeBase64(byteStream.toByteArray());
    }
    catch (Exception e) {
      throw new EncryptException(e);
    }
    finally {
      try {
        if (out != null) {
          out.close();
        }
        if (byteStream != null) {
          byteStream.close();
        }
      }
      catch (Exception ignored) {
        ;
      }
    }
    return returnBytes;
  }

  private static void write(DataOutputStream out, byte[] bytes)
      throws IOException {
    if (bytes == null) {
      out.writeInt(0);
    }
    else {
      out.writeInt(bytes.length);
      out.write(bytes, 0, bytes.length);
    }
  }

  private static byte[] read(DataInputStream in) throws IOException {
    byte[] bytes = new byte[in.readInt()];
    if (bytes.length > 0) {
      in.read(bytes, 0, bytes.length);
    }
    else {
      bytes = null;
    }
    return bytes;
  }
  
  private static byte[] copy(byte[] source) {
    return ArrayUtils.clone(source);
  }
}