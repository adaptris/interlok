package com.adaptris.core.transport;

import com.adaptris.transport.TcpSocketTransport;
import com.adaptris.transport.Transport;
import com.adaptris.transport.TransportException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Class that acts as configuration for a client tcp connection.
 * <p>
 * A client connection is one where we actually make the connection to the remote server.
 * </p>
 * 
 * @config tcp-client-connection
 * 
 * @author lchan
 * @author $Author: hfraser $
 */
@XStreamAlias("tcp-client-connection")
public class TcpClientConnection extends TransportConfig {

  private String host;
  private int port;

  /** @see Object#Object()
   *
   *
   */
  public TcpClientConnection() {
    super();
  }

  public TcpClientConnection(String host, int port) {
    this();
    setHost(host);
    setPort(port);
  }

  /** Get the port to connect to.
   *
   * @return the port.
   */
  public int getPort() {
    return port;
  }

  /** Set the port to connect to.
   *
   * @param remotePort the port.
   */
  public void setPort(int remotePort) {
    this.port = remotePort;
  }

  /** Set the host to connect to.
   *
   * @param remoteHost the host.
   */
  public void setHost(String remoteHost) {
    this.host = remoteHost;
  }

  /** Get the host to connect to.
   *
   * @return the host.
   */
  public String getHost() {
    return this.host;
  }

  /** @see com.adaptris.core.transport.TransportConfig#createTransport()
   */
  @Override
  public Transport createTransport() throws TransportException {
    return initialiseTransport();
  }

  private Transport initialiseTransport() throws TransportException {
    TcpSocketTransport layer = new TcpSocketTransport();
    layer.setBlockSize(getBlockSize());
    layer.setPort(getPort());
    layer.setHost(getHost());
    layer.setConnectTimeout(getTimeoutMs());
    return layer;
  }
}
