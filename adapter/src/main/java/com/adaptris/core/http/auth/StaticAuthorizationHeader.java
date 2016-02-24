package com.adaptris.core.http.auth;

import java.net.HttpURLConnection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.HttpConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("http-static-authorization-header")
public class StaticAuthorizationHeader implements HttpAuthenticator {

  private String headerValue;
  
  public String getHeaderValue() {
    return headerValue;
  }

  /**
   * The value for the authorization header
   * @param headerValue
   */
  public void setHeaderValue(String headerValue) {
    this.headerValue = headerValue;
  }

  @Override
  public HttpAuthenticator setup(AdaptrisMessage msg) throws CoreException {
    return this;
  }

  @Override
  public void configureConnection(HttpURLConnection conn) {
    conn.addRequestProperty(HttpConstants.AUTHORIZATION, headerValue);
  }

  @Override
  public void close() {
  }

}
