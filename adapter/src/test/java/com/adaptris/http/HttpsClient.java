package com.adaptris.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.adaptris.http.util.AlwaysTrustManager;
import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.util.URLString;

/**
 * <p>
 * A simple HTTPS client based on the JSSE reference SSL implementation.
 * </p>
 * 
 * @see HttpClientTransport
 * @see HttpClient
 */
public class HttpsClient extends HttpClientTransport {

  private char[] privateKeyPassword;
  private boolean alwaysTrust = false;
  private boolean initialised = false;
  private KeystoreProxy keystoreProxy;
  private SSLSocketFactory socketFactory;
  private String[] protocols = null;

  /**
   * @see Object#Object()
   * 
   * 
   */
  public HttpsClient() {
    super();
  }

  /**
   * @see com.adaptris.http.HttpClientTransport#getPort()
   */
  public int getPort() {
    return (currentUrl.getPort() == -1) ? 443 : currentUrl.getPort();
  }

  /**
   * Constructor
   * 
   * @param url the url to connect to
   * @throws HttpException on error.
   */
  public HttpsClient(String url) throws HttpException {
    super(url);
  }

  /**
   * Constructor
   * 
   * @param url the url to connect to
   * @throws HttpException on error.
   */
  public HttpsClient(URLString url, String... protocols) throws HttpException {
    super(url.toString());
    this.protocols = protocols;
  }

  /**
   * Always trust the server.
   * <p>
   * If set to true (not the default state), then a special trustmanager is used that always returns true when queried about a
   * certificate chain
   * <p>
   * This is quite a dangerous flag to set because it means that you don't care whether the server is "who it says it is".
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
   * @see HttpClientTransport#createConnection(int)
   */
  protected Socket createConnection(int timeout) throws HttpException {

    SSLSocket sslSocket = null;
    try {
      if (!initialised) {
        initSecurity();
        initialised = true;
      }
      logR.debug("Connecting to " + this.getHost() + ":" + this.getPort());
      sslSocket = (SSLSocket) socketFactory.createSocket();
      if (protocols != null) sslSocket.setEnabledProtocols(protocols);
      SocketAddress address = new InetSocketAddress(this.getHost(), ((this.getPort() == -1) ? 443 : this.getPort()));
      sslSocket.setSoTimeout(timeout);
      sslSocket.connect(address, timeout);
      logR.debug("Connected using " + sslSocket.getSession().getCipherSuite());
    }
    catch (Exception e) {
      throw new HttpException(e);
    }
    return sslSocket;
  }

  /**
   * @see HttpClientTransport#canHandle(String)
   */
  protected boolean canHandle(String url) {
    URLString urlString = new URLString(url);
    if (urlString.getProtocol() == null || !urlString.getProtocol().equalsIgnoreCase("https")) {
      return false;
    }
    return true;
  }

  private void initSecurity() throws Exception {
    initProperties();
    SSLContext ctx = SSLContext.getInstance(Https.SSL_CONTEXT_TYPE);
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(Https.KEY_MANAGER_TYPE);
    keystoreProxy.load();
    KeyStore ks = keystoreProxy.getKeystore();
    if (ks == null) {
      throw new IOException("Failed to get a handle on a keystore " + keystoreProxy);
    }
    kmf.init(ks, privateKeyPassword);
    if (alwaysTrust) {
      // Always trust the certificate ! - quite dangerous ;)
      TrustManager[] tm = new TrustManager[1];
      tm[0] = (TrustManager) new AlwaysTrustManager();
      ctx.init(kmf.getKeyManagers(), tm, null);
    }
    else {
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(Https.KEY_MANAGER_TYPE);
      tmf.init(ks);
      ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    }
    socketFactory = ctx.getSocketFactory();
  }

  private void initProperties() throws Exception {
    privateKeyPassword = Https.getPrivateKeyPassword(privateKeyPassword);
    keystoreProxy = Https.getKeystoreProxy(keystoreProxy);
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