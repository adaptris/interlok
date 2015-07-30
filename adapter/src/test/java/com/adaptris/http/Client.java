/*
 * $RCSfile: Client.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/09/06 07:59:08 $
 * $Author: lchan $
 */
package com.adaptris.http;


/**
 * Interface describing core HTTP Client functionality.
 * 
 * @author lchan
 * 
 */
public interface Client {

  /** The Default Socket Timeout */
  public static final int DEFAULT_SOCKET_TIMEOUT = 5 * 60 * 1000;

  /**
   * Send a document via Http. Send a document, returning either true (if the
   * HTTP response was 2xx) or false if any other response
   * 
   * @param input the input stream containing the data to be sent.
   * @return true or false.
   * @throws HttpException on error.
   */
  boolean sendDocument(byte[] input) throws HttpException;

  /**
   * Send a document via Http. Send a document, returning either true (if the
   * HTTP response was 2xx) or false if any other response
   * 
   * @param input the input stream containing the data to be sent.
   * @param timeout the timeout to wait for
   * @param allowRedirect whether to allow the server to redirect the client
   *          with 3xx response.
   * @return true or false.
   * @throws HttpException on error.
   */
  boolean sendDocument(HttpMessage input, int timeout, boolean allowRedirect)
      throws HttpException;

  /**
   * Send a Http Request. Send a request, returning the session associated with
   * the HTTP Connection,
   * <p>
   * This inputstream will contain the data, without the preceding HTTP header
   * information.
   * </p>
   * <p>
   * If this method is directly invoked, then it is expected that the
   * content-length for this request has previously been set.
   * </p>
   * 
   * @param input the input stream to send.
   * @param timeout the timeout to wait for
   * @param allowRedirect whether to allow the server to redirect the client
   *          with 3xx response.
   * @return an HttpSession which can be used to query the results.
   * @throws HttpException
   */
  HttpSession send(HttpMessage input, int timeout, boolean allowRedirect)
      throws HttpException;

  /**
   * Send a Http Request. Send a request, returning the session associated with
   * the HTTP Connection,
   * <p>
   * This inputstream will contain the data, without the preceding HTTP header
   * information.
   * </p>
   * <p>
   * If this method is directly invoked, then it is expected that the
   * content-length for this request has previously been set.
   * </p>
   * 
   * @param input the input stream to send.
   * @return an HttpSession which can be used to query the results.
   * @throws HttpException
   */
  HttpSession send(byte[] input) throws HttpException;

  /**
   * Set the HTTP Method to be used.
   * 
   * @param s the method.
   */
  void setMethod(String s);

}
