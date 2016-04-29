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

package com.adaptris.core.socket;

import java.io.IOException;
import java.net.Socket;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.NoOpConnection;

/** Abstract ProduceConnection class for the Socket Adapter.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class ProduceConnection extends NoOpConnection {

  @AdvancedConfig
  private int socketTimeout;
  
  /** @see Object#Object()
   * 
   *
   */
  public ProduceConnection() {
    socketTimeout = 60000;
  }

  /** Create the socket that will be used by the Producer.
   *  <p>The destination is expected to be a URL that can be handled by the
   *  concrete implementation of ProduceConnection.
   * @return the socket.
   * @param dest the destination to produce to.
   * @throws IOException if there was an error creating a socket, or if the
   * destination was unparseable
   */
  public abstract Socket createSocket(String dest) throws IOException;
  
  /** Get the configured timeout.
   * 
   * @return the timeout value in milliseconds
   */
  public int getSocketTimeout() {
    return socketTimeout;
  }
  
  /** Set the timeout in milliseconds for socket operations.
   * 
   * @param i the timeout.
   */
  public void setSocketTimeout(int i) {
    socketTimeout = i;
  }
}
