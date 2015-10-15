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

package com.adaptris.transport;

import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.adaptris.security.keystore.KeystoreFactory;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.security.password.Password;

/**
 * Concrete implementation of Transport.
 * <p>
 * This implementation will use a JSSE 1.0.2 compliant provider in order to
 * provide the appropriate security and certificate handling.
 * <p>
 * An attempt is made to add the default JSSE provider
 * <code>com.sun.net.ssl.internal.ssl.Provider()</code> in a static
 * initialisation loop, but no error is logged if this fails (not required under
 * JDK1.4)
 * <p>
 * This class naturally extends <code>TcpSocketTransport</code> as a secure
 * socket is simply a encryption/decryption layer over the top of a plain
 * socket. Because of this, the configuration for a <code>SSLSocketTransport
 *  </code>
 * object is similar to that for a <code>TcpSocketTransport</code> object with
 * a number of additions discussed below
 * <ul>
 * <li><code>transport.security.keystore.url</code> The path to the keystore
 * in the as a URL. The url may follow the rules as specified by
 * KeystoreFactory. </li>
 * <li><code>transport.security.keystore.password</code> The password to
 * access the keystore. </li>
 * <li><code>transport.security.keystore.privatekeypassword</code> The
 * private key password </li>
 * <li><code>transport.socket.ssl.always.trust</code> if set to
 * <code>true</code> when this Transport instance is created, then a the
 * private class <code>AlwaysTrustManager</code> is used to verify the
 * certificate chain sent by the remote party
 * <p>
 * The purpose of this private class is to implement a <code>
 *  X509TrustManager</code>
 * that always returns true, regardless of the situation.
 * <p>
 * Its use is primarily during the test phase, when the Certificate Authority or
 * the certificates themselves have not been agreed upon. It will allow any
 * certificate to be used, ensuring the encryption of the data, but not anything
 * else. </li>
 * <li><code>transport.socket.ssl.require.client.auth</code> if set to true
 * the the client is always required to present a certificate. </li>
 * </ul>
 * <p>
 * More information about SSL/TLS can be found on the internet draft <a
 * href='http://www.faqs.org/rfcs/rfc2246.html'>RFC2246</a>. Java Secure Socket
 * Extensions can be found <a href='http://java.sun.com/products/jsse/'> here</a>
 *
 * @see SocketConstants
 * @see TransportConstants
 * @see TcpSocketTransport
 * @see com.adaptris.security.keystore.KeystoreFactory
 */
public final class SSLSocketTransport extends TcpSocketTransport {

  private String keystoreUrl = null;
  private String keyStorePassword = null;
  private String privateKeyPassword = null;

  private boolean alwaysTrust = false;
  private boolean requireClientAuth = false;
  private boolean securityInitialised = false;

  private SSLSocketFactory clientSocketFactory = null;
  private SSLServerSocketFactory serverSocketFactory = null;

  /** These are hard-coded for now */
  private static final String SSL_CONTEXT_TYPE = "SSL";
  private static final String KEY_MANAGER_TYPE = "SunX509";

  static {
    try {
      ;
      // Uncomment for JDK1.3.1
      // Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    }
    catch (Exception ignoredIntentionally) {
      ;
    }
  }

  /**
   * @see Object#Object()
   *
   *
   */
  public SSLSocketTransport() {
    super();
  }

  /**
   * @see com.adaptris.transport.Transport#connect()
   */
  @Override
  public TransportLayer connect()
      throws InterruptedIOException, TransportException, IllegalStateException {
    return doConnect();
  }

  /**
   * @see com.adaptris.transport.Transport#listen(int)
   */
  @Override
  public TransportLayer listen(int listenTimeout)
      throws TransportException, IllegalStateException, InterruptedIOException {
    return doListen(listenTimeout);
  }

  /**
   * Get the always trust value.
   *
   * @return the always trust flag.
   */
  public boolean getAlwaysTrust() {
    return alwaysTrust;
  }

  /**
   * Get the keystore password
   *
   * @return the kesytore password
   */
  public String getKeystorePassword() {
    return keyStorePassword;
  }

  /**
   * Get the keystore path.
   *
   * @return the keystore path
   */
  public String getKeystoreUrl() {
    return keystoreUrl;
  }

  /**
   * Get the private key password
   *
   * @return the private key password
   */
  public String getPrivateKeyPassword() {
    return privateKeyPassword;
  }

  /**
   * Get the require client authentication flag.
   *
   * @return the flag.
   */
  public boolean getRequireClientAuth() {
    return requireClientAuth;
  }

  /**
   * set the AlwaysTrust flag.
   *
   * @param b true or false.
   */
  public void setAlwaysTrust(boolean b) {
    alwaysTrust = b;
  }

  /**
   * Set the keystore password.
   *
   * <p>
   * In additional to plain text passwords, the passwords can also be encoded
   * using the appropriate {@link Password}
   * </p>
   *
   * @param pw the password, defaults to null as the password may be embedded in
   *          {@link #setKeystoreUrl(String)}
   */
  public void setKeystorePassword(String pw) {
    keyStorePassword = pw;
  }

  /**
   * Set the Keystore path
   *
   * @param string the path
   */
  public void setKeystoreUrl(String string) {
    keystoreUrl = string;
  }

  /**
   * Set the private key password *
   * <p>
   * In additional to plain text passwords, the passwords can also be encoded
   * using the appropriate {@link Password}
   * </p>
   *
   * @param pw the password
   */
  public void setPrivateKeyPassword(String pw) {
    privateKeyPassword = pw;
  }

  /**
   * Set the require client authentication flag.
   *
   * @param b true or false
   */
  public void setRequireClientAuth(boolean b) {
    requireClientAuth = b;
  }

  private TransportLayer doConnect()
      throws InterruptedIOException, TransportException, IllegalStateException {
    // Get the configuration.
    SocketLayer socket = null;
    Socket clientSocket = null;
    if (getPort() == -1 || getHost() == null) {
      throw new IllegalStateException("No client configuration for this "
          + "TransportLayer");
    }
    try {
      this.initSecurity();
      logR.debug("SSL : Attempting to connect to " + getHost() + ":"
          + getPort());
      clientSocket = clientSocketFactory.createSocket();
      InetSocketAddress addr = new InetSocketAddress(getHost(), getPort());
      clientSocket.connect(addr, getConnectTimeout());
      // clientSocket = clientSocketFactory.createSocket(getHost(), getPort());
      socket = new SocketLayer(clientSocket);
      socket.setTimeout(getConnectTimeout());
      socket.setBlockSize(getBlockSize());
      this.printSocketInfo(clientSocket);
    }
    catch (InterruptedIOException e) {
      e.fillInStackTrace();
      throw e;
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
    return socket;
  }

  private TransportLayer doListen(int listenTimeout)
      throws TransportException, IllegalStateException, InterruptedIOException {
    SocketLayer socket = null;
    if (getListenPort() == -1) {
      throw new IllegalStateException("No listen configuration for this "
          + "TransportLayer");
    }
    try {
      this.initSecurity();
      // logR.debug("SSL : Listening on " + listenPort);
      if (serverSocket == null) {
        serverSocket = serverSocketFactory.createServerSocket(getListenPort(),
            255);
        ((SSLServerSocket) serverSocket).setNeedClientAuth(requireClientAuth);
      }
      serverSocket.setSoTimeout(listenTimeout);
      Socket sock = serverSocket.accept();
      socket = new SocketLayer(sock);
      socket.setTimeout(getConnectTimeout());
      socket.setBlockSize(getBlockSize());
      this.printSocketInfo(sock);
    }
    catch (InterruptedIOException e) {
      socket = null;
      throw e;
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
    return socket;
  }

  /**
   * @see com.adaptris.transport.TcpSocketTransport#initFromProperties()
   */
  @Override
  protected void initFromProperties() throws TransportException {
    super.initFromProperties();
    try {
      // logR.trace("SSL : Initialising from properties\n" + listProperties());
      String urlString = config.getProperty(CONFIG_KEYSTORE_FILE);
      if (urlString == null) {
        throw new TransportException("No keystore config");
      }

      String kspw = config.getProperty(CONFIG_KEYSTORE_PW);
      if (kspw != null) {
        setKeystorePassword(kspw);
      }
      setKeystoreUrl(urlString);
      String pkpw = config.getProperty(CONFIG_PRIVATE_KEY_PW);
      if (pkpw != null) {
        setPrivateKeyPassword(pkpw);
      }
      setAlwaysTrust(Boolean.valueOf(config.getProperty(CONFIG_ALWAYS_TRUST,
          Boolean.FALSE.toString())).booleanValue());
      setRequireClientAuth(Boolean.valueOf(config.getProperty(
          CONFIG_CLIENT_AUTH, Boolean.TRUE.toString())).booleanValue());
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
  }

  /**
   * Initialise security settings.
   * <p>
   * This initialises the SSL context, and loads the keystore
   */
  private void initSecurity() throws Exception {
    if (securityInitialised) {
      return;
    }
    SSLContext ctx = SSLContext.getInstance(SSL_CONTEXT_TYPE);
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
    KeystoreLocation ksl = null;
    KeystoreFactory fac = KeystoreFactory.getDefault();

    if (keyStorePassword == null) {
      ksl = fac.create(keystoreUrl);
    }
    else {
      ksl = fac.create(keystoreUrl, Password.decode(keyStorePassword).toCharArray());
    }
    KeystoreProxy ksp = fac.create(ksl);
    ksp.load();
    KeyStore ks = ksp.getKeystore();
    kmf.init(ks, Password.decode(privateKeyPassword).toCharArray());
    if (alwaysTrust) {
      // Always trust the certificate ! - quite dangerous ;)
      TrustManager[] tm = new TrustManager[1];
      tm[0] = new AlwaysTrustManager();
      ctx.init(kmf.getKeyManagers(), tm, null);
    }
    else {
      TrustManagerFactory tmf = TrustManagerFactory
          .getInstance(KEY_MANAGER_TYPE);
      tmf.init(ks);
      ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    }
    clientSocketFactory = ctx.getSocketFactory();
    serverSocketFactory = ctx.getServerSocketFactory();
    securityInitialised = true;
    return;
  }

  /**
   * Always trust manager
   * <p>
   * The purpose of this class is to implement an X509TrustManager that always
   * returns true, regardless of the situation.
   * <p>
   * Its use is primary during the test phase, when the Certificate Authority or
   * the certificates themselves have not been agreed upon. It will allow any
   * certificate to be used, ensuring the encryption of the data, but not
   * anything else.
   */
  private class AlwaysTrustManager implements X509TrustManager {
    AlwaysTrustManager() {
    }

    public X509Certificate[] getAcceptedIssuers() {
      X509Certificate[] x = new X509Certificate[0];
      return x;
    }

    public boolean isClientTrusted(X509Certificate[] chain) {
      return true;
    }

    public boolean isServerTrusted(X509Certificate[] chain) {
      return true;
    }

    @Override
    public String toString() {
      return "toString: AlwaysTrustManager";
    }

    public void checkClientTrusted(X509Certificate[] x509Certificate, String str)
        throws CertificateException {
      return;
    }

    public void checkServerTrusted(X509Certificate[] x509Certificate, String str)
        throws CertificateException {
      return;
    }

  }
}
