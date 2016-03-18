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

import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.security.ConfiguredPrivateKeyPasswordProvider;
import com.adaptris.core.security.PrivateKeyPasswordProvider;
import com.adaptris.http.HttpClientTransport;
import com.adaptris.http.HttpException;
import com.adaptris.http.Https;
import com.adaptris.http.HttpsClient;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Produce connection for HTTPS
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 * 
* <p>
 * In the adapter configuration file this class is aliased as <b>https-produce-connection</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 * @author $Author: lchan $
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.JdkHttpProducer} instead
 */
@Deprecated
@XStreamAlias("https-produce-connection")
public class HttpsProduceConnection extends NullConnection implements HttpClientConnection {

  private String keystore = null;
  private String keystorePassword = null;
  private boolean alwaysTrust = false;
  private PrivateKeyPasswordProvider privateKeyPasswordProvider;

  public HttpsProduceConnection() {
    super();
    setPrivateKeyPasswordProvider(new HttpLegacyPrivateKeyPasswordProvider());
  }

  /**
   *
   * @see com.adaptris.core.NullConnection#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
    super.initConnection();
    if (keystore == null) {
      log.warn("No Configured keystore/password, " + "SSL configuration will be sourced from " + Https.CONFIG_PROPERTY_FILE);
    }
    if (alwaysTrust) {
      log.warn("Configured to ALWAYS trust server certificate");
    }
  }

  /**
   * Set the keystore to use.
   *
   * @param s the keystore in the form of a URL as used by the standard security services.
   * @see com.adaptris.core.security.CoreSecurityService #addKeystoreUrl(com.adaptris.security.keystore.ConfiguredKeystore)
   * @see com.adaptris.security.keystore.ConfiguredUrl
   */
  public void setKeystore(String s) {
    keystore = s;
  }

  /**
   * Set the password to access the keystore.
   * <p>
   * In additional to plain text passwords, the passwords can also be encoded using the appropriate {@link com.adaptris.security.password.Password}
   * </p>
   *
   * @param s the password, defaults to null as the password may be embedded in {@link #setKeystore(String)}
   */
  public void setKeystorePassword(String s) {
    keystorePassword = s;
  }

  /**
   * The configured keystore.
   *
   * @return the keystore.
   */
  public String getKeystore() {
    return keystore;
  }

  /**
   * Get the keystore password.
   *
   * @return the password.
   */
  public String getKeystorePassword() {
    return keystorePassword;
  }

  /**
   * Set the Always Trust flag.
   * <p>
   * Always trust implies that you always trust the certificate that is presented by the client, even if it is revoked or not in
   * your keystore.
   * <p>
   * It has no effect on whether the session is encrypted or not. The session is always encrypted, using any available ciphers.
   *
   * @param b true or false.
   */
  public void setAlwaysTrust(boolean b) {
    alwaysTrust = b;
  }

  /**
   * Get the always trust flag.
   *
   * @return true or false.
   */
  public boolean getAlwaysTrust() {
    return alwaysTrust;
  }

  /**
   *
   * @see HttpClientConnection#initialiseClient(java.lang.String)
   */
  @Override
  public HttpClientTransport initialiseClient(String url) throws HttpException {
    HttpsClient client = new HttpsClient(url);
    try {
      if (keystore != null) {
        KeystoreFactory ksf = KeystoreFactory.getDefault();
        KeystoreLocation ksl = null;
        if (keystorePassword != null) {
          ksl = ksf.create(keystore, Password.decode(keystorePassword).toCharArray());
        }
        else {
          ksl = ksf.create(keystore);
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
    client.setAlwaysTrust(alwaysTrust);
    return client;
  }

  public PrivateKeyPasswordProvider getPrivateKeyPasswordProvider() {
    return privateKeyPasswordProvider;
  }

  /**
   * Set the private key password provider.
   *
   * @param pkpp the provider; default is {@link HttpLegacyPrivateKeyPasswordProvider} which retrieves the private key password from
   *          'adp-http.properties' on the classpath to support backward compatibility.
   * @see HttpLegacyPrivateKeyPasswordProvider
   * @see ConfiguredPrivateKeyPasswordProvider
   */
  public void setPrivateKeyPasswordProvider(PrivateKeyPasswordProvider pkpp) {
    privateKeyPasswordProvider = pkpp;
  }
}
