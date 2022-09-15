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
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.security.util.AlwaysTrustManager;

/**
 * This is a listener for HTTPS requests.
 * <p>
 * It is possible to configure this listener directly from a set of properties
 * on the classpath. The file is <b>adp-http.properties</b> and should contain
 * the properties.
 * <ul>
 * <li>adp.https.keystoreurl=url</li>
 * <li>adp.https.privatekeypassword=privatekeypassword</li>
 * </ul>
 * <p>
 * These properties will only be used, if the corresponding get/set methods on
 * the listener have not been invoked. The private key password must always
 * exist in this file and cannot be set programmatically.
 * </p>
 *
 * @see HttpListener
 */
public class HttpsListener extends HttpListener {

  private char[] privateKeyPassword = null;
  private KeystoreProxy keystoreProxy;
  private boolean alwaysTrust = false;
  private boolean requireClientAuth = false;

  /**
   * @see HttpListener#HttpListener(int)
   */
  public HttpsListener(int listenPort) {
    super(listenPort);
  }

  /**
   * @see HttpListener#HttpListener(int, int)
   */
  public HttpsListener(int listenPort, int poolSize) {
    super(listenPort, poolSize);
  }

  /**
   * Require a client to present a certificat.
   *
   * @param b
   *          true if clients must always present a certificate
   */
  public void setRequireClientAuth(boolean b) {
    requireClientAuth = b;
  }

  /**
   * Get the require authorisation flag.
   *
   * @return true or false, depending
   */
  public boolean getRequireClientAuth() {
    return requireClientAuth;
  }

  /**
   * Always trust the client certificate.
   * <p>
   * If set to true (not the default state), then a special trustmanager is used
   * that always returns true when queried about a certificate chain
   * <p>
   * This is quite a dangerous flag to set because it means that you don't care
   * whether the client is "who it says it is".
   * </p>
   * <p>
   * Regardless of the flag setting, the actual data communication is encrypted
   * </p>
   *
   * @param b true of false
   */
  public void setAlwaysTrust(boolean b) {
    alwaysTrust = b;
  }

  /**
   * Get the always trust flag.
   *
   * @return the flag true or false
   */
  public boolean getAlwaysTrust() {
    return alwaysTrust;
  }

  /**
   * @see HttpListener#initialise()
   */
  @Override
  public void initialise() throws HttpException {
    try {
      if (initialised) {
        return;
      }
      privateKeyPassword = Https.getPrivateKeyPassword(privateKeyPassword);
      keystoreProxy = Https.getKeystoreProxy(keystoreProxy);
      serverSocket = initSocket(initSecurity());
      listenPort = serverSocket.getLocalPort();
      initialised = true;
    }
    catch (Exception e) {
      throw new HttpException(e);
    }
    return;
  }

  private SSLContext initSecurity() throws Exception {
    SSLContext sslContext = SSLContext.getInstance(Https.SSL_CONTEXT_TYPE);

    KeyManagerFactory kmf = KeyManagerFactory
        .getInstance(Https.KEY_MANAGER_TYPE);
    keystoreProxy.load();
    KeyStore ks = keystoreProxy.getKeystore();
    if (ks == null) {
      throw new IOException("Failed to get a handle on a keystore "
          + keystoreProxy);
    }
    kmf.init(ks, privateKeyPassword);
    if (alwaysTrust) {
      // Always trust the certificate ! - quite dangerous ;)
      TrustManager[] tm = new TrustManager[1];
      tm[0] = new AlwaysTrustManager();
      sslContext.init(kmf.getKeyManagers(), tm, null);
    }
    else {
      TrustManagerFactory tmf = TrustManagerFactory
          .getInstance(Https.KEY_MANAGER_TYPE);
      tmf.init(ks);
      sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    }
    return sslContext;
  }

  private SSLServerSocket initSocket(SSLContext sslContext) throws IOException {

    ServerSocketFactory serverSocketFactory = sslContext
        .getServerSocketFactory();
    SSLServerSocket s = (SSLServerSocket) serverSocketFactory
        .createServerSocket(listenPort, 1024);
    if (logR.isTraceEnabled()) {
      logR.trace("Server socket timeout set to " + serverSocketTimeout + "ms");
    }
    s.setSoTimeout(serverSocketTimeout);
    s.setNeedClientAuth(requireClientAuth);
    if (logR.isInfoEnabled()) {
      logR.info("Initialised to listen for HTTPS connections on port "
          + s.getLocalPort());
    }
    return s;
  }

  /**
   * Register the keystore to use when listening for connections.
   *
   * @param keystoreProxy the keystoreProxy to set
   */
  public void registerKeystore(KeystoreProxy keystoreProxy) {
    this.keystoreProxy = keystoreProxy;
  }

  /**
   * Register a private key password.
   *
   * @param pkpw
   */
  public void registerPrivateKeyPassword(char[] pkpw) {
    privateKeyPassword = pkpw;
  }
}
