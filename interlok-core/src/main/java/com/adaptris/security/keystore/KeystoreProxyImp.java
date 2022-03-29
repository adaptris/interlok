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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.CertException;
import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.util.Constants;
import com.adaptris.security.util.SecurityUtil;

/**
 * Default implementation of KeystoreProxy that handles any keystore provider
 * implementation of keystore.
 *
 * @author $Author: lchan $
 */
class KeystoreProxyImp implements KeystoreProxy {

  private KeyStore keyStore = null;
  private KeystoreLocation keystoreLocation = null;
  private transient Logger logR = LoggerFactory.getLogger(KeystoreProxy.class);

  /**
   * Default Constructor.
   */
  public KeystoreProxyImp() {
    SecurityUtil.addProvider();
  }

  /**
   * Construct the object using the KeyStoreInfo object.
   *
   * @param k the KeyStoreInfo object
   * @see KeystoreLocation
   * @throws AdaptrisSecurityException if an error is encountered
   */
  public KeystoreProxyImp(KeystoreLocation k) throws AdaptrisSecurityException {
    this();
    setKeystoreLocation(k);
  }

  /**
   * Set the internal KeystoreProxy object to be used.
   * <p>
   * Based on this information, a new KeyStore object is created, and
   * initialised.
   *
   * @param k the KeystoreProxy object
   * @see KeystoreLocation
   * @throws AdaptrisSecurityException wrapping the underlying exception
   */
  public void setKeystoreLocation(KeystoreLocation k)
      throws AdaptrisSecurityException {
    try {
      keystoreLocation = k;
      if (keyStore == null) {
        keyStore = KeyStore.getInstance(keystoreLocation.getKeystoreType());

        // We must load the keystore before use, so load with null parameters
        // if we explicitly call keystore.load(), then we will re-init with
        // the input stream
        keyStore.load(null, null);
      }
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
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

    try (InputStream in = keystoreLocation.openInput()) {
      keyStore.load(in, keystoreLocation.getKeystorePassword());
    }
    catch (GeneralSecurityException e) {
      throw KeystoreProxy.wrapException(e);
    }
  }

  /**
   * Save the contents of the keystore to file.
   *
   * @throws AdaptrisSecurityException if an error was encountered writing to
   *           the keystore
   * @throws IOException if the file could not be written to
   * @see KeystoreLocation#isWriteable()
   */
  public void commit() throws AdaptrisSecurityException, IOException {

    if (!keystoreLocation.isWriteable()) {
      throw new KeystoreException(keystoreLocation + " is not writeable");
    }
    try (OutputStream out = keystoreLocation.openOutput()) {
      keyStore.store(out, keystoreLocation.getKeystorePassword());
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
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

    PrivateKey p = null;
    try {
      if (keyPassword == null) {
        logR.trace("No private key password passed as parameter, using keystore password as key password");
      }
      char[] pw = keyPassword == null ? keystoreLocation.getKeystorePassword() : keyPassword;
      p = (PrivateKey) keyStore.getKey(alias, pw);
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
    return p;
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

    try {
      return keyStore.getCertificate(alias);
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
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

    try {
      return keyStore.getCertificateChain(alias);
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
  }

  /**
   * Assign the given certificate to the given alias.
   *
   * @param alias the alias of the Certificate
   * @param cert the Certificate
   * @throws AdaptrisSecurityException for any error
   */
  public void setCertificate(String alias, Certificate cert)
      throws AdaptrisSecurityException {
    try {
      keyStore.setCertificateEntry(alias, cert);
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
  }

  /**
   * Assign the given InputStream (contaning a certificate) to the given alias.
   * <p>
   * The InputStream is expected to contain a PEM or DER encoded certificate
   * </p>
   *
   * @param alias the alias of the Certificate
   * @param in the InputStream containing the certificate
   * @throws AdaptrisSecurityException for any error
   * @see #setCertificate(String, Certificate)
   */
  public void setCertificate(String alias, InputStream in)
      throws AdaptrisSecurityException {
    try {
      CertificateFactory factory = CertificateFactory.getInstance("X.509");
      Certificate x509 = (X509Certificate)factory.generateCertificate(in);
      this.setCertificate(alias, x509);
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
  }

  /**
   * Assign the given File (contaning a certificate) to the given alias.
   * <p>
   * The File is expected to contain a PEM or DER encoded certificate
   * </p>
   *
   * @param alias the alias of the Certificate
   * @param file the file containing the certificate
   * @throws AdaptrisSecurityException for any error
   * @see #setCertificate(String, InputStream)
   */
  public void setCertificate(String alias, File file)
      throws AdaptrisSecurityException {
    try (InputStream in = new FileInputStream(file)) {
      this.setCertificate(alias, in);
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
  }

  /**
   * Assign the given file (contaning a certificate) to the given alias.
   * <p>
   * The File is expected to contain a PEM or DER encoded certificate
   * </p>
   *
   * @param alias the alias of the Certificate
   * @param filename the file containing the certificate
   * @see #setCertificate(String, File)
   * @throws AdaptrisSecurityException for any error
   */
  public void setCertificate(String alias, String filename)
      throws AdaptrisSecurityException {
    this.setCertificate(alias, new File(filename));
  }

  /**
   * Assigns the given key to the given alias, protecting it with the given
   * password.
   * <p>
   * If the given alias already exists, the keystore information associated with
   * it is overridden by the given key (and possibly certificate chain).
   * </p>
   *
   * @param alias the alias of the Certificate
   * @param privKey the PrivateKey
   * @param keyPassword the password to protect the private key
   * @param certChain the certificate chain
   * @throws AdaptrisSecurityException for any error
   */
  public void setPrivateKey(String alias, PrivateKey privKey,
                            char[] keyPassword, Certificate[] certChain)
      throws AdaptrisSecurityException {
    try {
      keyStore.setKeyEntry(alias, privKey, keyPassword, certChain);
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
  }

  /**
   * Import a private key from an inputstream, and assign it to the given alias.
   * <p>
   * The key is protected by the given key password
   * </p>
   * <p>
   * The inputstream is expected to contain a PKCS12 object exported from
   * Netscape Navigator / Internet Explorer
   * </p>
   *
   * @param alias the alias of the Certificate
   * @param keyPassword the password to protect the private key
   * @param in InputStream containing the PKCS12 object
   * @param filePassword The password protecting the PKCS12
   * @throws AdaptrisSecurityException for any error
   * @see #setPrivateKey(String, PrivateKey, char[], Certificate[])
   */
  public void importPrivateKey(String alias, char[] keyPassword,
                               InputStream in, char[] filePassword)
      throws AdaptrisSecurityException {

    try {
      KeyStore keystore = KeyStore.getInstance(Constants.KEYSTORE_PKCS12);//,, Constants.SECURITY_PROVIDER);
      keystore.load(in, filePassword);
      Key key = keystore.getKey(alias, keyPassword);
      if (key instanceof PrivateKey) {
        Certificate[] certChain = keystore.getCertificateChain(alias);
        this.setPrivateKey(alias, (PrivateKey)key, keyPassword, certChain);
      }
    }
    catch (AdaptrisSecurityException e) {
      throw e;
    }
    catch (Exception e) {
      throw new CertException(e.getMessage(), e);
    }
  }

  /**
   * Import a private key from a File, and assign it to the given alias.
   * <p>
   * The key is protected by the given key password
   * </p>
   * <p>
   * The File is expected to contain a PKCS12 object exported from Netscape
   * Navigator / Internet Explorer
   * </p>
   *
   * @param alias the alias of the Certificate
   * @param keyPassword the password to protect the private key
   * @param file the File containing the PKCS12 object
   * @param filePassword The password protecting the PKCS12
   * @see #importPrivateKey(String, char[], InputStream, char[])
   * @throws AdaptrisSecurityException for any error
   */
  public void importPrivateKey(String alias, char[] keyPassword, File file,
                               char[] filePassword)
      throws AdaptrisSecurityException {

    try (InputStream in = new FileInputStream(file)) {
      this.importPrivateKey(alias, keyPassword, in, filePassword);
    }
    catch (IOException e) {
      throw new CertException(e.getMessage(), e);
    }
  }

  /**
   * Import a private key from a File, and assign it to the given alias.
   * <p>
   * The key is protected by the given key password
   * </p>
   * <p>
   * The File is expected to contain a PKCS12 object exported from Netscape
   * Navigator / Internet Explorer
   * </p>
   *
   * @param alias the alias of the Certificate
   * @param keyPassword the password to protect the private key
   * @param file the File containing the PKCS12 object
   * @param filePassword The password protecting the PKCS12
   * @see #importPrivateKey(String, char[], File, char[])
   * @throws AdaptrisSecurityException for any error
   */
  public void importPrivateKey(String alias, char[] keyPassword, String file,
                               char[] filePassword)
      throws AdaptrisSecurityException {
    this.importPrivateKey(alias, keyPassword, new File(file), filePassword);
  }

  /**
   * Import a certificate chain from a file, giving it the assigned alias.
   * <p>
   * Certificate Chains are only appropriate for keystore <code>keyEntry</code>
   * types.
   * <p>
   * This assumes that a <code>keyEntry</code> with the alias
   * <code>alias</code> has already been created, and the secret key
   * associated with this <code>keyEntry</code> is protected by
   * <code>keyPassword</code>
   *
   * @param keyPassword the password to access the private key
   * @param alias the alias to be assigned
   * @param file the Certificate Chain file to be imported
   * @throws AdaptrisSecurityException for any error
   * @see #importCertificateChain(String, char[], File)
   */
  public void importCertificateChain(String alias, char[] keyPassword,
                                     String file)
      throws AdaptrisSecurityException {
    this.importCertificateChain(alias, keyPassword, new File(file));
  }

  /**
   * Import a certificate chain from a file, giving it the assigned alias.
   * <p>
   * Certificate Chains are only appropriate for keystore <code>keyEntry</code>
   * types.
   * <p>
   * This assumes that a <code>keyEntry</code> with the alias
   * <code>alias</code> has already been created, and the secret key
   * associated with this <code>keyEntry</code> is protected by
   * <code>keyPassword</code>
   *
   * @param keyPassword the password to access the private key
   * @param alias the alias to be assigned
   * @param f the Certificate Chain file to be imported
   * @throws AdaptrisSecurityException for any error
   * @see #importCertificateChain(String, char[], InputStream)
   */
  public void importCertificateChain(String alias, char[] keyPassword, File f)
      throws AdaptrisSecurityException {

    try (InputStream in = new FileInputStream(f)) {
      this.importCertificateChain(alias, keyPassword, in);
    }
    catch (IOException e) {
      throw new CertException(e.getMessage(), e);
    }
  }

  /**
   * Import a certificate chain from a file, giving it the assigned alias.
   * <p>
   * This deals with certificate chains as used by Netscape Navigator and
   * Microsoft Internet Explorer.
   * <p>
   * Certificate Chains are only appropriate for keystore <code>keyEntry</code>
   * types.
   * <p>
   * This assumes that a <code>keyEntry</code> with the alias
   * <code>alias</code> has already been created, and the secret key
   * associated with this <code>keyEntry</code> is protected by
   * <code>keyPassword</code>
   *
   * @param keyPassword the password to access the private key
   * @param alias the alias to be assigned
   * @param in the Certificate Chain file to be imported
   * @throws AdaptrisSecurityException for any error
   * @see #setPrivateKey(String, PrivateKey, char[], Certificate[])
   */
  public void importCertificateChain(String alias, char[] keyPassword,
                                     InputStream in)
      throws AdaptrisSecurityException {

    try (PemReader pemReader = new PemReader(new InputStreamReader(in))) {
      CertificateFactory cf = CertificateFactory.getInstance(Constants.KEYSTORE_X509);//,, Constants.SECURITY_PROVIDER);
      Collection<?> certs = cf.generateCertificates(in);
      Certificate[] pkcs7b = certs.toArray(new Certificate[0]);

      PrivateKey pkey = this.getPrivateKey(alias, keyPassword);
      if (pkey == null) {
        throw new Exception("No Private key for alias " + alias);
      }
      this.setPrivateKey(alias, pkey, keyPassword, pkcs7b);
    }
    catch (AdaptrisSecurityException e) {
      throw e;
    }
    catch (Exception e) {
      throw new CertException(e.getMessage(), e);
    }
  }

  /**
   * Checks if the given alias exists in this keystore.
   *
   * @param alias the alias to check of
   * @return true if it does contain this alias
   * @throws AdaptrisSecurityException for any error
   */
  public boolean containsAlias(String alias) throws AdaptrisSecurityException {
    try {
      return keyStore.containsAlias(alias);
    }
    catch (Exception e) {
      throw KeystoreProxy.wrapException(e);
    }
  }

  /**
   * Return the underyling keystore object for manual querying.
   *
   * @return the underlying keystore
   * @see KeyStore
   */
  public KeyStore getKeystore() {
    return keyStore;
  }

  @Override
  public String toString() {
    return "[" + keystoreLocation.toString() + "]";
  }
}
