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

/**
 * Constants used in configuration.
 * <p>
 * All configuration exists in a Properties hashtable, this is expected to contain some or all of
 * the keys defined in this class. The values from these keys will be used to configure the
 * underlying <code>SocketTransport</code> class
 * 
 * @see Transport#setConfiguration(java.util.Properties)
 * @see TcpSocketTransport
 * @see SSLSocketTransport
 */
public interface SocketConstants extends TransportConstants {
  /**
   * The key within the configuration containing the name of the remote host.
   */
  String CONFIG_HOST = "transport.socket.host";

  /**
   * The key within the configuration containing the name of the remote port.
   */
  String CONFIG_PORT = "transport.socket.port";

  /**
   * The key within the configuration containing the listen port.
   */
  String CONFIG_LISTEN = "transport.socket.listen.port";

  /**
   * The key within the configuration containing the length of the timeout to be
   * passed to the transport Layer.
   * 
   * @see TransportLayer#setTimeout(int)
   */
  String CONFIG_TIMEOUT = "transport.socket.timeout";

  /**
   * The key within the configuration containing the blocksize to be passed to
   * the TransportLayer.
   * 
   * @see TransportLayer#setBlockSize(int)
   */
  String CONFIG_BLOCKSIZE = "transport.socket.blocksize";

  /**
   * Always trust the server.
   * <p>
   * If true, then a <code>TrustManager</code> is used that trusts all
   * certificates presented to it.
   */
  String CONFIG_ALWAYS_TRUST = "transport.socket.ssl.always.trust";

  /**
   * Require Client Authoriation.
   * <p>
   * If true, then all incmoing connections that are serviced securely will
   * require a certificate to be presented by the client. This option can be
   * used in conjunction with <code>CONFIG_ALWAYS_TRUST</code> configuration
   * property.
   */
  String CONFIG_CLIENT_AUTH = "transport.socket.ssl.requireclient.auth";

}
