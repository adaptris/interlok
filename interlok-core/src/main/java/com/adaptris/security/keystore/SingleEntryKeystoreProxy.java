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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.util.Constants;
import com.adaptris.security.util.SecurityUtil;

/**
 * Abstract class that allows the proxying of a single keystore element.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
abstract class SingleEntryKeystoreProxy implements KeystoreProxy {
  protected static final String DEFAULT_KS_IMPL = "JKS";

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());
  private KeystoreLocation keystoreLocation = null;
  private String aliasName;

  public SingleEntryKeystoreProxy() {
    SecurityUtil.addProvider();
  }

  /**
   * 
   * @see KeystoreProxy#setKeystoreLocation(KeystoreLocation)
   */
  public void setKeystoreLocation(KeystoreLocation k)
      throws AdaptrisSecurityException {
    keystoreLocation = k;
    String s = k.getAdditionalParams().getProperty(Constants.KEYSTORE_ALIAS);
    if (s == null) {
      throw new KeystoreException(
          "No alias name associated with this certificate");
    }
    aliasName = s.toLowerCase();
  }

  /**
   * 
   * @see com.adaptris.security.keystore.KeystoreProxy#commit()
   */
  public void commit() throws AdaptrisSecurityException, IOException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#setCertificate(String, Certificate)
   */
  public void setCertificate(String alias, Certificate cert)
      throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#setCertificate(java.lang.String, java.io.InputStream)
   */
  public void setCertificate(String alias, InputStream in)
      throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#setCertificate(java.lang.String, java.io.File)
   */
  public void setCertificate(String alias, File file)
      throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#setCertificate(java.lang.String, java.lang.String)
   */
  public void setCertificate(String alias, String filename)
      throws AdaptrisSecurityException {
    this.setCertificate(alias, new File(filename));
  }

  /**
   * 
   * @see KeystoreProxy#setPrivateKey(java.lang.String,
   *      java.security.PrivateKey, char[], java.security.cert.Certificate[])
   */
  public void setPrivateKey(String alias, PrivateKey privKey,
                            char[] keyPassword, Certificate[] certChain)
      throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#importPrivateKey(java.lang.String, char[],
   *      java.io.InputStream, char[])
   */
  public void importPrivateKey(String alias, char[] keyPassword,
                               InputStream in, char[] filePassword)
      throws AdaptrisSecurityException {

    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#importPrivateKey(java.lang.String, char[], java.io.File,
   *      char[])
   */
  public void importPrivateKey(String alias, char[] keyPassword, File file,
                               char[] filePassword)
      throws AdaptrisSecurityException {

    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#importPrivateKey(java.lang.String, char[],
   *      java.lang.String, char[])
   */
  public void importPrivateKey(String alias, char[] keyPassword, String file,
                               char[] filePassword)
      throws AdaptrisSecurityException {
    this.importPrivateKey(alias, keyPassword, new File(file), filePassword);
  }

  /**
   * 
   * @see KeystoreProxy#importCertificateChain(java.lang.String, char[],
   *      java.lang.String)
   */
  public void importCertificateChain(String alias, char[] keyPassword,
                                     String file)
      throws AdaptrisSecurityException {
    this.importCertificateChain(alias, keyPassword, new File(file));
  }

  /**
   * 
   * @see KeystoreProxy#importCertificateChain(java.lang.String, char[],
   *      java.io.File)
   */
  public void importCertificateChain(String alias, char[] keyPassword, File f)
      throws AdaptrisSecurityException {

    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#importCertificateChain(java.lang.String, char[],
   *      java.io.InputStream)
   */
  public void importCertificateChain(String alias, char[] keyPassword,
                                     InputStream in)
      throws AdaptrisSecurityException {

    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   * 
   * @see KeystoreProxy#containsAlias(java.lang.String)
   */
  public boolean containsAlias(String alias) throws AdaptrisSecurityException {
    return aliasName.equals(alias);
  }

  /**
   * 
   * @see com.adaptris.security.keystore.KeystoreProxy#getKeystore()
   */
  public KeyStore getKeystore() {
    return buildTemporaryKeystore();
  }

  /**
   * @return the aliasName
   */
  protected String getAliasName() {
    return aliasName;
  }

  /**
   * @return the keystoreLocation
   */
  protected KeystoreLocation getKeystoreLocation() {
    return keystoreLocation;
  }

  protected abstract KeyStore buildTemporaryKeystore();

  public String toString() {
    return "[" + keystoreLocation.toString() + "]";
  }
}
