package com.adaptris.http.legacy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.mail.Header;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.CoreException;
import com.adaptris.http.Http;
import com.adaptris.http.HttpException;
import com.adaptris.http.HttpHeaders;
import com.adaptris.http.HttpSession;
import com.adaptris.http.RequestProcessor;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.license.License;
import com.adaptris.util.text.Conversion;

/**
 * Abstract class for consumers of an HTTP Connection.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class HttpConsumerImp extends AdaptrisMessageConsumerImp
    implements RequestProcessor {

  private static final String BASIC_AUTH_PREFIX = "Basic ";
  private static final String COLON = ":";
  private String headerPrefix;
  private boolean preserveHeaders;

  private boolean traceLogging;

  /**
   * @see AdaptrisMessageConsumerImp#AdaptrisMessageConsumerImp()
   *
   *
   */
  public HttpConsumerImp() {
    super();
    headerPrefix = "";
    preserveHeaders = false;
  }

  /**
   * @see AdaptrisMessageConsumerImp#close()
   */
  @Override
  public void close() {
    // does nothing
  }

  /**
   * @see AdaptrisMessageConsumerImp#init()
   */
  @Override
  public void init() throws CoreException {
    // check config
  }

  /**
   * @see AdaptrisMessageConsumerImp#start()
   */
  @Override
  public void start() throws CoreException {
    // does nothing
  }

  /**
   * @see AdaptrisMessageConsumerImp#stop()
   */
  @Override
  public void stop() {
    // does nothing
  }

  /**
   * @see RequestProcessor#getUri()
   */
  @Override
  public String getUri() {
    return getDestination().getDestination();
  }

  /**
   * Enable or Disable low level trace logging.
   *
   * @param b true or false
   */
  public void setTraceLogging(boolean b) {
    traceLogging = b;
  }

  /**
   * Get the configured low level trace logging configuration.
   *
   * @return true or false.
   */
  public boolean getTraceLogging() {
    return traceLogging;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License l) throws CoreException {
    return true;
  }

  protected KeyValuePair getUsernamePassword(HttpHeaders h) {
    KeyValuePair kp = null;
    if (h.containsHeader(Http.AUTHORIZATION)) {
      String line = h.get(Http.AUTHORIZATION).trim();
      if (line.startsWith(BASIC_AUTH_PREFIX)) {
        String base64 = line.replaceAll(BASIC_AUTH_PREFIX, "");
        byte[] b = Conversion.base64StringToByteArray(base64);
        String upw = new String(b);
        if (!COLON.equals(upw)) {
          StringTokenizer st = new StringTokenizer(new String(b), COLON);
          kp = new KeyValuePair(st.nextToken(), st.nextToken());
        }
      }
    }
    return kp;
  }

  /**
   * @see RequestProcessor#processRequest(HttpSession)
   */
  @Override
  public final void processRequest(HttpSession httpSession)
      throws IOException, IllegalStateException, HttpException {

    ConsumeConnection c = retrieveConnection(ConsumeConnection.class);
    renameThread();
    if (getTraceLogging()) {
      log.trace("Request Headers : "
          + httpSession.getRequestMessage().getHeaders().toString());
      log.trace("Default response headers : "
          + httpSession.getResponseMessage().getHeaders().toString());
      log.trace("Default response content type : "
          + httpSession.getResponseMessage().getContentType());
    }

    KeyValuePair kp = getUsernamePassword(httpSession.getRequestMessage()
        .getHeaders());

    if (!c.isValid(kp)) {
      httpSession.getResponseLine().setResponseCode(
          HttpURLConnection.HTTP_FORBIDDEN);
      if (kp != null) {
        httpSession.getResponseLine().setResponseMessage(
            (!"".equals(kp.getKey()) ? kp.getKey() : "<no user>")
                + " has invalid credentials");
      }
      else {
        httpSession.getResponseLine()
            .setResponseMessage("Credentials required");
      }
      return;
    }

    AdaptrisMessage m = handleRequest(httpSession);
    if (m != null) {
      if (getPreserveHeaders()) {
        Enumeration e = httpSession.getRequestMessage().getHeaders()
            .getAllHeaders();
        while (e.hasMoreElements()) {
          Header h = (Header) e.nextElement();
          m.addMetadata(getHeaderPrefix() + h.getName(), h.getValue());
        }
      }
      retrieveAdaptrisMessageListener().onAdaptrisMessage(m);
    }
  }

  /**
   * Toggle header preservation.
   * <p>
   * If set to true, then an attempt is made to preserve all the HTTP headers
   * that were sent as part of the request, in the message metadata.
   *
   * @param b true to enable header preservation.
   * @see #setHeaderPrefix(String)
   */
  public void setPreserveHeaders(boolean b) {
    preserveHeaders = b;
  }

  /**
   * Get the flag currently controlling header preservation.
   *
   * @return the header preservation flag.
   * @see #setPreserveHeaders(boolean)
   */
  public boolean getPreserveHeaders() {
    return preserveHeaders;
  }

  /**
   * Set the header prefix for any stored headers.
   *
   * @param s the header prefix
   * @see #setPreserveHeaders(boolean)
   */
  public void setHeaderPrefix(String s) {
    headerPrefix = s;
  }

  /**
   * get the header prefix for any stored headers.
   *
   * @return the header prefix
   * @see #setPreserveHeaders(boolean)
   */
  public String getHeaderPrefix() {
    return headerPrefix;
  }

  /**
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer("[");
    sb.append(HttpConsumerImp.class.getName());
    sb.append(",headerPrefix=").append(getHeaderPrefix());
    sb.append(",preserveHeaders=").append(getPreserveHeaders());
    sb.append(",traceLogging=").append(getTraceLogging());
    sb.append("]");
    return sb.toString();
  }

  protected abstract AdaptrisMessage handleRequest(HttpSession session)
      throws IOException, IllegalStateException, HttpException;
}
