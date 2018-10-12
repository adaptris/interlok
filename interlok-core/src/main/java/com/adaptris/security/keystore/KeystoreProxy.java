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

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.KeystoreException;

/**
 * Used to read and write from a keystore.
 * <p>
 * The KeystoreProxy class is used to manage Partner Certificates and Private Keys stored in a Java Keystore.
 * </p>
 * <p>
 * Any sun keystore provider can be used, provided that it is added as a provider before any concrete implementations are
 * initialised. The default SUN keystore implementation JKS (or JCEKS in JDK1.4) is provided as part of the JRE
 * </p>
 * <p>
 * In additional to the standard types the following are also supported :
 * <ul>
 * <li>BKS - Provided by the BC JCE Implementation</li>
 * <li>PKCS12 - where you wish to proxy a single PKCS12 file containing a private key</li>
 * <li>X509 - where you wish to proxy a single file containing a certificate.</li>
 * <li>XmlKeyInfo - where you wish to proxy a XML KeyInfo element that contains certifcate information</li>
 * </ul>
 * When using custom implementations such as PKCS12, then you also need to provide an alias (
 * {@link com.adaptris.security.util.Constants#KEYSTORE_ALIAS}) to be associated with the file. Additionally, the proxy is
 * implicitly readonly, regardless of where the physical file is held.
 * </p>
 * <p>
 * Example usage
 * </p>
 * 
 * <pre>
 * {@code 
 *    String url = "file://localhost/path/to/keystore?keyStoreType=JKS"
 *    char[] ksPw = "myPassword".toCharArray();
 *    KeystoreLocation kloc = KeystoreFactory.getDefault().create(url, ksPw);
 *    KeystoreProxy ksm = KeystoreFactory.getDefault().create(kloc);
 *    ksm.load();
 *    PrivateKey key = ksm.getPrivateKey("myAlias", "myPassword".toCharArray());
 *
 *    KeystoreLocation k2 = KeystoreFactory.getDefault().create(u2);
 *    KeystoreProxy ksm2 = KeystoreFactory.getDefault().create(k2);
 *    ksm2.importPrivateKey("myAlias", "myKeyPassword".toCharArray()
 *                         "myPFXfile", myPFXpassword.toCharArray());
 *    ksm2.setCertificate("partnerA", partnerCert);
 *    ksm2.commit();
 * }
 * </pre>
 * 
 * @author lchan
 * @author $Author: lchan $
 * @see KeystoreFactory
 * @see KeystoreLocation
 */
public interface KeystoreProxy {
  /**
   * Set the internal KeystoreLocation object to be used.
   * <p>
   * Based on this information, a new KeyStore object is created, and
   * initialised.
   * 
   * @param k the KeystoreProxy object
   * @see KeystoreLocation
   * @throws AdaptrisSecurityException wrapping the underlying exception
   */
  void setKeystoreLocation(KeystoreLocation k) throws AdaptrisSecurityException;

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
  void load() throws AdaptrisSecurityException, IOException;

  /**
   * Save the contents of the keystore to file.
   * 
   * @throws AdaptrisSecurityException if an error was encountered writing to
   *           the keystore
   * @throws IOException if the file could not be written to
   * @see KeystoreLocation#isWriteable()
   */
  default void commit() throws AdaptrisSecurityException, IOException {
    throw new KeystoreException("Default behaviour is read-only");
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
  PrivateKey getPrivateKey(String alias, char[] keyPassword)
      throws AdaptrisSecurityException;

  /**
   * Return the certificate specified by the given alias.
   * 
   * @param alias the alias of the Certificate
   * @return Certificate the requested certificate, or null if the alias does
   *         not exist/not a certificate
   * @throws AdaptrisSecurityException for any error
   */
  Certificate getCertificate(String alias) throws AdaptrisSecurityException;

  /**
   * Return the certificate specified by the given alias.
   * 
   * @param alias the alias of the Certificate Chain
   * @return the requested certificate chain, or null if the alias does not
   *         exist/not a certificate.
   * @throws AdaptrisSecurityException for any error
   */
  Certificate[] getCertificateChain(String alias)
      throws AdaptrisSecurityException;

  /**
   * Assign the given certificate to the given alias.
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param alias the alias of the Certificate
   * @param cert the Certificate
   * @throws AdaptrisSecurityException for any error
   */
  default void setCertificate(String alias, Certificate cert) throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Assign the given InputStream (contaning a certificate) to the given alias.
   * <p>
   * The InputStream is expected to contain a PEM or DER encoded certificate
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param alias the alias of the Certificate
   * @param in the InputStream containing the certificate
   * @throws AdaptrisSecurityException for any error
   * @see #setCertificate(String, Certificate)
   */
  default void setCertificate(String alias, InputStream in) throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Assign the given File (contaning a certificate) to the given alias.
   * <p>
   * The File is expected to contain a PEM or DER encoded certificate
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param alias the alias of the Certificate
   * @param file the file containing the certificate
   * @throws AdaptrisSecurityException for any error
   * @see #setCertificate(String, InputStream)
   */
  default void setCertificate(String alias, File file) throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Assign the given file (contaning a certificate) to the given alias.
   * <p>
   * The File is expected to contain a PEM or DER encoded certificate
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param alias the alias of the Certificate
   * @param filename the file containing the certificate
   * @see #setCertificate(String, File)
   * @throws AdaptrisSecurityException for any error
   */
  default void setCertificate(String alias, String filename) throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Assigns the given key to the given alias, protecting it with the given password.
   * <p>
   * If the given alias already exists, the keystore information associated with it is overridden by the given key (and possibly
   * certificate chain).
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param alias the alias of the Certificate
   * @param privKey the PrivateKey
   * @param keyPassword the password to protect the private key
   * @param certChain the certificate chain
   * @throws AdaptrisSecurityException for any error
   */
  default void setPrivateKey(String alias, PrivateKey privKey, char[] keyPassword, Certificate[] certChain)
      throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Import a private key from an inputstream, and assign it to the given alias.
   * <p>
   * The key is protected by the given key password; The inputstream is expected to contain a KEYSTORE_PKCS12 object exported from
   * Netscape Navigator / Internet Explorer
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param alias the alias of the Certificate
   * @param keyPassword the password to protect the private key
   * @param in InputStream containing the KEYSTORE_PKCS12 object
   * @param filePassword The password protecting the KEYSTORE_PKCS12
   * @throws AdaptrisSecurityException for any error
   * @see #setPrivateKey(String, PrivateKey, char[], Certificate[])
   */
  default void importPrivateKey(String alias, char[] keyPassword, InputStream in, char[] filePassword)
      throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Import a private key from a File, and assign it to the given alias.
   * <p>
   * The key is protected by the given key password; The File is expected to contain a KEYSTORE_PKCS12 object exported from Netscape
   * Navigator / Internet Explorer
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param alias the alias of the Certificate
   * @param keyPassword the password to protect the private key
   * @param file the File containing the KEYSTORE_PKCS12 object
   * @param filePassword The password protecting the KEYSTORE_PKCS12
   * @see #importPrivateKey(String, char[], InputStream, char[])
   * @throws AdaptrisSecurityException for any error
   */
  default void importPrivateKey(String alias, char[] keyPassword, File file, char[] filePassword) throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Import a private key from a File, and assign it to the given alias.
   * <p>
   * The key is protected by the given key password; The File is expected to contain a KEYSTORE_PKCS12 object exported from Netscape
   * Navigator / Internet Explorer
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param alias the alias of the Certificate
   * @param keyPassword the password to protect the private key
   * @param file the File containing the KEYSTORE_PKCS12 object
   * @param filePassword The password protecting the KEYSTORE_PKCS12
   * @see #importPrivateKey(String, char[], File, char[])
   * @throws AdaptrisSecurityException for any error
   */
  default void importPrivateKey(String alias, char[] keyPassword, String file, char[] filePassword)
      throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Import a certificate chain from a file, giving it the assigned alias.
   * <p>
   * Certificate Chains are only appropriate for keystore <code>keyEntry</code> types.
   * This assumes that a <code>keyEntry</code> with the alias <code>alias</code> has already been created, and the secret key
   * associated with this <code>keyEntry</code> is protected by <code>keyPassword</code>
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param keyPassword the password to access the private key
   * @param alias the alias to be assigned
   * @param file the Certificate Chain file to be imported
   * @throws AdaptrisSecurityException for any error
   * @see #importCertificateChain(String, char[], File)
   */
  default void importCertificateChain(String alias, char[] keyPassword, String file)
      throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Import a certificate chain from a file, giving it the assigned alias.
   * <p>
   * Certificate Chains are only appropriate for keystore <code>keyEntry</code> types.
   * This assumes that a <code>keyEntry</code> with the alias <code>alias</code> has already been created, and the secret key
   * associated with this <code>keyEntry</code> is protected by <code>keyPassword</code>
   * </p>
   * 
   * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * @param keyPassword the password to access the private key
   * @param alias the alias to be assigned
   * @param f the Certificate Chain file to be imported
   * @throws AdaptrisSecurityException for any error
   * @see #importCertificateChain(String, char[], InputStream)
   */
  default void importCertificateChain(String alias, char[] keyPassword, File f) throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Import a certificate chain from a file, giving it the assigned alias.
   * <p>
   * This deals with certificate chains as used by Netscape Navigator and Microsoft Internet Explorer; Certificate Chains are only
   * appropriate for keystore <code>keyEntry</code> types. This assumes that a <code>keyEntry</code> with the alias
   * <code>alias</code> has already been created, and the secret key associated with this <code>keyEntry</code> is protected by
   * <code>keyPassword</code>
   * </p>
   * * @implSpec The default implementation throws an instance of {@link AdaptrisSecurityException} and performs no other action.
   * 
   * @param keyPassword the password to access the private key
   * @param alias the alias to be assigned
   * @param in the Certificate Chain file to be imported
   * @throws AdaptrisSecurityException for any error
   * @see #setPrivateKey(String, PrivateKey, char[], Certificate[])
   */
  default void importCertificateChain(String alias, char[] keyPassword, InputStream in)
      throws AdaptrisSecurityException {
    throw new KeystoreException("Default behaviour is read-only");
  }

  /**
   * Checks if the given alias exists in this keystore.
   * 
   * @param alias the alias to check of
   * @return true if it does contain this alias
   * @throws AdaptrisSecurityException for any error
   */
  boolean containsAlias(String alias) throws AdaptrisSecurityException;

  /**
   * Return the underyling keystore object for manual querying.
   * 
   * @return the underlying keystore, maybe null if there is no underlying
   *         keystore
   * @see KeyStore
   */
  KeyStore getKeystore();

  static KeystoreException wrapException(Throwable orig) {
    if (orig instanceof KeystoreException) {
      return (KeystoreException) orig;
    }
    return new KeystoreException(orig.getMessage(), orig);
  }
}
