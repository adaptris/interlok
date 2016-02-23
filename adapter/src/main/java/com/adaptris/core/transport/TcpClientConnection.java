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

package com.adaptris.core.transport;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
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
@ComponentProfile(summary = "Connection for a client tcp connection", tag = "connections,socket,tcp")
@DisplayOrder(order = {"host", "port", "timeout", "blocksize"})
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
