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
  @Override
  public InputStream openInput() throws IOException, AdaptrisSecurityException {
    return new ByteArrayInputStream(keystoreBytes);
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#openOutput()
   */
  @Override
  public OutputStream openOutput()
      throws IOException, AdaptrisSecurityException {
    throw new IOException(this.getClass() + " is implicitly readonly");
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#isWriteable()
   */
  @Override
  public boolean isWriteable() {
    return false;
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreLocation#exists()
   */
  @Override
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
  @Override
  public Properties getAdditionalParams() {
    return additionalParams;
  }


  /**
   *
   * @see KeystoreLocation#getKeyStoreType()
   */
  @Override
  public String getKeyStoreType() {
    return keystoreType;
  }


  /**
   *
   * @see KeystoreLocation#getKeystorePassword()
   */
  @Override
  public char[] getKeystorePassword() {
    return keystorePassword;
  }


  /**
   *
   * @see KeystoreLocation#setAdditionalParams(java.util.Properties)
   */
  @Override
  public void setAdditionalParams(Properties p) {
    additionalParams = p;
  }

  /**
   *
   * @see KeystoreLocation#setKeystorePassword(char[])
   */
  @Override
  public void setKeystorePassword(char[] pw) {
    keystorePassword = pw;
  }


  /**
   *
   * @see KeystoreLocation#setKeystoreType(java.lang.String)
   */
  @Override
  public void setKeystoreType(String s) {
    keystoreType = s;
  }

  @SuppressWarnings({"lgtm [java/weak-cryptographic-algorithm]"})
  private static byte[] calculateHash(byte[] b) {
    byte[] result = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA1"); // java/weak-cryptographic-algorithm
      md.update(b);
      result = md.digest();
    }
    catch (Exception e) {
      result = new byte[0];
    }
    return result;
  }
}
