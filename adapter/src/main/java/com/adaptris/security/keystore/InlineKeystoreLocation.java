/*
 * $Author: lchan $
 * $RCSfile: InlineKeystoreLocation.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/10/20 13:56:16 $
 */
package com.adaptris.security.keystore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Properties;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.util.text.Conversion;

/**
 * Convenience wrapper for KeystoreLocation that wraps a byte array.
 *
 * @author lchan
 * @author $Author: lchan $
 */
final class InlineKeystoreLocation implements KeystoreLocation {

  private byte[] keystoreBytes;
  private char[] keystorePassword;
  private String keystoreType;
  private Properties additionalParams;
  private byte[] keystoreHash;

  private InlineKeystoreLocation() {
    super();
    setAdditionalParams(new Properties());

  }

  public InlineKeystoreLocation(byte[] bytes) {
    this();
    keystoreBytes = bytes;
    keystoreHash = calculateHash(bytes);
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "[" + getKeyStoreType() + "][" + this.getClass() + "]";
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#openInput()
   */
  public InputStream openInput() throws IOException, AdaptrisSecurityException {
    return new ByteArrayInputStream(keystoreBytes);
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#openOutput()
   */
  public OutputStream openOutput()
      throws IOException, AdaptrisSecurityException {
    throw new IOException(this.getClass() + " is implicitly readonly");
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#isWriteable()
   */
  public boolean isWriteable() {
    return false;
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreLocation#exists()
   */
  public boolean exists() {
    return true;
  }

  /**
   * @see Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    boolean rc = false;
    if (o instanceof InlineKeystoreLocation) {
      InlineKeystoreLocation rhs = (InlineKeystoreLocation) o;
      rc = MessageDigest.isEqual(keystoreHash, rhs.keystoreHash)
          && getKeyStoreType().equalsIgnoreCase(rhs.getKeyStoreType());
    }
    return rc;
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Conversion.byteArrayToBase64String(keystoreHash).hashCode() + getKeyStoreType().hashCode();
  }


  /**
   *
   * @see KeystoreLocation#getAdditionalParams()
   */
  public Properties getAdditionalParams() {
    return additionalParams;
  }


  /**
   *
   * @see KeystoreLocation#getKeyStoreType()
   */
  public String getKeyStoreType() {
    return keystoreType;
  }


  /**
   *
   * @see KeystoreLocation#getKeystorePassword()
   */
  public char[] getKeystorePassword() {
    return keystorePassword;
  }


  /**
   *
   * @see KeystoreLocation#setAdditionalParams(java.util.Properties)
   */
  public void setAdditionalParams(Properties p) {
    this.additionalParams = p;
  }

  /**
   *
   * @see KeystoreLocation#setKeystorePassword(char[])
   */
  public void setKeystorePassword(char[] pw) {
    this.keystorePassword = pw;
  }


  /**
   *
   * @see KeystoreLocation#setKeystoreType(java.lang.String)
   */
  public void setKeystoreType(String s) {
    this.keystoreType = s;
  }

  private static byte[] calculateHash(byte[] b) {
    byte[] result = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      md.update(b);
      result = md.digest();
    }
    catch (Exception e) {
      result = new byte[0];
    }
    return result;
  }
}