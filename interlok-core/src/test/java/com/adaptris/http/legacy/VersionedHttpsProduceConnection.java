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

import com.adaptris.http.HttpClientTransport;
import com.adaptris.http.HttpException;
import com.adaptris.http.HttpsClient;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.password.Password;
import com.adaptris.util.URLString;

@SuppressWarnings("deprecation")
public class VersionedHttpsProduceConnection extends HttpsProduceConnection {

  private String[] protocolVersion;

  public VersionedHttpsProduceConnection(String... protocolVersion) {
    super();
    this.protocolVersion = protocolVersion;
  }

  /**
   *
   * @see HttpClientConnection#initialiseClient(java.lang.String)
   */
  @Override
  public HttpClientTransport initialiseClient(String url) throws HttpException {
    HttpsClient client = new HttpsClient(new URLString(url), protocolVersion);
    try {
      if (getKeystore() != null) {
        KeystoreFactory ksf = KeystoreFactory.getDefault();
        KeystoreLocation ksl = null;
        if (getKeystorePassword() != null) {
          ksl = ksf.create(getKeystore(), Password.decode(getKeystorePassword()).toCharArray());
        }
        else {
          ksl = ksf.create(getKeystore());
        }
        char[] pkpw = PasswordOverride.discoverPrivateKeyPassword(ksl, getPrivateKeyPasswordProvider());
        if (pkpw != null) {
          client.registerPrivateKeyPassword(pkpw);
        }
        client.registerKeystore(ksf.create(ksl));
      }
    }
    catch (AdaptrisSecurityException e) {
      throw new HttpException(e);
    }
    client.setAlwaysTrust(getAlwaysTrust());
    return client;
  }

}
