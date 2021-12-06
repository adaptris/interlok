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

package com.adaptris.http.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import com.adaptris.core.security.PrivateKeyPasswordProvider;
import com.adaptris.http.Https;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Legacy private key password provider based on the property file 'adp-http.properties'.
 *
 * <p>
 * This retrieves the password from the file 'adp-http.properties' which is expected to be on the classpath; the property containing
 * the private key is {@value com.adaptris.http.Https#CONFIG_PRIVATE_KEY_PW} within this file.
 * </p>
 *
* <p>
 * In the adapter configuration file this class is aliased as <b>http-legacy-private-key-password-provider</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 *
 */
@XStreamAlias("http-legacy-private-key-password-provider")
public class HttpLegacyPrivateKeyPasswordProvider implements PrivateKeyPasswordProvider {

  private char[] pkPassword;

  public HttpLegacyPrivateKeyPasswordProvider() {

  }

  /**
   * Return the private key password as a char[] array.
   *
   * @return the private key sourced from 'adp-http.properties' and decoded using {@link com.adaptris.security.password.Password#decode(String)}
   */
  @Override
  public char[] retrievePrivateKeyPassword() throws PasswordException {
    if (pkPassword == null) {
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(Https.CONFIG_PROPERTY_FILE);
      try {
        if (is != null) {
          Properties p = new Properties();
          p.load(is);
          pkPassword = Password.decode(p.getProperty(Https.CONFIG_PRIVATE_KEY_PW)).toCharArray();
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
