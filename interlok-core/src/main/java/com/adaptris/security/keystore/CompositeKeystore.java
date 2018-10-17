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
              logR.warn("{} already exists in keystore group, ignoring {} in {}", key, key, k);
            }
          }
        }
        else {
          String key = k.getAdditionalParams().getProperty(Constants.KEYSTORE_ALIAS);
          if (!addToAliases(key, kk)) {
            logR.warn("{} already exists in keystore group, ignoring {} in {}", key, key, k);
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
   * @see KeystoreProxy#setKeystoreLocation(KeystoreLocation)
   */
  public void setKeystoreLocation(KeystoreLocation k) throws AdaptrisSecurityException {
    addKeystore(k);
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
