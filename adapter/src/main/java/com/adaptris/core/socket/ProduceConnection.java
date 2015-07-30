/*
 * $RCSfile: ProduceConnection.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/04/29 11:36:41 $
 * $Author: lchan $
 */
package com.adaptris.core.socket;

import java.io.IOException;
import java.net.Socket;

import com.adaptris.core.NullConnection;

/** Abstract ProduceConnection class for the Socket Adapter.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class ProduceConnection extends NullConnection {

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
