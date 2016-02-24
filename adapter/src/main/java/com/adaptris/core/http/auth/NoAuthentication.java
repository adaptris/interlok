package com.adaptris.core.http.auth;

import java.net.HttpURLConnection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("http-no-authentication")
public class NoAuthentication implements HttpAuthenticator {

  @Override
  public HttpAuthenticator setup(AdaptrisMessage msg) throws CoreException {
    ThreadLocalCredentials.getInstance().removeThreadCredentials();
    return this;
  }

  @Override
  public void configureConnection(HttpURLConnection conn) {
  }

  @Override
  public void close() {
  }

}
