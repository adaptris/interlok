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
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.adaptris.core.security.ConfiguredPrivateKeyPasswordProvider;
import com.adaptris.core.security.PrivateKeyPasswordProvider;
import com.adaptris.http.HttpException;
import com.adaptris.http.Https;
import com.adaptris.http.HttpsListener;
import com.adaptris.http.Listener;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.password.Password;

/**
 * The consume connection for HTTPs.
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 *
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.jetty.HttpsConnection} instead
 * <p>
 * In the adapter configuration file this class is aliased as <b>https-consume-connection</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@Deprecated
@XStreamAlias("https-consume-connection")
public class HttpsConsumeConnection extends ConsumeConnection {

  private int httpsListenPort = -1;
  private String keystore;
  private String keystorePassword;
  private boolean requireClientAuth = true;
  private boolean alwaysTrust = false;
  private PrivateKeyPasswordProvider privateKeyPasswordProvider;

  public HttpsConsumeConnection() {
    super();
    setPrivateKeyPasswordProvider(new HttpLegacyPrivateKeyPasswordProvider());
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
    if (httpsListenPort == -1) {
      throw new CoreException("no listen port");
    }
    if (keystore == null) {
      log.warn("No Configured keystore/password, " + "SSL configuration will be sourced from " + Https.CONFIG_PROPERTY_FILE);
    }
    if (alwaysTrust && requireClientAuth) {
      log.warn("Configured to ALWAYS trust client certificate, and " + "we require client certificates");
    }
  }

  /**
   * Set the https listen port.
   *
   * @param port the port to listen on.
   */
  public void setHttpsListenPort(int port) {
    httpsListenPort = port;
  }

  /**
   * Get the configured listen port.
   *
   * @return the port.
   */
  public int getHttpsListenPort() {
    return httpsListenPort;
  }

  /**
   * Set the keystore to use.
   *
   * @param keystoreUrl the keystore in the form of a URL as used by the standard security services.
   * @see com.adaptris.core.security.CoreSecurityService #addKeystoreUrl(com.adaptris.security.keystore.ConfiguredKeystore)
   * @see com.adaptris.security.keystore.ConfiguredUrl
   */
  public void setKeystore(String keystoreUrl) {
    keystore = keystoreUrl;
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
   * Set the password to access the keystore.
   * <p>
   * In additional to plain text passwords, the passwords can also be encoded using the appropriate {@link Password}
   * </p>
   *
   * @param password the password, defaults to null as the password may be embedded in {@link #setKeystore(String)}
   */
  public void setKeystorePassword(String password) {
    keystorePassword = password;
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
   * Set the flag requiring client authentication.
   * <p>
   * This flag controls whether the client is required to present a certificate when perform SSL handshake.
   * <p>
   * It has no effect on whether the session is encrypted or not. The session is always encrypted, using any available ciphers.
   *
   * @param b require client authentication
   */
  public void setRequireClientAuth(boolean b) {
    requireClientAuth = b;
  }

  /**
   * Get the flag for client authentication.
   *
   * @return true or false.
   */
  public boolean getRequireClientAuth() {
    return requireClientAuth;
  }

  @Override
  Listener initialiseListener() throws HttpException {
    HttpsListener httpsListener = new HttpsListener(httpsListenPort);
    httpsListener.setServerSocketTimeout(getServerSocketTimeout());
    httpsListener.setSocketTimeout(getSocketTimeout());
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
          httpsListener.registerPrivateKeyPassword(pkpw);
        }
        httpsListener.registerKeystore(ksf.create(ksl));
      }
    }
    catch (AdaptrisSecurityException e) {
      throw new HttpException(e);
    }
    httpsListener.setAlwaysTrust(alwaysTrust);
    httpsListener.setRequireClientAuth(requireClientAuth);
    return httpsListener;
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
