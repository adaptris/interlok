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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Legacy private key password provider based on the property file 'security.properties'.
 * 
 * <p>
 * The private key password for your local KeyEntry certificate is not stored in the runtime configuration. It is instead sourced
 * from a file <code>security.properties</code> that is expected to be in the classpath.
 * </p>
 * <p>
 * This file only needs to contain a single entry:
 * 
 * <pre>{@code 
 * adaptris.privatekey.password=[private key password]
 * }</pre>
 * </p>
 * 
 * @config legacy-private-key-password-provider
 * @author lchan
 * 
 */
@XStreamAlias("legacy-private-key-password-provider")
public class LegacyPrivateKeyPasswordProvider implements PrivateKeyPasswordProvider {
  private static final String PKEY_PW_FILE = "security.properties";
  private static final String PKEY_PW_KEY = "adaptris.privatekey.password";

  private transient char[] pkPassword;

  public LegacyPrivateKeyPasswordProvider() {

  }

  /**
   * Return the private key password as a char[] array.
   * 
   * @return the private key password sourced from 'security.properties' and decoded using {@link Password#decode(String)}
   */
  @Override
  public char[] retrievePrivateKeyPassword() throws PasswordException {
    if (pkPassword == null) {
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(PKEY_PW_FILE);
      try {
        if (is != null) {
          Properties p = new Properties();
          p.load(is);
          pkPassword = Password.decode(p.getProperty(PKEY_PW_KEY)).toCharArray();
        }
      }
      catch (IOException e) {
        throw new PasswordException(e);
      }
      finally {
        IOUtils.closeQuietly(is);
      }
    }
    return pkPassword;
  }

}
