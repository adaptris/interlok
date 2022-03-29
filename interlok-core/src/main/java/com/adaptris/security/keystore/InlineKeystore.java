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

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.util.Constants;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Properties;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

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

  /** The Encoded certificate
   *
   */
  @Getter
  @Setter
  @NotBlank(message="The certificate associated with an inline keystore may not be blank")
  @MarshallingCDATA
  private String certificate;

  /**
   * The type of keystore this is.
   * <p>
   * Supported types are
   * <ul>
   * <li>XMLKEYINFO</li>
   * <li>X509</li>
   * </ul>
   * </p>
   */
  @Getter
  @Setter
  @AdvancedConfig
  private String type;
  /** The Alias to be associated with this certificate.
   *
   */
  @Getter
  @Setter
  @NotBlank(message="The alias associated with the certificate may not be blank.")
  private String alias;
  private transient InlineKeystoreLocation keystoreLocation;

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

}
