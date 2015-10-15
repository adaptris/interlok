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

package com.adaptris.http.test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

/**
 */
public class Config {

  /** The configuration */
  private static Properties config = null;
  static final byte[] POST_BYTES = "The quick brown fox jumps over the lazy dog"
      .getBytes();

  static final String HTTPS_CLIENT_EXTERNAL_URL = "https.client.external.url";
  static final String HTTP_CLIENT_EXTERNAL_URL = "http.client.external.url";
  static final String HTTPS_ALWAYSTRUST = "https.alwaystrust";
  static final String HTTPS_KEYSTOREPASSWORD = "https.keystorepassword";
  static final String HTTPS_PRIVATEKEY_PASSWORD = "adp.https.privatekeypassword";
  static final String HTTPS_KEYSTOREURL = "https.keystoreurl";


  static final String LOOPBACK_REDIRECTURI = "http.client.loopback.redirecturi";

  static final String LOOPBACK_URI_NOTFOUND = "http.client.loopback.urinotfound";

  static final String LOOPBACK_URI = "http.client.loopback.uri";

  static final String HTTP_SERVER_REQUESTPROCESSORS = "http.server.requestprocessors";
  static final String HTTPS_SERVER_LISTEN_PORT = "https.server.listen.port";
  static final String HTTP_SERVER_LISTEN_PORT = "http.server.listen.port";
  static final String HTTP_SERVER_LISTENER = "http.server.listener";

  static final String CONFIG_FILE = "http-test.properties";
  static final String STOP_FILE = "http-test.stop";

  static final String HTTPS_CLIENT_PKCS12_KEYSTORE = "https.pkcs12.keystore.url";
  static final String HTTPS_CLIENT_PKCS12_KEYSTORE_PW = "https.pkcs12.keystore.password";
  static final String HTTPS_CLIENT_PKCS12_KEYSTORE_PKPW = "https.pkcs12.keystore.privatekey.password";
  static final String HTTPS_CLIENT_PKCS12_KEYSTORE_BAD_PKPW = "https.pkcs12.keystore.privatekey.password.dummy";

  static {
    try {
      InputStream is = Config.class.getClassLoader().getResourceAsStream(
          CONFIG_FILE);

      if (is != null) {
        config = new Properties();
        config.load(is);
        is.close();
      }
    }
    catch (Exception e) {
      ;
    }
  }

  static Properties getConfig() {
    return config;
  }

  static synchronized SimpleHttpServer createHttpServer() throws Exception {
    return new SimpleHttpServer(config);
  }

  /**
   * This method allows you to get sets from keys. An example would be, if you
   * passed in 'myname.blah.' then you will be returned all the properties whose
   * key starts with 'myname.blah.'
   *
   * @param s what the key should start with
   * @return all the properties whose key starts with startString
   */
  static Properties getPropertySubset(String s, Properties p) {

    Properties newProperties = new Properties();
    Iterator i = p.keySet().iterator();
    while (i.hasNext()) {
      String key = i.next().toString();
      if (key.startsWith(s)) {
        newProperties.setProperty(key, p.getProperty(key));
      }
    }
    return newProperties;
  }
}
