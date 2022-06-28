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

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Allows the wrapping of a configured URL as a KeystoreLocation
 *
 * @config configured-keystore-url
 *
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("configured-keystore-url")
@DisplayOrder(order = {"url", "password"})
@NoArgsConstructor
public class ConfiguredUrl extends ConfiguredKeystore {

  /** The URL to the keystore.
   *
   */
  @NotBlank(message="The URL for the keystore may not be null")
  @Getter
  @Setter
  private String url;
  /**
   * The password to the keystore.
   * <p>
   * In additional to plain text passwords, the passwords can also be encoded using the appropriate {@link
   * com.adaptris.security.password.Password}
   * </p>
   */
  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  private String keystorePassword;
  private transient KeystoreLocation keystoreLocation;

  public ConfiguredUrl(String url) {
    this();
    setUrl(url);
  }

  public ConfiguredUrl(String url, String password) {
    this();
    setUrl(url);
    setKeystorePassword(password);
  }

  /**
   *
   * @see com.adaptris.security.keystore.ConfiguredKeystore#asKeystoreLocation()
   */
  @Override
  public synchronized KeystoreLocation asKeystoreLocation() throws AdaptrisSecurityException {
    KeystoreFactory ksf = getKeystoreFactory(this);
    if (keystoreLocation == null) {
      if (getKeystorePassword() != null) {
        keystoreLocation = ksf.create(url,
            Password.decode(ExternalResolver.resolve(getKeystorePassword())).toCharArray());
      } else {
        keystoreLocation = ksf.create(url);
      }
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
    if (o instanceof ConfiguredUrl) {
      result = url.equals(((ConfiguredUrl) o).url);
    }
    return result;
  }

  /**
   *
   * @see ConfiguredKeystore#hashCode()
   */
  @Override
  public int hashCode() {
    if (url != null) {
      return url.hashCode();
    }
    return 0;
  }

  @Override
  public String toString() {
    return "ConfiguredUrl=[" + url + "]";
  }

}
