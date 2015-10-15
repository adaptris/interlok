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

package com.adaptris.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.security.password.Password;

/**
 * Constants specific to HTTPS.
 *
 */
public final class Https {
  private Https() {
  }

  /** SSL_CONTEXT_TYPE is SSL for use with JSSE */
  public static final String SSL_CONTEXT_TYPE = "SSL";
  /** KEY_MANAGER_TYPE is SunX509 for use with JSSE */
  public static final String KEY_MANAGER_TYPE = "SunX509";
  /** Property in our adp-http.properties file */
  public static final String CONFIG_PRIVATE_KEY_PW = "adp.https.privatekeypassword";
  /** Property in our adp-http.properties file */
  public static final String CONFIG_KEYSTORE_URL = "adp.https.keystoreurl";
  /** Property in our adp-http.properties file */
  public static final String CONFIG_KEYSTORE_PW = "adp.https.keystorepassword";
  /** The name of the properties */
  public static final String CONFIG_PROPERTY_FILE = "adp-http.properties";
  private static boolean initialised = false;
  private static Properties httpsProperties;

  static char[] getPrivateKeyPassword(char[] pkpw) throws IOException {
    if (pkpw != null) {
      return pkpw;
    }
    initialise();
    if (httpsProperties.getProperty(CONFIG_PRIVATE_KEY_PW) == null) {
      throw new IOException("Could not find private key to use in "
          + CONFIG_PROPERTY_FILE);
    }
    return httpsProperties.getProperty(CONFIG_PRIVATE_KEY_PW).toCharArray();
  }

  static KeystoreProxy getKeystoreProxy(KeystoreProxy ksp) throws Exception {
    if (ksp != null) {
      return ksp;
    }
    initialise();
    String url = httpsProperties.getProperty(CONFIG_KEYSTORE_URL);
    if (url == null) {
      throw new IOException("Could not a keystore url to use in "
          + CONFIG_PROPERTY_FILE);
    }
    String pw = httpsProperties.getProperty(CONFIG_KEYSTORE_PW);
    KeystoreLocation ksl = null;
    if (pw != null) {
      ksl = KeystoreFactory.getDefault().create(url, Password.decode(pw).toCharArray());
    }
    else {
      ksl = KeystoreFactory.getDefault().create(url);
    }
    return KeystoreFactory.getDefault().create(ksl);
  }

  private static void initialise() throws IOException {
    if (initialised) {
      return;
    }
    httpsProperties = new Properties();
    InputStream is = Https.class.getClassLoader().getResourceAsStream(
        CONFIG_PROPERTY_FILE);
    if (is != null) {
      httpsProperties.load(is);
      is.close();
    }
    initialised = true;
  }
}
