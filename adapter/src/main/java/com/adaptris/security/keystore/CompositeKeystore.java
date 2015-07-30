/*
 * $Author: lchan $
 * $RCSfile: CompositeKeystore.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/03/07 17:22:13 $
 */
package com.adaptris.security.keystore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.util.Constants;

/**
 * Composite keystore which manages keystores of different types.
 * <p>
 * This is essentially a convenience wrapper around a list of KeystoreProxy
 * objects and exposes commonly used methods.
 * </p>
 * <p>
 * The CompositeKeystore is case insensitive for matching aliases, MYPRIVATEKEY
 * is considered semantically equivalent to myprivatekey
 * </p>
 *
 * @author lchan
 * @author $Author: lchan $
 * @version $Revision: 1.5 $
 */
public class CompositeKeystore implements KeystoreProxy {

  private List<KeystoreLocation> keystores;
  private Hashtable<String, AliasListEntry> aliasCache;
  private KeystoreFactory keystoreFactory;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass());

  /**
   * Default constructor.
   */
  public CompositeKeystore() {
    keystores = new ArrayList<KeystoreLocation>();
    keystoreFactory = KeystoreFactory.getDefault();
  }

  /**
   * Constructor using the default KeystoreFactory
   *
   * @param l a list of KeystoreLocation objects
   */
  public CompositeKeystore(List<KeystoreLocation> l) {
    this(KeystoreFactory.getDefault(), l);

  }

  /**
   * Constructor supplying both the factory and list of keystore locations.
   *
   * @param kf the keystoreFactory to use.
   * @param keystoreList a list of KeystoreLocation objects
   */
  public CompositeKeystore(KeystoreFactory kf, List<KeystoreLocation> keystoreList) {
    this();
    setKeystores(keystoreList);
    keystoreFactory = kf;
  }

  /**
   * Adds a keystore file to the keystore list.
   *
   * @param keyFile the name of the keystore file
   */
  public void addKeystore(KeystoreLocation keyFile) {
    keystores.add(keyFile);
  }

  /**
   *
   * @param l the list of KeystoreLocation objects.
   */
  public void setKeystores(List<KeystoreLocation> l) {
    this.keystores = l;
  }

  /**
   * @see KeystoreProxy#load()
   */
  public void load() throws AdaptrisSecurityException {
    aliasCache = new Hashtable<String, AliasListEntry>();
    try {
      for (KeystoreLocation k : keystores) {
        KeystoreProxy kp = keystoreFactory.create(k);
        kp.load();
        KeyStore ks = kp.getKeystore();
        AliasListEntry kk = new AliasListEntry(kp, k);
        if (ks != null) {
          for (Enumeration<String> e = kp.getKeystore().aliases(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (!addToAliases(key, kk)) {
              logR.warn(key + " already exists in keystore group," + " ignoring " + key + " in " + k);
            }
          }
        }
        else {
          String key = k.getAdditionalParams().getProperty(Constants.KEYSTORE_ALIAS);
          if (!addToAliases(key, kk)) {
            logR.warn(key + " already exists in keystore group," + " ignoring " + key + " in " + k);
          }
        }
      }
    }
    catch (GeneralSecurityException e) {
      aliasCache = null;
      throw new KeystoreException(e);
    }
    catch (IOException e) {
      aliasCache = null;
      throw new KeystoreException(e);
    }
  }

  private boolean addToAliases(String key, AliasListEntry kp) {
    boolean rc = false;
    if (!aliasCache.containsKey(key)) {
      aliasCache.put(key, kp);
      rc = true;
    }
    return rc;
  }

  /**
   * Gets all the aliases of the keystores pointed by this composite keystore.
   *
   * @return an enumeration of string, holding the aliases of the keys
   */
  public Enumeration<String> aliases() throws AdaptrisSecurityException {
    if (aliasCache == null) {
      load();
    }
    return aliasCache.keys();
  }

  /**
   * Gets the total number of keys/certificates in all the keystores pointed by
   * this composite keystore.
   *
   * @return the total number of keys/certificates
   */
  public int size() throws AdaptrisSecurityException {
    if (aliasCache == null) {
      load();
    }
    return aliasCache.size();
  }

  /**
   * @see KeystoreProxy#containsAlias(String)
   */
  public boolean containsAlias(String alias) throws AdaptrisSecurityException {
    if (aliasCache == null) {
      load();
    }
    if (alias == null) {
      return false;
    }
    return aliasCache.containsKey(alias.toLowerCase());
  }

  /**
   * @see KeystoreProxy#getCertificate(String)
   */
  public Certificate getCertificate(String alias) throws AdaptrisSecurityException {
    if (aliasCache == null) {
      load();
    }
    if (!containsAlias(alias)) {
      return null;
    }
    String lca = alias.toLowerCase();
    AliasListEntry kk = aliasCache.get(lca);
    if (kk != null) {
      return kk.getProxy().getCertificate(lca);
    }
    return null;
  }

  /**
   * @see KeystoreProxy#getPrivateKey(String, char[])
   */
  public PrivateKey getPrivateKey(String alias, char[] password) throws AdaptrisSecurityException {
    PrivateKey pk = null;
    if (aliasCache == null) {
      load();
    }
    if (!containsAlias(alias)) {
      return null;
    }
    String lca = alias.toLowerCase();
    AliasListEntry kk = aliasCache.get(lca);
    if (kk != null) {
      if (password == null) {
        logR.trace("No private key password passed as parameter, " + "using keystore password as key password");
      }
      try {
        char[] pw = password == null ? kk.getLocation().getKeystorePassword() : password;
        pk = kk.getProxy().getPrivateKey(lca, pw);
      }
      catch (Exception e) {
        if (AdaptrisSecurityException.class.isAssignableFrom(e.getClass())) {
          throw (AdaptrisSecurityException) e;
        }
        else {
          throw new KeystoreException(e);
        }
      }
    }
    return pk;
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
   * @see KeystoreProxy#getCertificateChain(java.lang.String)
   */
  public Certificate[] getCertificateChain(String alias) throws AdaptrisSecurityException {
    Certificate[] certChain = null;
    if (aliasCache == null) {
      load();
    }
    if (!containsAlias(alias)) {
      return null;
    }
    AliasListEntry kk = aliasCache.get(alias.toLowerCase());
    if (kk != null) {
      certChain = kk.getProxy().getCertificateChain(alias.toLowerCase());
    }
    return certChain;
  }

  /**
   * Return the keystore.
   * <p>
   * In this instance, the keystore object is always null as we do not wrap
   * multiple keystore.
   * </p>
   *
   * @see com.adaptris.security.keystore.KeystoreProxy#getKeystore()
   */
  public KeyStore getKeystore() {
    return null;
  }

  /**
   *
   * @see KeystoreProxy#importCertificateChain(String, char[], String)
   */
  public void importCertificateChain(String alias, char[] keyPassword, String file) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see KeystoreProxy#importCertificateChain(String, char[], java.io.File)
   */
  public void importCertificateChain(String alias, char[] keyPassword, File f) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see KeystoreProxy#importCertificateChain(java.lang.String, char[],
   *      java.io.InputStream)
   */
  public void importCertificateChain(String alias, char[] keyPassword, InputStream in) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreProxy#importPrivateKey(java.lang.String,
   *      char[], java.io.InputStream, char[])
   */
  public void importPrivateKey(String alias, char[] keyPassword, InputStream in, char[] filePassword)
      throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreProxy#importPrivateKey(java.lang.String,
   *      char[], java.io.File, char[])
   */
  public void importPrivateKey(String alias, char[] keyPassword, File file, char[] filePassword) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreProxy#importPrivateKey(java.lang.String,
   *      char[], java.lang.String, char[])
   */
  public void importPrivateKey(String alias, char[] keyPassword, String file, char[] filePassword) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreProxy#setCertificate(java.lang.String,
   *      java.security.cert.Certificate)
   */
  public void setCertificate(String alias, Certificate cert) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreProxy#setCertificate(java.lang.String,
   *      java.io.InputStream)
   */
  public void setCertificate(String alias, InputStream in) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see KeystoreProxy#setCertificate(java.lang.String, java.io.File)
   */
  public void setCertificate(String alias, File file) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see KeystoreProxy#setCertificate(java.lang.String, java.lang.String)
   */
  public void setCertificate(String alias, String filename) throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  /**
   *
   * @see KeystoreProxy#setKeystoreLocation(KeystoreLocation)
   */
  public void setKeystoreLocation(KeystoreLocation k) throws AdaptrisSecurityException {
    addKeystore(k);
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreProxy#setPrivateKey(java.lang.String,
   *      java.security.PrivateKey, char[], java.security.cert.Certificate[])
   */
  public void setPrivateKey(String alias, PrivateKey privKey, char[] keyPassword, Certificate[] certChain)
      throws AdaptrisSecurityException {
    throw new KeystoreException(this.getClass() + " is implicitly read-only");
  }

  private class AliasListEntry {

    private KeystoreProxy keystoreProxy;
    private KeystoreLocation keystoreLocation;

    AliasListEntry(KeystoreProxy kp, KeystoreLocation kl) {
      keystoreProxy = kp;
      keystoreLocation = kl;
    }

    KeystoreProxy getProxy() {
      return keystoreProxy;
    }

    KeystoreLocation getLocation() {
      return keystoreLocation;
    }
  }
}
