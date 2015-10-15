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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.util.URLString;

/**
 * HttpClientTransport forms the basis of a HTTP Client connection.
 * <p>
 * It is intended as a partial replacement for the default Sun Java class
 * java.net.HttpURLConnection. It can be used when a http connection does not
 * need to be cached. java.net.HttpURLConnection behaves oddly when it
 * encounters HTTP return codes that are outside of a file based scenario...
 * e.g. when 403 is returned by the server (forbidden) an error would be thrown,
 * but getErrorStream would not return a valid InputStream, even though useful
 * data has been returned.
 * <p>
 * The raw java.net.Socket class is used to handle the connection between the
 * two endpoints, the actual connection should be made by the concrete
 * sub-class. In the case of SSL, then it is expected that certificate handling
 * and key exchange should be handled during the createConnection phase
 * <p>
 * Redirection by the remote server is handled, up to a limit of 30
 * redirections, it will also fail if there is an attempt to redirect to an
 * already visited URL.
 * </p>
 * <p>
 * The HTTP version is always HTTP/1.0 initially, this can be changed by
 * manipulating the header to be send to the server.
 * </p>
 * <p>
 * No Support for proxy servers is currently available, nor anticipated for
 * future versions.
 * </p>
 * 
 * @see HttpHeaders
 * @see HttpSession
 * @see HttpMessage
 */
public abstract class HttpClientTransport implements Client {

  /** Hard Limit for number of redirect attempts, currently set to 30 */
  private static final int MAX_REDIRECTS = 30;
  private int redirectCount = 0;
  private Socket connection = null;
  private String method;
  private ArrayList urlList = new ArrayList();
  private HttpMessageFactory messageFactory;

  private int socketTimeout = Client.DEFAULT_SOCKET_TIMEOUT;
  protected URLString currentUrl = null;
  protected transient Log logR;

  /** Default Constructor */
  public HttpClientTransport() {
    logR = LogFactory.getLog(this.getClass());
    messageFactory = HttpMessageFactory.getDefaultInstance();
    setMethod("POST");
  }

  /**
   * construct the class using a string representation of a url.
   * 
   * @param urlString the URL to connect to
   * @throws HttpException on error.
   */
  public HttpClientTransport(String urlString) throws HttpException {
    this();
    setUrl(urlString);
  }

  protected abstract int getPort();

  /**
   * Create an Socket connection
   */
  protected abstract Socket createConnection(int timeout) throws HttpException;

  /**
   * Determine if this url is in fact valid for our connection type.
   */
  protected abstract boolean canHandle(String url);

  protected String getHost() {
    return currentUrl.getHost();
  }

  /**
   * Set the method to use for this client
   * 
   * @param method the method.
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Is the HTTP response code considered to be a success.
   * <p>
   * There are 7 possible HTTP codes that signify success or partial success :-
   * <code>200,201,202,203,204,205,206</code>
   * </p>
   * 
   * @return true if the transaction was successful.
   */
  private boolean wasSuccessful(HttpSession session) {

    boolean rc = false;
    switch (session.getResponseLine().getResponseCode()) {
    case HttpURLConnection.HTTP_ACCEPTED:
    case HttpURLConnection.HTTP_CREATED:
    case HttpURLConnection.HTTP_NO_CONTENT:
    case HttpURLConnection.HTTP_NOT_AUTHORITATIVE:
    case HttpURLConnection.HTTP_OK:
    case HttpURLConnection.HTTP_PARTIAL:
    case HttpURLConnection.HTTP_RESET: {
      rc = true;
      break;
    }
    default: {
      rc = false;
      break;
    }
    }
    return rc;
  }

  /**
   * Set the url
   * 
   * @param urlString the url
   * @throws HttpException on error.
   */
  private void setUrl(String urlString) throws HttpException {
    if (!canHandle(urlString)) {
      throw new HttpException("This url cannot be handled by "
          + this.getClass().getName());
    }
    if (currentUrl != null) {
      urlList.add(currentUrl.toString());
    }
    currentUrl = new URLString(urlString);
  }

  /**
   * 
   * @see Client#sendDocument(byte[])
   */
  public boolean sendDocument(byte[] input) throws HttpException {
    return sendDocument(createHttpMessage(input), Client.DEFAULT_SOCKET_TIMEOUT, false);
  }

  /**
   * 
   * @see Client#sendDocument(HttpMessage, int, boolean)
   */
  public boolean sendDocument(HttpMessage input, int timeout,
                              boolean allowRedirect) throws HttpException {
    boolean rc = false;
    HttpSession session = send(input, timeout, allowRedirect);
    rc = wasSuccessful(session);
    session.close();
    return rc;
  }

  /**
   * 
   * @see com.adaptris.http.Client#send(byte[])
   */
  public HttpSession send(byte[] bytes) throws HttpException {
    return send(createHttpMessage(bytes), Client.DEFAULT_SOCKET_TIMEOUT, false);
  }

  /**
   * 
   * @see com.adaptris.http.Client#send(HttpMessage, int, boolean)
   */
  public HttpSession send(HttpMessage input, int timeout, boolean allowRedirect)
      throws HttpException {
    HttpSession session = null;
    try {
      if (input == null) {
        throw new IOException("Nothing to send");
      }
      connection = createConnection(timeout);
      session = new ClientSession();
      session.setMessageFactory(messageFactory);
      session.setSocket(connection);
      session.setRequestMessage(input);
      session.getRequestLine().setMethod(method);
      populateHeaders(session);
      session.commit();
      if (hasMoved(session.getResponseLine())) {
        if (allowRedirect) {
          session = handleRedirection(session, input, timeout);
        }
        else {
          throw (new IOException(
              "Re-direction, but not enabled for this client"));
        }
      }
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
    return session;
  }

  /**
   * Handle a redirected input.
   * <p>
   * 
   * @param input The input stream to be resent.
   * @return InputStream the resulting data from the Http server
   */
  private HttpSession handleRedirection(HttpSession session, HttpMessage input,
                                        int timeout) throws IOException,
      HttpException {

    URLString newUrl = null;

    HttpHeaders receiveHeader = session.getResponseMessage().getHeaders();
    // Even if we wanted to, can we do so?
    // 301 / 302 gives us a Location: <newlocation>
    if (!receiveHeader.containsHeader(Http.LOCATION)) {
      throw (new IOException("Re-direction, but to nowhere!"));
    }

    if ((++redirectCount) > MAX_REDIRECTS) {
      throw (new IOException("Maxiumum number of redirects has been reached :"
          + MAX_REDIRECTS));
    }

    logR.debug("Redirection enabled, and attempting to handle");

    // Get the new URL
    String newUrlString = (String) receiveHeader.get(Http.LOCATION);
    newUrl = mergeUrl(currentUrl, newUrlString);
    if (urlList.contains(newUrl.toString())) {
      throw (new IOException("Possible recursive redirection, location "
          + newUrl + " already visited"));
    }

    // The new Location could be a
    // http://localhost:8888/NasApp/ICOE/B2BTest?datatype=CIDX201
    // or a /NasApp/ICOE/B2BTest?datatype=CIDX201
    logR.trace("Current uri " + currentUrl + " is being redirected to "
        + newUrl);
    setUrl(newUrl.toString());
    if (!newUrl.getHost().equals(currentUrl.getHost())) {
      input.getHeaders().put(Http.HOST, currentUrl.getHost());
    }
    session.close();
    return this.send(input, timeout, true);
  }

  private boolean hasMoved(HttpResponse r) {
    return (r.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP)
        || (r.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM);
  }

  /**
   * Merge a URLString object with an arbitary string. if original is
   * http://localhost:8888/FRED/FRED?datatype=CIDX201 and newurl =
   * /NasApp/ICOE/B2BTest?datatype=CIDX201 then the merged url should be
   * http://localhost:8888/NasApp/ICOE/B2BTest?datatype=CIDX201
   */
  private static URLString mergeUrl(URLString orig, String newurl) {

    URLString temp = null;
    do {
      temp = new URLString(newurl);
      if (orig == null) {
        break;
      }

      StringBuffer sb = new StringBuffer();
      sb.append((temp.getProtocol() != null) ? temp.getProtocol() : orig
          .getProtocol());
      sb.append("://");
      sb.append((temp.getHost() != null) ? temp.getHost() : orig.getHost());
      sb.append(":");
      sb.append((temp.getPort() != -1) ? temp.getPort() : orig.getPort());
      sb.append("/");
      sb.append((temp.getFile() != null) ? temp.getFile() : orig.getFile());
      temp = new URLString(sb.toString());
    }
    while (false);
    return temp;
  }

  /**
   * Make a nice URI string from the url.
   * 
   * @param url the url
   */
  private static String getUriString(URLString url) {

    StringBuffer urlString = new StringBuffer("");
    if (url == null) {
      urlString.append("/");
    }
    else {
      if (!url.getFile().startsWith("/")) {
        urlString.append("/");
      }
      urlString.append(url.getFile());
    }
    return urlString.toString();
  }

  private void populateHeaders(HttpSession session) throws IOException,
      HttpException {
    HttpMessage msg = session.getRequestMessage();
    HttpHeaders hdr = msg.getHeaders();
    if (!hdr.containsHeader(Http.HOST)) {
      hdr.put(Http.HOST, getHost() + ":" + getPort());
    }
    HttpRequest request = session.getRequestLine();
    request.setURI(getUriString(currentUrl));
  }

  private HttpMessage createHttpMessage(byte[] bytes) throws HttpException {
    HttpMessage result = messageFactory.create();
    try {
      result.getOutputStream().write(bytes);
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
    return result;
  }
}
