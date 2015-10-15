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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Abstract implementation of HttpSession
 */
abstract class HttpSessionImp implements HttpSession {

  private boolean alreadyCommitted;
  private Socket socket;
  protected transient Log logR = null;
  // protected ByteArrayInputStream input = null;
  protected ByteArrayOutputStream output = null;
  protected HttpRequest httpRequestLine = null;
  protected HttpResponse httpResponseLine = null;
  protected HttpMessage requestMessage = null;
  protected HttpMessage responseMessage = null;

  protected HttpMessageFactory httpMessageFactory =
    HttpMessageFactory.getDefaultInstance();
  protected transient Log socketLogger;

  protected HttpSessionImp() {
    logR = LogFactory.getLog(this.getClass());
    socketLogger = Http.getSocketLogger();
    output = new ByteArrayOutputStream();
    httpRequestLine = new HttpRequest();
    httpResponseLine = new HttpResponse();
  }

  /** @see HttpSession#getRequestLine()
   */
  public HttpRequest getRequestLine() {
    return httpRequestLine;
  }

  /** @see HttpSession#getResponseLine()
   */
  public HttpResponse getResponseLine() {
    return httpResponseLine;
  }

  /** @see HttpSession#setMessageFactory(HttpMessageFactory)
   */
  public void setMessageFactory(HttpMessageFactory f) {
    httpMessageFactory = f;
  }

  /** @see HttpSession#setSocket(Socket)
   */
  public void setSocket(Socket s) throws HttpException {
    socket = s;
    this.displaySocketInfo();
    initialise();
  }

  /** @see HttpSession#close()
   */
  public void close() throws HttpException {
    try {
      socket.close();
    } catch (Exception e) {
      throw new HttpException(e.getMessage(), e);
    }
  }

  /** @see HttpSession#commit()
   */
  public void commit() throws HttpException {
    if (alreadyCommitted) {
      logR.trace("Session already commited once, ignoring");
    } else {
      commitToSocket();
      alreadyCommitted = true;
    }
  }

  /** Return the socket
   */
  protected Socket getSocket() {
    return socket;
  }

  /** Initialisation to be done by concrete classes.
   *  @throws HttpException on error
   */
  protected abstract void initialise() throws HttpException;

  /** Perform the commit to the socket
   * 
   * @throws HttpException on error.
   */
  protected abstract void commitToSocket() throws HttpException;

  /** Log the socket information.
   *  <p>Conceivably it could be done through reflection...
   *
   */
  private void displaySocketInfo() {
    try {

      if (socketLogger.isTraceEnabled()) {
        socketLogger.trace(
          "Socket information"
            + "\nBound Port     :"
            + socket.getLocalPort()
            + "\nBound Address  :"
            + socket.getLocalAddress().toString()
            + "\nRemote Port    :"
            + socket.getPort()
            + "\nRemote Address :"
            + socket.getInetAddress().toString()
            + "\nSO_LINGER      :"
            + socket.getSoLinger()
            + "\nTCP_NODELAY    :"
            + socket.getTcpNoDelay()
            + "\nKEEP_ALIVE     :"
            + socket.getKeepAlive()
            + "\nSO_TIMEOUT     :"
            + socket.getSoTimeout()
            + "\nSendBuffer     :"
            + socket.getSendBufferSize()
            + "\nReceiveBuffer  :"
            + socket.getReceiveBufferSize());
      }
    } catch (Exception e) {
      ;
    }
  }

  protected void finalizeSocket() {
    try {
      HttpHeaders response = responseMessage.getHeaders();
      HttpHeaders request = requestMessage.getHeaders();
      // If Connection: Close is specified in either header or 
      // the version is HTTP/1.0 (which doesn't support Connection: Keep-Alive)
      // the close the socket as we are finished with it.
      if (Http.CLOSE.equals(response.get(Http.CONNECTION))
        || Http.CLOSE.equals(request.get(Http.CONNECTION))
        || Http.VERSION_1.equalsIgnoreCase(httpRequestLine.getVersion())
        || Http.VERSION_1.equalsIgnoreCase(httpResponseLine.getVersion())) {

        getSocket().close();
      }
    } catch (IOException ignoredByDesign) {
      ;
    }
  }
}
