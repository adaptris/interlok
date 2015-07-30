/*
 * $Author: lchan $
 * $RCSfile: ConfiguredKeystore.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/18 12:59:54 $
 */
package com.adaptris.security.keystore;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;

/**
 * Abstract class for presenting arbitary configuation as KeystoreLocation
 * objects.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class ConfiguredKeystore {

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());
  private KeystoreFactory keystoreFactory = null;
  private transient KeystoreProxy proxy = null;

  public ConfiguredKeystore() {
  }

  public abstract KeystoreLocation asKeystoreLocation()
      throws AdaptrisSecurityException;

  /**
   * @return the keystoreFactory
   */
  public final KeystoreFactory getKeystoreFactory() {
    return keystoreFactory;
  }

  /**
   * @param ksf the keystoreFactory to set
   */
  public final void setKeystoreFactory(KeystoreFactory ksf) {
    keystoreFactory = ksf;
  }

  protected static KeystoreFactory getKeystoreFactory(ConfiguredKeystore k) {
    KeystoreFactory ksf = KeystoreFactory.getDefault();
    if (k != null) {
      ksf = k.getKeystoreFactory() != null ? k.getKeystoreFactory() : ksf;
    }
    return ksf;
  }

  public final KeystoreProxy asKeystoreProxy()
      throws AdaptrisSecurityException, IOException {
    if (proxy == null) {
      KeystoreLocation ks = asKeystoreLocation();
      KeystoreFactory ksf = getKeystoreFactory(this);
      proxy = ksf.create(ks);
      proxy.load();
    }
    return proxy;
  }

}
