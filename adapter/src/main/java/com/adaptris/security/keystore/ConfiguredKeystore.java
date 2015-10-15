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
