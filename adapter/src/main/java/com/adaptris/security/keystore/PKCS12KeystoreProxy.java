/*
 * $Author: lchan $
 * $RCSfile: PKCS12KeystoreProxy.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/03/07 17:22:13 $
 */
package com.adaptris.security.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.util.Constants;

/**
 * Keystore Proxy implementation that only handles a single certificate that is
 * stored in PKCS12 format.
 *
 * @author $Author: lchan $
 */
class PKCS12KeystoreProxy extends SingleEntryKeystoreProxy {

  private KeyStore keystore = null;

  /**
   * Default Constructor.
   */
  public PKCS12KeystoreProxy() {
  }

  /**
   * Construct the object using the KeyStoreInfo object.
   *
   * @param k the KeyStoreInfo object
   * @see KeystoreLocation
   * @throws AdaptrisSecurityException if an error is encountered
   */
  public PKCS12KeystoreProxy(KeystoreLocation k) throws AdaptrisSecurityException {
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
      keystore = KeyStore.getInstance(Constants.KEYSTORE_PKCS12);//,, Constants.SECURITY_PROVIDER);
      keystore.load(in, getKeystoreLocation().getKeystorePassword());
    }
    catch (Exception e) {
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
    return;
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
  public PrivateKey getPrivateKey(String alias, char[] keyPassword) throws AdaptrisSecurityException {
    PrivateKey pk = null;
    if (getAliasName().equalsIgnoreCase(alias)) {
      // We should be able to ignore the keyPassword at this point
      // As the file itself is encrypted with a pw.
      try {
        pk = (PrivateKey)keystore.getKey(alias, keyPassword);
      }
      catch (Exception e) {
        throw new AdaptrisSecurityException(e);
      }
    }
    else {
      throw new KeystoreException(alias + " not found");
    }
    return pk;
  }

  /**
   * Return the certificate specified by the given alias.
   *
   * @param alias the alias of the Certificate
   * @return Certificate the requested certificate, or null if the alias does
   *         not exist/not a certificate
   * @throws AdaptrisSecurityException for any error
   */
  public Certificate getCertificate(String alias) throws AdaptrisSecurityException {
    Certificate cert = null;
    if (getAliasName().equalsIgnoreCase(alias)) {
      try {
        Certificate[] certChain = keystore.getCertificateChain(alias);
        // for (int i = 0; i < certChain.length; i++) {
        // logR.debug(certChain[i].getSubjectDN());
        // }
        cert = certChain[0];
      }
      catch (Exception e) {
        throw new AdaptrisSecurityException(e);
      }
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
  public Certificate[] getCertificateChain(String alias) throws AdaptrisSecurityException {
    Certificate[] certChain = null;
    if (getAliasName().equalsIgnoreCase(alias)) {
      try {
        certChain = keystore.getCertificateChain(alias);
      }
      catch (Exception e) {
        throw new AdaptrisSecurityException(e);
      }
    }
    else {
      throw new KeystoreException(alias + " not found");
    }
    return certChain;
  }

  @Override
  protected KeyStore buildTemporaryKeystore() {
    KeyStore ks = null;
    try {
      ks = KeyStore.getInstance(DEFAULT_KS_IMPL);
      ks.load(null, null);

      char[] pw = getKeystoreLocation().getKeystorePassword();
      String alias = getAliasName();

      Certificate[] certChain = keystore.getCertificateChain(alias);
      PrivateKey pk = (PrivateKey)keystore.getKey(alias, pw);

      ks.setKeyEntry(alias, pk, pw, certChain);
    }
    catch (Exception e) {
      logR.warn("Failed to create a temporary keystore [" + e.getMessage() + "], returning null");
      ks = null;
    }
    return ks;
  }

}