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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of Transport.
 * <p>
 * This is a plain socket implementation of the Transport class, and requires no
 * special java classes other than then standard <code>java.net.Socket</code>
 * <p>
 * This Transport type can be configured in one of two ways, either via a
 * <code>java.util.Properties</code> object or directly using the setters and
 * getters.
 * </p>
 * <p>
 * The configuration properties are :-
 * <ul>
 * <li><code>transport.socket.host</code> The remote host to connect to in
 * client mode.</li>
 * <li><code>transport.socket.port</code> The remote port to connect to</li>
 * <li><code>transport.socket.timeout</code> The time to wait for a <code>
 *  connect(), read(), or send()</code>
 * to complete.</li>
 * <li><code>transport.socket.listen.port</code> The port to listen on for
 * incoming requests.</li>
 * <li><code>transport.socket.blocksize</code> The initial block size that is
 * given to the underlying TransportLayer.</li>
 * </ul>
 *
 * @see Socket
 * @see TransportLayer
 * @see SocketConstants
 */
public class TcpSocketTransport extends Transport implements SocketConstants {

  /**
   * The configuration object.
   *
   * @see #setConfiguration(Properties)
   */
  protected Properties config = null;

  /** The remote host. */
  private String host = null;

  /** The timeout. */
  private int socketTimeout = 0;
  /** blocksize. */
  private int blockSize = 1024;
  /** the remote port. */
  private int port = -1;
  /** The local port to listen on. */
  private int listenPort = -1;

  protected ServerSocket serverSocket = null;

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());
  protected transient Logger socketLogger = LoggerFactory.getLogger("com.adaptris.transport.SocketLogger");

  /**
   * @see Object#Object()
   *
   *
   */
  public TcpSocketTransport() {
    super();
  }

  /**
   * Set the connect timeout.
   *
   * @param i the timeout.
   */
  public void setConnectTimeout(int i) {
    this.socketTimeout = i;
  }

  /**
   * Get the connect timeout.
   *
   * @return the connect timeout.
   */
  public int getConnectTimeout() {
    return socketTimeout;
  }

  /**
   * Set the blocksize for send and receive of data.
   *
   * @param i the blocksize.
   */
  public void setBlockSize(int i) {
    this.blockSize = i;
  }

  /**
   * Get the blocksize.
   *
   * @return the blocksize
   */
  public int getBlockSize() {
    return blockSize;
  }

  /**
   * Set the host to connect to.
   *
   * @param remoteHost the host.
   */
  public void setHost(String remoteHost) {
    this.host = remoteHost;
  }

  /**
   * Get the host to connect to
   *
   * @return the host.
   */
  public String getHost() {
    return host;
  }

  /**
   * Set the port to connect to
   *
   * @param i the port.
   */
  public void setPort(int i) {
    this.port = i;
  }

  /**
   * Get the port to connect to
   *
   * @return the port
   *
   */
  public int getPort() {
    return port;
  }

  /**
   * Set the listen port.
   *
   * @param i the listen port.
   */
  public void setListenPort(int i) {
    this.listenPort = i;
  }

  /**
   * Get the listen port.
   *
   * @return the listen port.
   */
  public int getListenPort() {
    return listenPort;
  }

  /**
   * Set the configuration for this transport layer.
   * <p>
   * The properties object is expected to contain information sufficient for
   * this object to make a socket connection to a remote machine and listen for
   * requests from a remote machine
   *
   * @see SocketConstants#CONFIG_HOST
   * @see SocketConstants#CONFIG_PORT
   * @see SocketConstants#CONFIG_LISTEN
   * @see SocketConstants#CONFIG_TIMEOUT
   * @see SocketConstants#CONFIG_BLOCKSIZE
   * @param p the configuration properties
   * @throws TransportException on error.
   */
  @Override
  public void setConfiguration(Properties p) throws TransportException {
    config = p;
    initFromProperties();
  }

  /**
   * @see Transport#connect()
   */
  @Override
  public TransportLayer connect() throws InterruptedIOException, TransportException, IllegalStateException {
    return doConnect();
  }

  /**
   * @see Transport#listen(int)
   */
  @Override
  public TransportLayer listen(int listenTimeout) throws InterruptedIOException, TransportException, IllegalStateException {
    return doListen(listenTimeout);
  }

  private TransportLayer doConnect() throws InterruptedIOException, TransportException, IllegalStateException {
    SocketLayer socket = null;
    Socket clientSocket = null;
    // Get the configuration.
    if (port == -1 || host == null) {
      throw new IllegalStateException("No client configuration for this " + "TransportLayer");
    }
    try {
      logR.debug("Tcp : Attempting to connect to " + host + ":" + port);

      clientSocket = new Socket();
      InetSocketAddress addr = new InetSocketAddress(getHost(), getPort());
      clientSocket.connect(addr, getConnectTimeout());
      // clientSocket = new Socket(host, port);
      socket = new SocketLayer(clientSocket);
      socket.setTimeout(socketTimeout);
      socket.setBlockSize(blockSize);
      printSocketInfo(clientSocket);
    }
    catch (InterruptedIOException e) {
      throw e;
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
    return socket;
  }

  private TransportLayer doListen(int listenTimeout) throws TransportException, IllegalStateException, InterruptedIOException {
    SocketLayer socket = null;
    if (listenPort == -1) {
      throw new IllegalStateException("No listen configuration for this " + "TransportLayer");
    }
    try {
      // logR.debug("Tcp : Listening on " + listenPort);
      if (serverSocket == null) {
        serverSocket = new ServerSocket(listenPort, 255);
      }
      serverSocket.setSoTimeout(listenTimeout);
      Socket sock = serverSocket.accept();
      socket = new SocketLayer(sock);
      socket.setTimeout(socketTimeout);
      socket.setBlockSize(blockSize);
      printSocketInfo(sock);
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
   * @see com.adaptris.transport.Transport#close()
   */
  @Override
  public void close() throws TransportException {
    try {
      if (serverSocket != null) {
        serverSocket.close();
      }
    }
    catch (Exception e) {
      throw new TransportException(e.getMessage(), e);
    }
    finally {
      serverSocket = null;
    }
    return;
  }

  /**
   * Perform initialisation from properties.
   *
   * @throws TransportException on error.
   */
  protected void initFromProperties() throws TransportException {
    try {
      // logR.trace("Tcp : Initialising from properties\n" + listProperties());
      this.setHost(config.getProperty(CONFIG_HOST));
      this.setPort(Integer.parseInt(config.getProperty(CONFIG_PORT, "-1")));
      this.setListenPort(Integer.parseInt(config.getProperty(CONFIG_LISTEN, "-1")));
      this.setConnectTimeout(Integer.parseInt(config.getProperty(CONFIG_TIMEOUT, "60000")));
      this.setBlockSize(Integer.parseInt(config.getProperty(CONFIG_BLOCKSIZE, "1024")));
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [blockSize=" + blockSize + ", config=" + config + ", host=" + host + ", listenPort="
        + listenPort + ", port=" + port + ", serverSocket=" + serverSocket + ", socketTimeout=" + socketTimeout + "]";
  }

  /**
   * Print informatino about the socket.
   *
   * @param clientSocket the socket that is being used to talk to the remote
   *          party.
   */
  protected void printSocketInfo(Socket clientSocket) throws Exception {
    socketLogger.trace("Socket Info" + "\nBound Port     :" + clientSocket.getLocalPort() + "\nBound Address  :"
        + clientSocket.getLocalAddress().toString() + "\nRemote Port    :" + clientSocket.getPort() + "\nRemote Address :"
        + clientSocket.getInetAddress().toString() + "\nSO_LINGER      :" + clientSocket.getSoLinger() + "\nTCP_NODELAY    :"
        + clientSocket.getTcpNoDelay() + "\nKEEP_ALIVE     :" + clientSocket.getKeepAlive() + "\nSO_TIMEOUT     :"
        + clientSocket.getSoTimeout() + "\nSendBuffer     :" + clientSocket.getSendBufferSize() + "\nReceiveBuffer  :"
        + clientSocket.getReceiveBufferSize());
  }

  /**
   * Get a list of the properties.
   *
   * @return String containing all the key value pairs in the configuration
   */
  protected String listProperties() throws Exception {
    StringBuffer sb = new StringBuffer();
    Enumeration e = config.propertyNames();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      String value = config.getProperty(key);
      sb.append(key);
      sb.append("=");
      sb.append(value);
      sb.append("\n");
    }
    return sb.toString();
  }
}
