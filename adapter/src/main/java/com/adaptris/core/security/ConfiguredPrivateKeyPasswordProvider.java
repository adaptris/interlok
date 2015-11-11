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

package com.adaptris.core.security;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Private key password provider which explicitly configures the password in configuration
 * 
 * <p>
 * Although there is nothing to stop you from having a plain text private key password configured in adapter configuration using
 * this provider; it is strongly discouraged. You should consider using the {@link com.adaptris.security.password.Password#encode(String, String)} method to
 * generate an encoded password for inserting into configuration.
 * </p>
 * 
 * @config configured-private-key-password-provider
 * @author lchan
 * @see Password#decode(String)
 * 
 */
@XStreamAlias("configured-private-key-password-provider")
public class ConfiguredPrivateKeyPasswordProvider implements PrivateKeyPasswordProvider {
  private transient char[] pkPassword;
  @InputFieldHint(style = "PASSWORD")
  private String encodedPassword = null;

  public ConfiguredPrivateKeyPasswordProvider() {

  }

  public ConfiguredPrivateKeyPasswordProvider(String encPassword) {
    this();
    setEncodedPassword(encPassword);
  }

  /**
   * Return the private key password as a char[] array.
   * 
   * @return the configured private key, decoded using {@link com.adaptris.security.password.Password#decode(String)}
   */
  @Override
  public char[] retrievePrivateKeyPassword() throws PasswordException {
    if (pkPassword == null && !isEmpty(encodedPassword)) {
      pkPassword = Password.decode(encodedPassword).toCharArray();
    }
    return pkPassword;
  }

  public String getEncodedPassword() {
    return encodedPassword;
  }

  public void setEncodedPassword(String encodedPassword) {
    // don't bother checking for null, as it's perfectly valid.
    this.encodedPassword = encodedPassword;
  }

}
