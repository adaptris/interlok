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

import java.util.Properties;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.util.Constants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Specifically presents an embedded encoded Certificate string as a KeystoreLocation object.
 * 
 * @config inline-keystore
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("inline-keystore")
@DisplayOrder(order = {"alias", "certificate", "type"})
public class InlineKeystore extends ConfiguredKeystore {

  private String certificate;
  private transient InlineKeystoreLocation keystoreLocation;
  @AdvancedConfig
  private String type;
  private String alias;

  public InlineKeystore() {
    super();
    setType(Constants.KEYSTORE_XMLKEYINFO);
  }

  /**
   *
   * @see com.adaptris.security.keystore.ConfiguredKeystore#asKeystoreLocation()
   */
  @Override
  public synchronized KeystoreLocation asKeystoreLocation()
      throws AdaptrisSecurityException {
    if (keystoreLocation == null) {
      keystoreLocation = new InlineKeystoreLocation(getCertificate().getBytes());
      keystoreLocation.setKeystoreType(getType());
      keystoreLocation.setKeystorePassword("".toCharArray());
      Properties p = new Properties();
      p.setProperty(Constants.KEYSTORE_ALIAS, getAlias());
      keystoreLocation.setAdditionalParams(p);
    }
    return keystoreLocation;
  }

  /**
   * @return the certificate
   */
  public String getCertificate() {
    return certificate;
  }

  /**
   * @param c the certificate to set
   */
  public void setCertificate(String c) {
    certificate = c;
  }

  /**
   *
   * @see ConfiguredKeystore#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    boolean result = false;
    if (o instanceof InlineKeystore) {
      result = certificate.equals(((InlineKeystore) o).certificate);
    }
    return result;
  }

  /**
   *
   * @see com.adaptris.security.keystore.ConfiguredKeystore#hashCode()
   */
  @Override
  public int hashCode() {
    if (certificate != null) {
      return certificate.hashCode();
    }
    return 0;
  }

  /**
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "InlineKeystore=[" + alias + "][" + type + "]";
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Set the type of keystore this is.
   * <p>
   * Supported types are
   * <ul>
   * <li>XMLKEYINFO</li>
   * <li>X509</li>
   * </ul>
   *
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * @param alias the alias to set
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }

}
