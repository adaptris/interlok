package com.adaptris.http.legacy;

import com.adaptris.http.HttpClientTransport;
import com.adaptris.http.HttpException;

/** Interface for handling client connections.
 * @author lchan
 * @author $Author: hfraser $
 */
public interface HttpClientConnection {

  /** Make a connection to the specified URL.
   * 
   * @param url the url.
   * @return an instance of HttpClientTransport
   * @throws HttpException if there was an error making the connection.
   */
  HttpClientTransport initialiseClient(String url) throws HttpException;
}
