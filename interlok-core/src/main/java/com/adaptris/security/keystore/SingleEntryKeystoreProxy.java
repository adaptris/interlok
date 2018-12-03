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

import java.security.KeyStore;

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
