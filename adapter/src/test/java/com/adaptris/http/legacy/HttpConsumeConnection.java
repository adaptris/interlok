/*
 * $RCSfile: HttpConsumeConnection.java,v $
 * $Revision: 1.11 $
 * $Date: 2008/07/30 08:16:22 $
 * $Author: lchan $
 */
package com.adaptris.http.legacy;

import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.adaptris.http.HttpException;
import com.adaptris.http.HttpListener;
import com.adaptris.http.Listener;

/**
 * The Consume Connection for vanilla Http.
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 *
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.jetty.HttpConnection} instead
 * <p>
 * In the adapter configuration file this class is aliased as <b>http-consume-connection</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@Deprecated
@XStreamAlias("http-consume-connection")
public class HttpConsumeConnection extends ConsumeConnection {
  private int httpListenPort = -1;

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
    if (httpListenPort == -1) {
      throw new CoreException("no listen port");
    }
    // Sort out config here.
  }

  /**
   * Set the Http Listen Port.
   *
   * @param port the port to listen on.
   */
  public void setHttpListenPort(int port) {
    httpListenPort = port;
  }

  /**
   * Get the port we're listening on.
   *
   * @return the listen port
   */
  public int getHttpListenPort() {
    return httpListenPort;
  }

  @Override
  Listener initialiseListener() throws HttpException {
    HttpListener http = new HttpListener(httpListenPort);
    http.setServerSocketTimeout(getServerSocketTimeout());
    http.setSocketTimeout(getSocketTimeout());
    return http;
  }

}
