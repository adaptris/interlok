package com.adaptris.http;

import java.net.Socket;

/** The interface that defines an httpsession.
 *  <p>An Http Session consists of a request message and then a response 
 *  message in reply.  Each of these messages is prefixed by a 
 *  Http-Request-Line or a Http-Status-Line respectively.
 */
public interface HttpSession {

  /** Commit any data to be sent.
   *  <p>This physically writes the data to the socket, and should be used 
   *  after the last of the payload has been written to the outputstream.
   *  </p>
   *  @throws HttpException on any error
   */
  void commit() throws HttpException;
  
  /** Close the session and free any underlying resources
   *  @throws HttpException on any error
   */
  void close() throws HttpException;
  
  /** Set the socket to be used for this session.
   *  @param s the socket
   *  @throws HttpException on any error
   */
  void setSocket(Socket s) throws HttpException;
  
  /** The message from a Http Request.
   *  @return the request message
   *  @throws HttpException on error.
   */
  HttpMessage getRequestMessage() throws HttpException;
  
  /** The message from a HttpResponse.
   *  @return the response message
   */
  HttpMessage getResponseMessage();
  
  /** Set the request message for this session.
   *  @param msg the request message.
   */
  void setRequestMessage(HttpMessage msg);
  
  /** Set the Response message for this session.
   *  @param msg the response message
   */
  void setResponseMessage(HttpMessage msg);
  
  /** Set the message factory to be used to parse messages.
   *  @param f the factory.
   */
  void setMessageFactory(HttpMessageFactory f);
  
  /** Set the HttpRequest Line
   *  @param req the httpRequest line.
   */
  void setRequestLine(HttpRequest req);
  
  /** Get the HttpRequest Line
   *  @return the httprequest
   */
  HttpRequest getRequestLine();
  
  /** Set the Http Response Line
   *  @param resp the response 
   */
  void setResponseLine(HttpResponse resp);
  
  /** Get the HttpResponse Line.
   *  @return the response line
   */
  HttpResponse getResponseLine();
  
  /** Whether or not this session allows no Content-Length Header or not.
   * 
   * @return true or false.
   */
  boolean allowsNoContentLength();
    
}