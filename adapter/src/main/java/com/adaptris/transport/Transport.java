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
import java.util.Properties;

/**
 * The framework for handling the various types of transport.
 * <p>
 * Concrete implementations of this class are expected to connect using their respective hardware protocol, and return an object
 * capable of sending and receiving.
 * <p>
 * A <code>Transport</code> client would generally look something like this:-
 * 
 * <pre>
 *  {@code 
 *    Transport transport = Transport.create("ssl");
 *    transport.setConfiguration(myConfiguration);
 *    TransportLayer layer = transport.connect();
 *    layer.send("The quick brown fox".getBytes("UTF-8"));
 *    byte[] bytes = layer.receive();
 *    System.out.println(new String(bytes));
 *  }
 *  </pre>
 * whereas a <code>Transport</code> server may look something like this:-
 * 
 * <pre>
 * {@code 
 *    Transport transport = Transport.create("tcp");
 *    transport.setConfiguration(myConfiguration);
 *    while(running) {
 *      try {
 *        TransportLayer layer = transport.listen(1000);
 *        byte[] bytes = layer.receive();
 *        layer.send("The quick brown fox jumps over the lazy dog".getBytes("UTF-8"));
 *        layer.close();
 *      } catch(InterruptedIOException) {
 *        continue;
 *      }
 *    }
 *  }
 *  </pre>
 * 
 * @see TransportLayer
 * @see TransportConstants
 * @see SSLSocketTransport
 * @see TcpSocketTransport
 */
public abstract class Transport {
  /** Create a new instance of a Transport.
   *  <p>The default types it knows about are "tcp" and "ssl", if an unknown
   *  type is used, then it attempts to create it via
   *  <code>Class.forName().newInstance()</code>
   *  @param type the type of transport to create.
   *  @return Transport a transport implementation, null if it could not be 
   *  created
   */
  public static final Transport create(String type) {
    Transport t = null;
    try {
      do {
        if (type.equalsIgnoreCase("tcp")) {
          t = new TcpSocketTransport();
          break;
        }
        if (type.equalsIgnoreCase("ssl")) {
          t = new SSLSocketTransport();
          break;
        }
        t = (Transport) (Class.forName(type)).newInstance();
      }
      while (false);
    }
    catch (Exception e) {
      t = null;
    }
    return (t);
  }

  /** Set the configuration.
   *  @param p Properties containing all the information required
   *  @see SocketConstants
   *  @throws TransportException on error.
   */
  public abstract void setConfiguration(Properties p)
    throws TransportException;

  /** Make a connection.
   *  <p>If this has to negotiate an X.25 pad, then this is where that happens.
   *  <p>The point where this method should return is when the connection has
   *  been made to the remote server, but before any actual transport protocol
   *  negotiation, other than that required to make the connection has occurred
   *  e.g. there is a connection to the remote server, but the OFTP SSRM has not
   *  yet 
   *  been received.
   *  @throws InterruptedIOException  If the attempt to connect exceeded
   *    the timeout
   *  @throws IllegalStateException  If this TransportLayer object can't make a
   *    client connection because it doesn't know how to
   *  @throws TransportException  Any other Exception
   *  @return an object capable of sending and receiving data
   */
  public abstract TransportLayer connect()
    throws InterruptedIOException, TransportException, IllegalStateException;

  /** Listen for requests.
   *  <p>The method should block until timeout or until a request is received 
   *  and  accepted.
   *  <p>The point where this method should return is when the connection has
   *  been made to the remote server, but before any actual transport protocol
   *  negotiation, other than that required to make the connection has occurred.
   *  e.g. The remote party is connected, but this server has not yet sent the
   *  OFTP SSRM.
   *  @param listenTimeout how long to listen for (ms)
   *  @throws InterruptedIOException if the attempt to listen has exceeded the
   *  listenTimeout
   *  @throws IllegalStateException  If this TransportLayer object can't listen
   *    because it doesn't know how to
   *  @throws TransportException  Any other Exception
   *  @return an object capable of sending and receiving.
   */
  public abstract TransportLayer listen(int listenTimeout)
    throws TransportException, IllegalStateException, InterruptedIOException;

  /** Close this transport and free any underlying resources.
   *  <p>Although TransportLayer itself exposes a close() method, this method
   *  is present as resources may need to be freed by the transport itself 
   *  (e.g. PPP connection).  Depending on the underlying implementation,
   *  invoking this method, may have a detrimental effect on any currently 
   *  open <code>TransportLayer</code> objects.
   *  
   * @throws TransportException on error.
   */
  public abstract void close() throws TransportException;
}
