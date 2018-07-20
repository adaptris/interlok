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

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.adaptris.security.certificate.CertificateHandler;
import com.adaptris.security.certificate.CertificateHandlerFactory;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.KeystoreException;

/**
 * Keystore Proxy implementation that only handles a single certificate.
 * 
 * @author $Author: lchan $
 */
class X509KeystoreProxy extends SingleEntryKeystoreProxy {

  private CertificateHandler certHandler;

  /**
   * Default Constructor.
   */
  public X509KeystoreProxy() {
  }

  /**
   * Construct the object using the KeyStoreInfo object.
   * 
   * @param k the KeyStoreInfo object
   * @see KeystoreLocation
   * @throws AdaptrisSecurityException if an error is encountered
   */
  public X509KeystoreProxy(KeystoreLocation k) throws AdaptrisSecurityException {
    this();
    setKeystoreLocation(k);
  }



  /**
   * Load the keystore.
   * <p>
   * Load the keystore ready for operations upon it
   * </p>
   * 
   * @throws AdaptrisSecurityException if there was an error reading the
   *           contents of the keystore
   * @throws IOException if the keystore is not found
   */
  public void load() throws AdaptrisSecurityException, IOException {

    InputStream in = getKeystoreLocation().openInput();
    try {
      certHandler = CertificateHandlerFactory.getInstance().generateHandler(in);
    }
    catch (CertificateException e) {
      throw new KeystoreException(e);
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
      }
      catch (Exception ignored) {
        ;
      }
    }
  }

  /**
   * Method to extract a Partner's Private Key from their Keystore entry and
   * return a PrivateKey object to the caller.
   * 
   * @param alias the alias in the keystore
   * @param keyPassword the associated password
   * @return the requested private key, or null if the alias does not exist/not
   *         a key entry
   * @throws AdaptrisSecurityException for any error
   */
  public PrivateKey getPrivateKey(String alias, char[] keyPassword)
      throws AdaptrisSecurityException {

    throw new KeystoreException(this.getClass()
        + " does not handle Private Keys");
  }

  /**
   * Return the certificate specified by the given alias.
   * 
   * @param alias the alias of the Certificate
   * @return Certificate the requested certificate, or null if the alias does
   *         not exist/not a certificate
   * @throws AdaptrisSecurityException for any error
   */
  public Certificate getCertificate(String alias)
      throws AdaptrisSecurityException {

    Certificate cert = null;
    if (getAliasName().equalsIgnoreCase(alias)) {
      cert = certHandler.getCertificate();
    }
    else {
      throw new KeystoreException(alias + " not found");
    }
    return cert;
  }

  /**
   * Return the certificate specified by the given alias.
   * 
   * @param alias the alias of the Certificate
   * @return requested certificate chain, or null if the alias does not
   *         exist/not a certificate
   * @throws AdaptrisSecurityException for any error
   */
  public Certificate[] getCertificateChain(String alias)
      throws AdaptrisSecurityException {
    Certificate cert = null;
    if (getAliasName().equalsIgnoreCase(alias)) {
      cert = certHandler.getCertificate();
    }
    else {
      throw new KeystoreException(alias + " not found");
    }
    Certificate[] certChain = new Certificate[1];
    certChain[0] = cert;
    return certChain;
  }
  
  protected KeyStore buildTemporaryKeystore() {
    KeyStore ks = null;
    try {
      ks = KeyStore.getInstance(DEFAULT_KS_IMPL);
      ks.load(null, null);
      ks.setCertificateEntry(getAliasName(), certHandler.getCertificate());
    }
    catch (Exception e) {
      logR.warn("Failed to create a temporary keystore [" + e.getMessage()
          + "], returning null");
      ks = null;
    }
    return ks;
  }
  
}
